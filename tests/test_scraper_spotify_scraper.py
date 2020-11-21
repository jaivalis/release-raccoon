from unittest import TestCase
from releaseraccoon.scraper.scraper import (
    RELEASE_ARTISTS_KEY,
    RELEASE_TYPE_KEY,
    RELEASE_NAME_KEY,
    RELEASE_DATE_KEY,
    RELEASE_SPOTIFY_URI_KEY
)
from releaseraccoon.scraper.spotify_scraper import SpotifyScraper

sp_dict = {
    RELEASE_NAME_KEY: '',
    RELEASE_TYPE_KEY: '',
    RELEASE_DATE_KEY: '',
    RELEASE_SPOTIFY_URI_KEY: '',
    RELEASE_ARTISTS_KEY: ''
}


class TestSpotifyScraper(TestCase):

    def test_process_release(self):
        filtered = ''

        release_dict = sp_dict.copy()
        release_dict[filtered] = ''

        res = SpotifyScraper._process_release(release_dict)

        self.assertTrue(filtered not in res.keys())
        self.assertEqual(len(sp_dict), len(res.keys()),
                         'Should not include filtered value.')
