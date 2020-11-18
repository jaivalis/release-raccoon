import logging

from releaseraccoon.app.app import session
from releaseraccoon.model import Artist, User, UserArtist
from releaseraccoon.scraper.lastfm_scraper import LastFmScraper
from releaseraccoon.app.db_util import get_or_create

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
