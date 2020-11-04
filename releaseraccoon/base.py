# coding=utf-8
from sqlalchemy import create_engine, MetaData
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
from sqlalchemy_utils import database_exists, create_database

from releaseraccoon.settings import db_username, db_password, db_host, db_port, db_name

Base = declarative_base()

db_uri = f'mysql+pymysql://{db_username}:{db_password}@{db_host}:{db_port}/{db_name}'
engine = create_engine(db_uri, pool_recycle=3600, echo=True)

Base.metadata.create_all(engine)

print(f'Looking for: {db_name} on {db_host}:{db_port}')
if not database_exists(engine.url):
    print(f'Database {db_name} not found, creating.')
    create_database(engine.url)

metadata = MetaData(engine)
metadata.create_all(engine)

print(f'Tables found {engine.table_names()}')
Session = sessionmaker(bind=engine)
session = Session()


def init_db():
    Base.metadata.create_all(bind=engine)

    print("Initialized the db")


init_db()

