import logging
import sys
from pprint import pprint

import spotipy
from spotipy.oauth2 import SpotifyClientCredentials

from zope.interface import implementer

from releaseraccoon import settings
from releaseraccoon.scraper.scraper import (
    IMusicReleaseScraper,
    IMusicTasteScraper,
    RELEASE_ARTISTS_KEY,
    RELEASE_TYPE_KEY,
    RELEASE_NAME_KEY,
    RELEASE_DATE_KEY,
    RELEASE_SPOTIFY_URI_KEY,
    RELEASE_ARTIST_NAME_KEY,
    RELEASE_ARTIST_SPOTIFY_URI_KEY
)

SPOTIFY_CLIENT_ID = settings.spotify_client_id
SPOTIFY_CLIENT_SECRET = settings.spotify_client_secret

SPOTIFY_KEY_FILTER = [
    RELEASE_NAME_KEY,
    RELEASE_TYPE_KEY,
    RELEASE_DATE_KEY,
    RELEASE_SPOTIFY_URI_KEY,
]

SPOTIFY_ARTIST_KEY_FILTER = [
    RELEASE_ARTIST_NAME_KEY,
    RELEASE_ARTIST_SPOTIFY_URI_KEY
]

LOG = logging.getLogger(__name__)


@implementer(IMusicTasteScraper)
@implementer(IMusicReleaseScraper)
class SpotifyScraper:

    def __init__(self):
        client_credentials_manager = SpotifyClientCredentials(client_id=SPOTIFY_CLIENT_ID,
                                                              client_secret=SPOTIFY_CLIENT_SECRET)
        self.sp = spotipy.Spotify(client_credentials_manager=client_credentials_manager)
        self.release_types = ['album']
        
    def scrape_releases(self, limit: int = None) -> list:
        """
        Returns a list containing dicts as returned by _process_release.
        :param limit: The max items to return, defaults to None.
        :rtype: list
        :return:
        """
        ret = []

        # Hit spotify
        sp_response = self.sp.new_releases()
        while sp_response:
            albums = sp_response['albums']
            for _, item in enumerate(albums['items']):
                keeper = SpotifyScraper._process_release(item)
                if keeper:
                    ret.append(keeper)

                if limit is not None and len(ret) == limit:
                    return ret

            # The sp_response is paginated
            if albums['next']:
                sp_response = self.sp.next(albums)
            else:
                sp_response = None
        return ret

    @classmethod
    def _process_release(cls, item: dict) -> dict:
        """
        Filters a spotify result dict on the fields that interest us.

        Results in a dict looking like this:
             {'album_type': 'single',
              'artists': [{'name': 'ar1',
                           'uri': 'spotify:artist:<HashID>'}],
              'name': '<ReleaseName]',
              'release_date': 'YYYY-MM-dd',
              'uri': 'spotify:album:<HashId>'}
        :param item: spotify originating dict
        :return: a dict
        """
        ret = {}
        try:
            ret = {k: item[k] for k in SPOTIFY_KEY_FILTER}

            ret[RELEASE_ARTISTS_KEY] = []
            for artist in item['artists']:
                ret[RELEASE_ARTISTS_KEY].append({k: artist[k] for k in SPOTIFY_ARTIST_KEY_FILTER})

            LOG.debug(f'Unpacked release: {ret}')
        except KeyError:
            LOG.warning(f'Exception when processing release {item}', exc_info=True)
        return ret


if __name__ == '__main__':
    log_format = '%(asctime)s %(levelname)s %(name)s | %(message)s'
    logging.basicConfig(stream=sys.stdout, level=logging.DEBUG, format=log_format)

    scraped = SpotifyScraper().scrape_releases()
    LOG.info(f'Scraped {len(scraped)} releases: {pprint(scraped)}')

