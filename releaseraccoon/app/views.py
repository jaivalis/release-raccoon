import logging

from flask import jsonify, request, redirect

from releaseraccoon.app.app import app
import releaseraccoon.service as service

LOG = logging.getLogger(__name__)


@app.route('/')
@app.route('/index')
@app.route('/users')
def get_all_users():
    return jsonify(service.get_all_users())


@app.route('/artists')
def get_all_artists():
    return jsonify(service.get_all_artists())


@app.route('/register', methods=['POST'])
def register_user():
    req_data = request.get_json(force=True)
    
    try:
        email = req_data['email']
        lastfm_username = req_data['lastfm_username']
    except KeyError:
        return redirect('/register/failed')
    
    LOG.debug(f'email: {email} lastfm_user: {lastfm_username}')
    
    response = service.handle_register_user(email, lastfm_username)
    LOG.debug(response)
    if response:
        return jsonify(response)
    else:
        # abort(404, {'message': 'Oops, something went wrong'})
        redirect('/register/failed')


@app.route('/register/failed')
def register_failed():
    # session.pop('spotify_user_id', None)
    return 'Failed to register new user.'

