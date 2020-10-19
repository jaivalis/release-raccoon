from typing import Generator

import pylast
from pylast import TopItem
from zope.interface import implementer

from releaseraccoon import settings
from releaseraccoon.model.artist import Artist
from releaseraccoon.model.user import User
from releaseraccoon.scraper.scraper import IMusicScraper

API_KEY = settings.lastfm_api_key
API_SECRET = settings.lastfm_shared_secret


@implementer(IMusicScraper)
class LastFmScraper:

    def __init__(self, user: User):
        self.user = user
        self.network = pylast.LastFMNetwork(api_key=API_KEY, api_secret=API_SECRET)

    @classmethod
    def _map_artist(cls, entry: TopItem) -> Artist:
        return Artist(name=entry.item.name)
    
    def get_artists(self, api_call_result: tuple) -> Generator[Artist, None, None]:
        for row in api_call_result:
            artist = LastFmScraper._map_artist(row)
            yield artist
            self.user.artists.append(artist)

    def scrape(self, limit: int):
        lastfm_user = self.network.get_user(self.user.lastfm_username)
        return lastfm_user.get_top_artists(limit=limit, period='PERIOD_OVERALL')


# start = time.time()
#
# scraper = LastFmScraper(User('nothing@nowhere.com', 'Aiwa-Lee'))
# result = scraper.scrape(1000)
# artists = [artist for artist in LastFmScraper.get_artists(result)]
#
# end = time.time()
# print(f'Time elapsed {end - start} seconds')
#
# print(artists)
