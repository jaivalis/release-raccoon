import pylast

import settings


API_KEY = settings.lastfm_api_key
API_SECRET = settings.lastfm_shared_secret

network = pylast.LastFMNetwork(api_key=API_KEY, api_secret=API_SECRET)

user = network.get_user('Aiwa-Lee')

artists = user.get_top_artists(limit=100)
print(artists)
print(len(artists))
print(artists[0])

