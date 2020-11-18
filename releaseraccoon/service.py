import logging

from releaseraccoon.app.app import session
from releaseraccoon.model import Artist, User, Release, UserArtist
from releaseraccoon.scraper.lastfm_scraper import LastFmScraper
from releaseraccoon.scraper.spotify_scraper import SpotifyScraper
from releaseraccoon.db_util import get_or_create

from releaseraccoon.scraper.scraper import (
    RELEASE_NAME_KEY,
    RELEASE_ARTISTS_KEY,
    RELEASE_TYPE_KEY,
    RELEASE_DATE_KEY,
    RELEASE_SPOTIFY_URI_KEY,
    RELEASE_ARTIST_NAME_KEY,
    RELEASE_ARTIST_SPOTIFY_URI_KEY
)


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


def handle_register_user(email: str, lastfm_username: str) -> bool:
    user = session.query(User).filter_by(email=email).first()
    if user is not None:
        LOG.debug(f'User with email {email} exists')
    else:
        user = User(email, lastfm_username)
        lastfm_scraper = LastFmScraper(user)
        max_weight = 0
        for artist_name, weight in lastfm_scraper.scrape_taste():
            max_weight = max(max_weight, weight)
            artist = get_or_create(session, Artist, name=artist_name)
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


def update_artist_releases() -> None:
    """
    Should hit all the release scraper sources to fetch all releases and update the db
    """
    for release_entry in fetch_all_releases():
        release_spotify_uri = release_entry[RELEASE_SPOTIFY_URI_KEY]
        release_name = release_entry[RELEASE_NAME_KEY]
        release_type = release_entry[RELEASE_TYPE_KEY]
        release_date = release_entry[RELEASE_DATE_KEY]

        # todo implement this
        # db_artists = extract_release_artists(release_entry[RELEASE_ARTISTS_KEY])
        # db_release = extract_release_release()
    session.commit()


def extract_release_artists(release_entry_artists: list) -> list[Artist]:
    """
    Maps a release dict entry from one of the scrapers to
    :param release_entry_artists: artists as originating from the scraper pojo objects
    :return:
    """
    for release_entry_artist in release_entry_artists:
        r_name = release_entry_artists[RELEASE_ARTIST_NAME_KEY]
        r_spotify_uri = release_entry_artists[RELEASE_ARTIST_SPOTIFY_URI_KEY]

        # artist = get_or_create(session, Artist, name=artist_name)
        # todo
    return artist


def extract_release_release(release_entry_artists: list) -> Release:
    """
    todo
    """
    pass


def fetch_all_releases() -> list:
    all_releases = []
    for scraper in RELEASE_SCRAPERS:
        all_releases.append(scraper.scrape_releases())
    # todo: When more scrapers are added, we need to ensure uniqueness per release here.
    LOG.info(f'Scraped a total of {len(all_releases)} releases from {len(RELEASE_SCRAPERS)} sources.')
    return all_releases


def handle_release(artist_name: str, spotify_id=None):
    """
    A new release for a given artist means that the artist table needs to be updated with the flag set to True.
    :param artist_name:
    :param spotify_id:
    :return:
    """
    artist = session.query(Artist).filter_by(name=artist_name).first()
    
    if not artist:
        LOG.debug(f'Artist {artist_name} not found in the db.')
        return

    artist.has_new_release = True
