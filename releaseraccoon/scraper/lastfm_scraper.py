from typing import Generator

import pylast
from pylast import TopItem
from zope.interface import implementer

from releaseraccoon import settings
from releaseraccoon.model import Artist, User  # todo decouple from model, use only lastfm_name
from releaseraccoon.scraper.scraper import IMusicReleaseScraper, IMusicTasteScraper

API_KEY = settings.lastfm_api_key
API_SECRET = settings.lastfm_shared_secret

OVERALL = 'PERIOD_OVERALL'
DEFAULT_SCRAPE_HISTORY = 100


@implementer(IMusicTasteScraper)
@implementer(IMusicReleaseScraper)
class LastFmScraper:

    network = pylast.LastFMNetwork(api_key=API_KEY, api_secret=API_SECRET)

    def __init__(self, user: User):
        self.user = user
    
    @classmethod
    def _map_artist(cls, entry: TopItem) -> tuple:
        return entry.item.name, int(entry.weight)

    def get_artists(self, api_call_result: tuple) -> Generator[tuple, None, None]:
        for row in api_call_result:
            artist, weight = LastFmScraper._map_artist(row)
            yield artist, weight

    def scrape_taste(self, limit: int = DEFAULT_SCRAPE_HISTORY) -> list:
        lastfm_user = LastFmScraper.network.get_user(self.user.lastfm_username)
        api_call_result = lastfm_user.get_top_artists(limit=limit, period=OVERALL)
        ret = []
        for artist, weight in self.get_artists(api_call_result):
            # should update the existing entry and not add more with the same name
            ret.append([artist, weight])
        return ret
    
    def scrape_releases(self, limit: int):
        pass
