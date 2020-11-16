from zope.interface import Interface

from releaseraccoon.model import Artist
from datetime import date


class IMusicReleaseScraper(Interface):
    """ Scrapes external sources for music releases """
    
    def scrape_releases(cal_date: date, limit: int):
        """

        :param cal_date: a calendar date
        :param limit:
        :return:
        """


class IMusicTasteScraper(Interface):
    """ Scrapes external sources for music taste of a given user """

    def scrape_taste(user_name: str, limit: int):
        """Gets top artists."""

    def _map_artist(cls, entry: object) -> Artist:
        """Maps an entry row originating from the API to an Artist object."""
