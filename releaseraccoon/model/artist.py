from releaseraccoon.app.app import db
from sqlalchemy.ext.associationproxy import association_proxy


# todo: @dataclass https://docs.python.org/3/library/dataclasses.html
class Artist(db.Model):
    __tablename__ = 'artists'
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(64), nullable=False, unique=True)
    has_new_release = db.Column(db.Boolean)

    users = association_proxy('user_artist', 'user')

    def __init__(self, name: str):
        self.name = name

    def __repr__(self):
        return f'{self.name}'

    def as_dict(self):
        return {c.name: getattr(self, c.name) for c in self.__table__.columns}
