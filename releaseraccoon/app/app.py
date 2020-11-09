"""
models imports app, but app does not import models so we haven't created
any loops.
"""
from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from releaseraccoon.settings import db_username, db_password, db_host, db_port, db_name

db_uri = f'mysql+pymysql://{db_username}:{db_password}@{db_host}:{db_port}/{db_name}'

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = db_uri
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False  # FSADeprecationWarning if not set
# app.config.from_object(config_object)

db = SQLAlchemy(app)

engine = create_engine(db_uri, pool_recycle=3600, echo=True)

Session = sessionmaker(bind=engine)
session = Session()
