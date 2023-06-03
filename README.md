# ReleaseRaccoon
---
![Github Actions build](https://github.com/jaivalis/release-raccoon/actions/workflows/build-branches.yml/badge.svg)
[![Known Vulnerabilities](https://snyk.io/test/github/jaivalis/release-raccoon/badge.svg)](https://snyk.io/test/github/jaivalis/release-raccoon)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=jaivalis_release-raccoon&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=jaivalis_release-raccoon)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=jaivalis_release-raccoon&metric=coverage)](https://sonarcloud.io/summary/new_code?id=jaivalis_release-raccoon)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=jaivalis_release-raccoon&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=jaivalis_release-raccoon)

A music release newsletter application powered by quarkus.

## Local development
See [guide](dev-guides/local-development.md).

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
