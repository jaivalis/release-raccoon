from sqlalchemy import Column, ForeignKey, Integer, Table
from sqlalchemy.orm import relationship

from releaseraccoon.base import Base

artist_release_association = Table('artist_releases', Base.metadata,
                                   Column('artist_id', Integer, ForeignKey('artists.id')),
                                   Column('release_id', Integer, ForeignKey('releases.id'))
                                   )


class Artist(Base):
    __tablename__ = 'artists'
    id = Column(Integer, primary_key=True)
    releases = relationship("Release", secondary=artist_release_association)

    def __init__(self, name: str):
        self.name = name
        
    def __repr__(self):
        return f'{self.name}'

