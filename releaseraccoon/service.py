import logging

from sqlalchemy import exc

from releaseraccoon.app.app import session
from releaseraccoon.db_util import get_one_or_create
from releaseraccoon.model import Artist, ArtistRelease, Release, User, UserArtist
from releaseraccoon.scraper.lastfm_scraper import LastFmScraper
from releaseraccoon.scraper.scraper import (RELEASE_ARTISTS_KEY, RELEASE_ARTIST_NAME_KEY,
                                            RELEASE_ARTIST_SPOTIFY_URI_KEY, RELEASE_DATE_KEY, RELEASE_NAME_KEY,
                                            RELEASE_SPOTIFY_URI_KEY, RELEASE_TYPE_KEY)
from releaseraccoon.scraper.spotify_scraper import SpotifyScraper

LOG = logging.getLogger(__name__)


def get_all_users() -> list:
    ret = []
    for user in session.query(User).all():
        ret.append(user.as_dict())
    return ret


def get_all_artists() -> list:
    ret = []
    for artist in session.query(Artist).all():
        ret.append(artist.as_dict())
    return ret


def get_all_releases() -> list:
    ret = []
    for release in session.query(Release).all():
        ret.append(release.as_dict())
    return ret


def handle_register_user(email: str, lastfm_username: str) -> bool:
    user = session.query(User).filter_by(email=email).first()
    if user is not None:
        LOG.debug(f'User with email {email} exists')
    else:
        user = User(email, lastfm_username)
        lastfm_scraper = LastFmScraper(user.lastfm_username)
        max_weight = 0
        for artist_name, weight in lastfm_scraper.scrape_taste():
            max_weight = max(max_weight, weight)
            artist, _ = get_one_or_create(session, Artist, name=artist_name)
            user.user_artist.append(UserArtist(user=user, artist=artist, weight=weight))
        user.normalize_weights(max_weight)
        session.add(user)
        session.commit()

    return user.as_dict()


def update_users_taste() -> None:
    """
    All the users in the db are re-scraped and updated.
    """
    for user in session.query(User).all():
        lastfm_scraper = LastFmScraper(user)
        lastfm_scraper.scrape_taste()
    session.commit()


#############################################################
# Can and probably should be extracted into a:
#       releases_service.py
#############################################################
RELEASE_SCRAPERS = [
    SpotifyScraper(),
]


def already_processed(spotify_uri: str) -> bool:
    """
    Returns true if the release is already in the db.

    Logic needs to be updated if new release scrapers are added.
    :param spotify_uri: unique spotify identifier.
    :return: boolean
    """
    return session.query(Release)\
        .filter(Release.spotify_uri == spotify_uri)\
        .first() is not None


def update_artist_releases() -> bool:
    """
    Should hit all the release scraper sources to fetch all releases and update the db.

    :return: True if operation successful.
    """
    try:
        artists_ids_with_new_releases = set()
        for release_entry in fetch_all_releases():
            release_spotify_uri = release_entry[RELEASE_SPOTIFY_URI_KEY]
            release_name = release_entry[RELEASE_NAME_KEY]
            release_type = release_entry[RELEASE_TYPE_KEY]
            release_date = release_entry[RELEASE_DATE_KEY]

            if already_processed(release_spotify_uri):
                continue

            release_artists = extract_release_artists(release_entry[RELEASE_ARTISTS_KEY])
            release = extract_release_release(
                release_spotify_uri,
                release_name,
                release_type,
                release_date
            )

            for artist in release_artists:
                artists_ids_with_new_releases.add(artist.id)
                # Add ArtistRelease association entries:
                release.artist_release.append(ArtistRelease(artist=artist, release=release))

        if update_userartists_has_new_release(artists_ids_with_new_releases):
            session.commit()
            return True
    except exc.SQLAlchemyError:
        LOG.warning('Exception occurred when updating artist releases', exc_info=True)
        return False


def update_userartists_has_new_release(artists_ids_with_new_releases: set) -> bool:
    try:
        session.query(UserArtist)\
            .filter(UserArtist.artist_id.in_(artists_ids_with_new_releases))\
            .update({UserArtist.has_new_release: True}, synchronize_session=False)
        return True
    except exc.SQLAlchemyError:
        LOG.warning('Exception occurred when updating UserArtist table', exc_info=True)
        return False


def extract_release_artists(release_entry_artists: list) -> list:
    """
    Maps a release dict entry from one of the scrapers to
    :param release_entry_artists: artists as originating from the scraper pojo objects
    :return: tuple[Artist]
    """
    artists = []
    for release_entry_artist in release_entry_artists:
        r_name = release_entry_artist[RELEASE_ARTIST_NAME_KEY]
        r_spotify_uri = release_entry_artist[RELEASE_ARTIST_SPOTIFY_URI_KEY]

        artist, _ = get_one_or_create(session, Artist,
                                      name=r_name)
        # Extract a method out of the following update(s) if more relevant fields end up getting scraped
        if r_spotify_uri is not None and artist.spotify_uri is None:
            artist.spotify_uri = r_spotify_uri

        artists.append(artist)
    return artists


def extract_release_release(release_spotify_uri: str,
                            release_name: str,
                            release_type: str,
                            release_date: str) -> Release:
    """

    :param release_spotify_uri:
    :param release_name:
    :param release_type:
    :param release_date:
    :return:
    """
    release, _ = get_one_or_create(session, Release,
                                   name=release_name,
                                   date=release_date,
                                   spotify_uri=release_spotify_uri,
                                   release_type=release_type)
    return release


def fetch_all_releases() -> list:
    """
    Traverse all scrapers, return all scraped releases

    :return: list of dict with all releases.
    """
    all_releases = []
    for scraper in RELEASE_SCRAPERS:
        all_releases.extend(scraper.scrape_releases())
    # todo: When more release scrapers are added, we need to ensure uniqueness per release here.
    LOG.info(f'Scraped a total of {len(all_releases)} releases from {len(RELEASE_SCRAPERS)} sources.')
    return all_releases
