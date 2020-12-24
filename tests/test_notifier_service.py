import unittest
# from releaseraccoon.model import User
# from unittest.mock import patch
from unittest.mock import MagicMock, patch
from alchemy_mock.mocking import UnifiedAlchemyMagicMock
from releaseraccoon.notifier_service import NotifierService


session = UnifiedAlchemyMagicMock()
session.query.return_value.all.return_value = [1, 2, 3]


class TestLastFmScraper(unittest.TestCase):

    def setUp(self):
        self.target = NotifierService(session)
    #
    # @patch('pylast.LastFMNetwork', autospec=True)
    # @patch("releaseraccoon.model.user.User")

    def test_notify_users(self):

        self.target.notify_users()
        self.assertTrue(True)


if __name__ == '__main__':
    unittest.main()
