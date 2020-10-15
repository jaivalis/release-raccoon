## Installation

```pip install -r requirements.txt```

## Running

### Environment variables.
All env vars from `.env.template` need to be defined in order for the scripts to run.
You can copy that file into a new `.env` file and define them there.

After defining the env vars source the file so they are available for starting the database.

### Starting the database
For the db docker compoes us used like so:
```
docker-compose up
```

```
# Check if the following is necessary:
GRANT ALL PRIVILEGES ON releases.* TO `raccoon`@`localhost` IDENTIFIED BY `raccoon`;
FLUSH PRIVILEGES;
SHOW GRANTS FOR `raccoon`@localhost;
```

