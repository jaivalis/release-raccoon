import logging

from releaseraccoon.app import app
from releaseraccoon.model import Artist, ArtistRelease, User, Release, UserArtist
from sqlalchemy import exc
from datetime import datetime, timedelta


LOG = logging.getLogger(__name__)


class NotifierService:

    def __init__(self, _session=app.session):
        self.session = _session

    def get_all_userartists_with_new_releases_grouped_by_artist(self) -> list:
        return self.session.query(UserArtist)\
            .filter(UserArtist.has_new_release == '1')\
            .group_by(UserArtist.user_id).all()

    def get_artist_latest_releases_since(self, artist_id: int, day_frequency: int) -> list:
        """
        Joins ArtistRelease, Artist & Release tables to return all relevant info to be used to update users

        :param artist_id: artist to look for
        :param day_frequency: retrieve releases after `day_frequency` days ago.
        :return: tuple of [ArtistRelease, Artist, Release]
        """
        current_time = datetime.utcnow()
        x_days_ago = current_time - timedelta(days=day_frequency)

        return self.session.query(ArtistRelease, Artist, Release).join(Artist)\
            .filter(ArtistRelease.artist_id == artist_id)\
            .filter(Release.id == ArtistRelease.release_id)\
            .filter(Artist.id == ArtistRelease.artist_id)\
            .filter(Release.date > x_days_ago)\
            .all()

    def _update_userartist_has_new_release(self, user_id: int) -> bool:
        try:
            self.session.query(UserArtist) \
                .filter(UserArtist.user_id == user_id) \
                .update({UserArtist.has_new_release: '0'}, synchronize_session=False)
            return True
        except exc.SQLAlchemyError:
            LOG.warning('Exception occurred when updating UserArtist table', exc_info=True)
            raise

    def notify_users(self) -> bool:
        user_artists_grouped_by_artist = self.get_all_userartists_with_new_releases_grouped_by_artist()
        if not user_artists_grouped_by_artist:
            LOG.info('Nothing to notify about.')
            return True

        try:
            current_user = user_artists_grouped_by_artist[0].user
            releases = []
            for user_artist in user_artists_grouped_by_artist:
                if current_user is not None and current_user is not user_artist.user:
                    self._handle_user_notification(current_user, releases)
                    releases = []

                current_user = user_artist.user
                artist = user_artist.artist

                releases.extend(
                    self.get_artist_latest_releases_since(artist.id, current_user.notify_frequency_days)
                )
            # Notify the last user
            self._handle_user_notification(current_user, releases)
        except exc.SQLAlchemyError:
            LOG.warning('Exception occurred when notifying users.', exc_info=True)
            return False

        return True

    def _handle_user_notification(self, user: User, releases: list) -> None:
        """
        Attempts to update the user, if successful marks the UserArtist.has_new_release to False.

        :param user: user to notify
        :param releases: tuple of [ArtistRelease, Artist, Release]
        :return:
        """
        try:
            self._notify_user(user, releases)
            self._update_userartist_has_new_release(user.id)
            self.session.commit()
        except exc.SQLAlchemyError:
            LOG.warning(f'Exception when notifying user {user}', exc_info=True)
            raise

    def _notify_user(self, user: User, releases: list) -> None:
        """

        :param user: User to notify
        :param releases: tuple of [ArtistRelease, Artist, Release]
        :return:
        """
        for _, artist, release in releases:
            LOG.info(f'Notifying user {user.email} for release(s): {artist}, {release}')
        # implementation pending.


def notify_users() -> bool:
    service = NotifierService()
    return service.notify_users()
