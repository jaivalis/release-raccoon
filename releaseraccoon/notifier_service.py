import logging

from releaseraccoon.app.app import session
from releaseraccoon.model import Artist, User, Release, UserArtist
from releaseraccoon.db_util import get_one_or_create
from sqlalchemy import exc


LOG = logging.getLogger(__name__)


def get_all_users_with_artists_with_new_releases() -> list:
    return session.query(UserArtist)\
        .filter(UserArtist.has_new_release.is_(True)).all()


def notify_users() -> bool:
    _ = get_all_users_with_artists_with_new_releases()

    return True


# def map_user_releases_generator():

def notify_user(user: User, releases: list) -> None:
    """

    :param user: User to notify
    :param releases: New releases
    :return:
    """
    LOG.info(f'Notifying user {user.email} for releases: {releases}')
    # implementation pending.
