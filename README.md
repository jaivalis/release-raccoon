# release-raccoon
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/9f7b06a4a03b447f8d09c1f5324db079)](https://app.codacy.com/gh/jaivalis/release-raccoon?branch=develop?utm_source=github.com&utm_medium=referral&utm_content=jaivalis/release-raccoon&utm_campaign=Badge_Grade) 

## Installation

```pip install -r requirements.txt```

## Running

### Environment variables
All env vars from `.env.template` need to be defined in order for the scripts to run.
You can copy that file into a new `.env` file and define them there.

After defining the env vars source the file so they are available for starting the database.

### Starting the database
For the database docker compose is used like so:
```bash
docker-compose up
```
