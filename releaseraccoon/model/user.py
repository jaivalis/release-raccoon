from sqlalchemy import Column, ForeignKey, Integer, Table, String
from sqlalchemy.orm import relationship

from releaseraccoon.base import Base

user_artist_association = Table('user_artists', Base.metadata,
                                Column('user_id', Integer, ForeignKey('users.id')),
                                Column('artist_id', Integer, ForeignKey('artists.id')),
                                Column('listens', Integer),
                                )


class User(Base):
    __tablename__ = 'users'
    id = Column(Integer, primary_key=True)
    email = Column(String(64), nullable=False, unique=True)
    lastfm_username = Column(String(64), unique=True)

    artists = relationship('Artist', secondary=user_artist_association)

    def __init__(self, email: str, lastfm_username: str):
        self.email = email
        self.lastfm_username = lastfm_username
        self.artists = []

    def as_dict(self):
        return {c.name: getattr(self, c.name) for c in self.__table__.columns}
