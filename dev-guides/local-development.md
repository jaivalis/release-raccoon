# Local Development

## Starting the db, keycloak and Elastic using the .env file
Create a .env file containing all the environment variables that are referred to from the [docker-compose](docker/docker-compose.yml) file.
Place that file under [release-raccoon-app](release-raccoon-app).

> **NOTE:**  The images in docker-compose.yml are currently set for development on arm CPUs, commented you will find the amd64 counterparts.

From the root of the project run:
```shell script
source release-raccoon-app/.env
docker compose --env-file ./release-raccoon-app/.env -f docker/docker-compose.yml up -d
```

## Setting up Keycloak
Access the [Keycloak Admin Console](http://127.0.0.1:${KEYCLOAK_PORT}/auth/admin) login with the password used by the [docker-compose](docker/docker-compose.yml).
The realm we will be using is `RaccoonRealm` and is defined in [realm](docker/keycloak_init/realm-export.json).
It is mounted to the container on start time.
You will need to create a new user in order to connect.
You also need to create a secret (Clients > release-raccoon > Credentials > Regenerate Secret) which you will need to pass as `quarkus.oidc.credentials.secret` to the [properties](release-raccoon-app/src/main/resources/application.properties).
You should be good to start the quarkus app.

> In case after a docker restart the redirect back from keycloak stops working in dev mode, you might need to regenerate a `quarkus.oidc.credentials.secret` and plug it into the [properties](release-raccoon-app/src/main/resources/application.properties) again.


## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev -pl release-raccoon-app
```

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `release.raccoon-0.0.1-SNAPSHOT-runner.jar` file in the `/target` directory. Be
aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar target/release.raccoon-0.0.1-SNAPSHOT-runner.jar`.


## Testing

For testing we rely on testcontainers:

```shell
sudo ln -s $HOME/.docker/run/docker.sock /var/run/docker.sock
```