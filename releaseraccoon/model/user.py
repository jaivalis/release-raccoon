from sqlalchemy.orm import relationship

from releaseraccoon.app.app import db


user_artist_association = db.Table('user_artists', db.Model.metadata,
                                   db.Column('user_id', db.Integer, db.ForeignKey('users.id')),
                                   db.Column('artist_id', db.Integer, db.ForeignKey('artists.id')),
                                   db.Column('listens', db.Integer),
                                   )


class User(db.Model):
    __tablename__ = 'users'
    id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(64), nullable=False, unique=True)
    lastfm_username = db.Column(db.String(64), unique=True)

    artists = relationship('Artist', secondary=user_artist_association)

    def __init__(self, email: str, lastfm_username: str):
        self.email = email
        self.lastfm_username = lastfm_username
        self.artists = []

    def as_dict(self):
        return {c.name: getattr(self, c.name) for c in self.__table__.columns}
