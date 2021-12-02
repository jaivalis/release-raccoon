#!/usr/bin/env bash

# exit when any command fails
set -e

# print all commands
set -x

echo "Copying new executable..."
cp /Users/jaivalis/Workspace/os/jaivalis/release-raccoon/build/release-raccoon-app-0.0.1-SNAPSHOT-runner ./docker/raccoon

pushd docker

echo "Building image"
docker build -f Dockerfile.native -t registry.heroku.com/release-raccoon/web .
echo "Publishing image"
docker push registry.heroku.com/release-raccoon/web
echo "Deploying new executable"
heroku container:release web --app release-raccoon

popd

echo "Done"
