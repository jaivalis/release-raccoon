from sqlalchemy.orm import relationship, backref
from sqlalchemy.ext.associationproxy import association_proxy
from releaseraccoon.app.app import db


class User(db.Model):
    __tablename__ = 'users'
    id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(64), nullable=False, unique=True)
    lastfm_username = db.Column(db.String(64), unique=True)
    
    artists = relationship('Artist', secondary='user_artist')
    
    def __init__(self, email: str, lastfm_username: str):
        self.email = email
        self.lastfm_username = lastfm_username

    def as_dict(self):
        return {c.name: getattr(self, c.name) for c in self.__table__.columns}
    
    # association proxy of "user_artists" collection
    # to "artists" attribute
    artists = association_proxy('user_artist', 'artist')


class UserArtist(db.Model):
    __tablename__ = 'user_artist'
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), primary_key=True)
    artist_id = db.Column(db.Integer, db.ForeignKey('artists.id'), primary_key=True)
    weight = db.Column(db.Integer)
    
    user = db.relationship('User', backref='user_artist')
    artist = db.relationship('Artist', backref='user_artist')
