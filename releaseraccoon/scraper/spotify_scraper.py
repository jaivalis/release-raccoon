import spotipy
from spotipy.oauth2 import SpotifyClientCredentials

from zope.interface import implementer

from releaseraccoon import settings
from releaseraccoon.scraper.scraper import IMusicReleaseScraper, IMusicTasteScraper
from datetime import date

SPOTIFY_CLIENT_ID = settings.spotify_client_id
SPOTIFY_CLIENT_SECRET = settings.spotify_client_secret


@implementer(IMusicTasteScraper)
@implementer(IMusicReleaseScraper)
class SpotifyScraper:

    def __init__(self):
        client_credentials_manager = SpotifyClientCredentials(client_id=SPOTIFY_CLIENT_ID,
                                                              client_secret=SPOTIFY_CLIENT_SECRET)
        self.sp = spotipy.Spotify(client_credentials_manager=client_credentials_manager)
        self.release_types = ['album']
        
    def scrape_releases(self, cal_date: date, limit: int):
        response = self.sp.new_releases()
    
        while response:
            albums = response['albums']
            for i, item in enumerate(albums['items']):
                if self.skip_release_item(item):
                    continue
                artists_str = ''
                for artist in item['artists']:
                    artists_str += artist['name'] + ' '
                print(albums['offset'] + i, item['type'], item['name'], artists_str)
        
            if albums['next']:
                response = self.sp.next(albums)
            else:
                response = None
                
    def skip_release_item(self, item: dict):
        return item['album_type'] in self.release_types
                
                
if __name__ == '__main__':
    scraper = SpotifyScraper()
    scraper.scrape_releases(None, 0)

