from releaseraccoon.base import session
from releaseraccoon.model.user import User
from releaseraccoon.scraper.lastfm_scraper import LastFmScraper

LASTFM_USERNAME = 'Aiwa-Lee'

if __name__ == '__main__':
    user = User('nothing', LASTFM_USERNAME)
    lastfm_scraper = LastFmScraper(user)
    lastfm_scraper.scrape(10)

    session.add(user)
    session.commit()
