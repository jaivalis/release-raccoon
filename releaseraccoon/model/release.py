from datetime import time

from sqlalchemy import Column, Integer

from releaseraccoon.base import Base


class Release(Base):
    __tablename__ = 'releases'
    id = Column(Integer, primary_key=True)
    
    def __init__(self, name: str, release_date: time):
        self.name = name
        self.release_date = release_date
