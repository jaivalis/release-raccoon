from sqlalchemy.orm import relationship

from releaseraccoon.app.app import db


# todo: @dataclass https://docs.python.org/3/library/dataclasses.html
artist_release_association = db.Table('artist_releases', db.Model.metadata,
                                      db.Column('artist_id', db.Integer, db.ForeignKey('artists.id')),
                                      db.Column('release_id', db.Integer, db.ForeignKey('releases.id'))
                                      )


class Artist(db.Model):
    __tablename__ = 'artists'
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(64), nullable=False, unique=True)

    releases = relationship("Release", secondary=artist_release_association)

    def __init__(self, name: str):
        self.name = name

    def __repr__(self):
        return f'{self.name}'

    def as_dict(self):
        return {c.name: getattr(self, c.name) for c in self.__table__.columns}
