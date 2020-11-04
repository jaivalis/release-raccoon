from unittest import main, TestCase
from releaseraccoon.model.user import User
from unittest.mock import patch


class LastFmScraperTest(TestCase):

    def setUp(self):
        self.USER = User('mail@mail.com', 'mail_fm')
        
    @patch('pylast.LastFMNetwork', autospec=True)
    @patch("releaseraccoon.model.user.User")
    def test_get_artists(self, lastfm_network, user):
        # TODO add some logic
        self.assertTrue(True)


if __name__ == '__main__':
    main()
