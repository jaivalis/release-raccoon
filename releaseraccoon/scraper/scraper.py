from zope.interface import Interface

RELEASE_NAME_KEY = 'name'
RELEASE_TYPE_KEY = 'album_type'
RELEASE_DATE_KEY = 'release_date'
RELEASE_SPOTIFY_URI_KEY = 'uri'
RELEASE_ARTISTS_KEY = 'artists'

RELEASE_ARTIST_NAME_KEY = 'name'
RELEASE_ARTIST_SPOTIFY_URI_KEY = 'uri'


class IMusicReleaseScraper(Interface):

    """ Scrapes external sources for music releases """
    
    def scrape_releases(limit: int = None):
        """
        Returns a list containing dicts as returned by _process_release.
        :param limit: The max items to return, defaults to None.
        :rtype: list
        :return:
        """

    def _process_release(cls, item: dict) -> dict:
        """
        Filters a returned result unit (release) on the fields that interest us.

        :param item: an item as returned from the API call.
        :return: a dict including only the fields of interest.
        """


class IMusicTasteScraper(Interface):
    """ Scrapes external sources for music taste of a given user """

    def scrape_taste(user_name: str, limit: int):
        """ Gets top artists. """

    def _map_artist(cls, entry: object) -> tuple:
        """ Maps an entry row originating from the API to an artist [dict, weight] tuple. """
