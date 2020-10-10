import pprint
import time

import pylast

from releaseraccoon import settings
from releaseraccoon.model.artist import Artist

API_KEY = settings.lastfm_api_key
API_SECRET = settings.lastfm_shared_secret

network = pylast.LastFMNetwork(api_key=API_KEY, api_secret=API_SECRET)


def get_user_top_overall(user_name: str, limit: int):
    user = network.get_user(user_name)
    return user.get_top_artists(limit=limit, period='PERIOD_OVERALL')
    
    
def get_artists(api_call_result: tuple):
    for row in api_call_result:
        yield _map_artist(row)
    

def _map_artist(entry: object) -> Artist:
    return Artist(name=entry.item.name)


start = time.time()

result = get_user_top_overall('Aiwa-Lee', 1000)
artists = [artist for artist in get_artists(result)]

end = time.time()
print(f'Time elapsed {end - start} seconds')

print(artists)
print(len(artists))
pprint.pp(artists[0].__dict__)


