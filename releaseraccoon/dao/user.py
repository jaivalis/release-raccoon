from sqlalchemy import Column, ForeignKey, Integer, Table
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
    artists = relationship('Artist', secondary=user_artist_association)

    def __init__(self, email: str, ):
        self.email = email
        

