import dateparser
from sqlalchemy.ext.associationproxy import association_proxy

from releaseraccoon.app.app import db


# todo: @dataclass https://docs.python.org/3/library/dataclasses.html
class Artist(db.Model):
    __tablename__ = 'artists'
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(64), nullable=False, unique=True)
    spotify_uri = db.Column(db.String(64), unique=True)

    users = association_proxy('user_artist', 'user')

    def __init__(self, name: str, spotify_uri: str = None, has_new_release: str = False):
        """
        :param name: Artist name
        :param spotify_uri: Used for easy linking in the future.
        :param has_new_release: Will determine if tracking users need an update.
        """
        self.name = name
        self.spotify_uri = spotify_uri

        self.has_new_release = has_new_release

    def __repr__(self):
        return f'{self.name}'

    def as_dict(self):
        return {c.name: getattr(self, c.name) for c in self.__table__.columns}


class User(db.Model):
    __tablename__ = 'users'
    id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(64), nullable=False, unique=True)
    lastfm_username = db.Column(db.String(64), unique=True)

    # To be updated per notification such that we don't notify too frequently
    notified_on = db.Column(db.Date, unique=False)
    notify_frequency_days = db.Column(db.Integer, unique=False, default=7)

    # association proxy of "user_artists" collection
    # to "artists" attribute
    artists = association_proxy('user_artist', 'artist')

    def __init__(self, email: str, lastfm_username: str):
        self.email = email
        self.lastfm_username = lastfm_username

    def as_dict(self):
        return {c.name: getattr(self, c.name) for c in self.__table__.columns}

    def normalize_weights(self, max_weight: int):
        """

        :param max_weight:
        :return:
        """
        assert max_weight > 0
        for user_artist in self.user_artist:
            user_artist.weight /= max_weight


class Release(db.Model):
    __tablename__ = 'releases'
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(64), unique=False)
    release_type = db.Column(db.String(20), unique=False)
    spotify_uri = db.Column(db.String(64), unique=True)
    date = db.Column(db.Date, unique=False)

    def __init__(self,
                 name: str,
                 date: str,
                 spotify_uri: str = None,
                 release_type: str = None):
        """
        :param name: Release name.
        :param date: Date released, only interested in calendar date.
        :param spotify_uri: Used for easy linking in the future.
        :param release_type: Currently one of album/single.
        """
        self.name = name
        self.date = dateparser.parse(date).date()
        self.release_type = release_type
        self.spotify_uri = spotify_uri

    def as_dict(self):
        return {c.name: getattr(self, c.name) for c in self.__table__.columns}


class UserArtist(db.Model):
    __tablename__ = 'user_artist'
    id = db.Column(db.Integer, primary_key=True)
    artist_id = db.Column(db.Integer, db.ForeignKey('artists.id'), primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), primary_key=True)
    weight = db.Column(db.Float)

    has_new_release = db.Column(db.Boolean, default=False)

    artist = db.relationship('Artist', backref='user_artist')
    user = db.relationship('User', backref='user_artist')


class ArtistRelease(db.Model):
    __tablename__ = 'artist_release'
    artist_id = db.Column(db.Integer, db.ForeignKey('artists.id'), primary_key=True)
    release_id = db.Column(db.Integer, db.ForeignKey('releases.id'), primary_key=True)

    artist = db.relationship('Artist', backref='artist_release')
    release = db.relationship('Release', backref='artist_release')
