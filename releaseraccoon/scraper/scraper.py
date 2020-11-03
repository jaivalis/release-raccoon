from zope.interface import Interface

from releaseraccoon.model import Artist


class IMusicScraper(Interface):

    def scrape(user_name: str, limit: int):
        """Gets top artists."""

    def _map_artist(cls, entry: object) -> Artist:
        """Maps an entry row originating from the API to an Artist object."""
