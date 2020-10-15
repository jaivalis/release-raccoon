import os
from os.path import dirname, join

from dotenv import load_dotenv

# Create .env file path.
dotenv_path = join(dirname(__file__), '../.env')

# Load file from the path.
load_dotenv(dotenv_path)

# db
db_username = os.getenv('DB_USERNAME')
db_password = os.getenv('DB_PASSWORD')
db_name = os.getenv('DB_NAME')
db_host = os.getenv('DB_HOST')
db_port = os.getenv('DB_PORT')

# lastfm
lastfm_api_key = os.getenv('LASTFM_API_KEY')
lastfm_application_name = os.getenv('LASTFM_APPLICATION_NAME')
lastfm_shared_secret = os.getenv('LASTFM_SHARED_SECRET')
