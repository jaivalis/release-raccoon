import logging

from releaseraccoon.app.app import session
from releaseraccoon.model import Artist, User
from releaseraccoon.scraper.lastfm_scraper import LastFmScraper

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
    # lookup existing users
    
    user = session.query(User).filter_by(email=email).first()
    if user is not None:
        LOG.debug(f'User with email {email} exists')
    else:
        user = User(email, lastfm_username)
        lastfm_scraper = LastFmScraper(user)
        lastfm_scraper.scrape_taste(10)
        session.add(user)
        session.commit()
        
    return user.as_dict()


def handle_update_user(user: User):
    lastfm_scraper = LastFmScraper(user)
    lastfm_scraper.scrape_taste(10)
    session.add(user)
    session.commit()
