from datetime import time

from releaseraccoon.app.app import db


class Release(db.Model):
    __tablename__ = 'releases'
    id = db.Column(db.Integer, primary_key=True)

    def __init__(self, name: str, release_date: time):
        self.name = name
        self.release_date = release_date

    def as_dict(self):
        return {c.name: getattr(self, c.name) for c in self.__table__.columns}
