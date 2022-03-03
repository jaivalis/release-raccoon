# ReleaseRaccoon
---

![Github Actions build](https://github.com/jaivalis/release-raccoon/actions/workflows/build.yml/badge.svg)
[![Known Vulnerabilities](https://snyk.io/test/github/jaivalis/release-raccoon/badge.svg)](https://snyk.io/test/github/jaivalis/release-raccoon)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=jaivalis_release-raccoon&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=jaivalis_release-raccoon)

A music release newsletter application built with quarkus.

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

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container
using:

```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
./mvnw package -Pnative -pl release-raccoon-app
```

You can then execute your native executable with: `./target/release.com.raccoon-0.0.1-SNAPSHOT-runner`

If you want to learn more about building native executables, please
consult https://quarkus.io/guides/maven-tooling.html.

# Starting the db and keycloak using the .env file
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

# Deploying the image to heroku

Build the project to get the jar
```shell
./mvnw package -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true
```
> This needs to be run from the project root otherwise the following property (pointing to the resource dir) needs to be configured accordingly in [application.properties](release-raccoon-app/src/main/resources/application.properties):
> -H:IncludeResources='${PWD}/release-raccoon-app/src/main/resources/META-INF/resources/.*'

(Login to heroku docker registry if you haven't already), build the docker image, push it to the heroku repository and deploy to heroku:
```shell
heroku container:login
cp build/release-raccoon-app-0.0.1-SNAPSHOT-runner ./docker && pushd docker && docker build -f Dockerfile.native -t registry.heroku.com/release-raccoon/web .
docker push registry.heroku.com/release-raccoon/web
heroku container:release web --app release-raccoon && popd
```

Check the logs for a successful start.
```shell
heroku logs --app release-raccoon --tail
```
