import logging
import sys

from flask import Flask, jsonify, request
from werkzeug.exceptions import abort

from releaseraccoon.service import handle_register_user, get_all_users
from releaseraccoon.settings import db_host, db_name, db_password, db_port, db_username

db_uri = f'mysql+pymysql://{db_username}:{db_password}@{db_host}:{db_port}/{db_name}'
LOG = logging.getLogger(__name__)


app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = db_uri


@app.route('/')
@app.route('/index')
def index():
    users = get_all_users()
    return jsonify(users)


@app.route('/register', methods=['POST'])
def register_user():
    req_data = request.get_json(force=True)
    
    email = req_data['email']
    lastfm_username = req_data['lastfm_username']
    LOG.debug(f'email: {email} lastfm_user: {lastfm_username}')
    
    response = handle_register_user(email, lastfm_username)
    LOG.debug(response)
    if response:
        return jsonify(response)
    else:
        abort(404, {'message': 'Oops, something went wrong'})


if __name__ == '__main__':
    log_format = '%(asctime)s %(levelname)s %(name)s | %(message)s'
    logging.basicConfig(stream=sys.stdout, level=logging.DEBUG, format=log_format)

    app.run(host='0.0.0.0', port=8888, debug=True)
