from sqlalchemy import Column, String, Integer, Date, Table, ForeignKey
from sqlalchemy.orm import relationship

from base import Base

user_artist_association = Table('user_artists', Base.metadata,
                                Column('user_id', Integer, ForeignKey('users.id')),
                                Column('artist_id', Integer, ForeignKey('artists.id'))
                                )


class User(Base):
    __tablename__ = 'users'
    id = Column(Integer, primary_key=True)
    classes = relationship("Class", secondary=user_artist_association)

    def __init__(self, email: str, ):
        self.email = email
        

