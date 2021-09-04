# ReleaseRaccoon

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev -pl release-raccoon-ap
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

# RESTEasy JAX-RS

<p>A Hello World RESTEasy resource</p>

Guide: https://quarkus.io/guides/rest-json

# Starting the db using the .env file
From the root of the project run:
```shell script
source release-raccoon/.env
docker-compose --env-file ./release-raccoon-app/.env -f docker/docker-compose.yml up
``` 

## Setting up Keycloak
Access the [Keycloak Admin Console](http://127.0.0.1:${KEYCLOAK_PORT}/auth/admin) login with the password used by the docker-compose.yml.
Import the [realm](resources/realm-export.json) to create a realm named `RaccoonRealm`.

In case the redirect back from keycloak doesn't work in dev mode, you might need to regenerate a `quarkus.oidc.credentials.secret` and plug it into the `application.properties` file.

## Deploying the image to heroku (manually)

Build the project to get the jar
```shell
./mvnw package -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true
```

Build the docker image, push it to the heroku repository and deploy to heroku:
```shell
cp build/release-raccoon-app-0.0.1-SNAPSHOT-runner && pushd docker && docker build -f Dockerfile.native -t registry.heroku.com/release-raccoon/web .
docker push registry.heroku.com/release-raccoon/web
heroku container:release web --app release-raccoon && popd
```

Check the logs for a successful start.
```shell
heroku logs --app release-raccoon
```
