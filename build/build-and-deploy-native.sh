#!/usr/bin/env bash

#############################################################################
# Builds the docker image and deploys it on heroku                          #
# Pre-step: an executable *-runner binary needs to be present in build      #
# Run this script from the root of the project:                             #
#                                                                           #
# $> ./build/build-and-deploy.sh                                            #
#############################################################################

# exit when any command fails
set -e

# print all commands
set -x

echo "Copying new executable..."
cp ./build/release-raccoon-app-0.0.1-SNAPSHOT-runner ./docker/raccoon

pushd docker

echo "Building image"
docker build -f Dockerfile.native -t registry.heroku.com/release-raccoon/web .
echo "Publishing image"
docker push registry.heroku.com/release-raccoon/web
echo "Deploying new executable"
heroku container:release web --app release-raccoon

popd

echo "Done"
