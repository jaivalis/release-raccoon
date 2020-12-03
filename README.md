# release-raccoon
[![Python 3.6](https://img.shields.io/badge/python-3.8-blue.svg)](https://www.python.org/downloads/release/python-380/)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/9f7b06a4a03b447f8d09c1f5324db079)](https://app.codacy.com/gh/jaivalis/release-raccoon?branch=develop?utm_source=github.com&utm_medium=referral&utm_content=jaivalis/release-raccoon&utm_campaign=Badge_Grade) 
[![CircleCI Docs Status](https://circleci.com/gh/jaivalis/release-raccoon/tree/develop.svg?style=svg)](https://circleci.com/gh/jaivalis/release-raccoon/tree/develop)
## Installation

We recommend to use virtualenv for development:

If you don't have it, start by installing it

```bash 
pip install virtualenv
```

Once installed, create a virtual environment
```bash 
source venv/bin/activate
```

Next, install the python dependencies on the virtual environment
```bash
pip install -r requirements.txt
```

## Configuration

### Environment variables
All env vars from `.env.template` need to be defined in order for the scripts to run.
You can copy that file into a new `.env` file and define them there.

After defining the env vars source the file so they are available for starting the database.

## Deploying locally

### Starting the database
For the database docker compose is used
```bash
docker-compose up
```

The entrypoint currently is a REST application that can be started by running `main.py`