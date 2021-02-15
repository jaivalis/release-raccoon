import logging
import sys

from releaseraccoon.app.app import app, db
# noinspection PyUnresolvedReferences
from releaseraccoon.app.views import (get_all_artists, get_all_users, notify_users, register_user,
                                      update_artist_releases)


def create_tables():
    # Create table for each model if it does not exist.
    db.init_app(app)
    db.drop_all()  # Use alembic or something similar for db migrations
    db.create_all()


if __name__ == '__main__':
    create_tables()
    app.run()

    log_format = '%(asctime)s %(levelname)s %(name)s | %(message)s'
    logging.basicConfig(stream=sys.stdout, level=logging.DEBUG, format=log_format)

    app.run(host='0.0.0.0', port=9898, debug=True,)
