#!/usr/bin/env bash

#################################################################################################
# Builds the docker image and deploys it on heroku						#
# Pre-steps:											#
#	- executable *-runner binary is present in build/					#
#	- logged in to heroku:									#
#		heroku login									#
#	- logged in to heroku docker registry:							#
#		docker login --username=_ --password=$(heroku auth:token) registry.heroku.com	#
# Execute this script from the root of the project:						#
#												#
# $> ./build/build-and-deploy.sh								#
#################################################################################################

# exit when any command fails
set -e

# print all commands
set -x

EXECUTABLE_TARGET_DIR="./docker/raccoon"

mkdir -p ${EXECUTABLE_TARGET_DIR}
echo "Copying new executable to: ${EXECUTABLE_TARGET_DIR}..."
cp ./build/release-raccoon-app-0.0.1-SNAPSHOT-runner ${EXECUTABLE_TARGET_DIR}

pushd docker

echo "Building image"
docker build -f Dockerfile.native -t registry.heroku.com/release-raccoon/web .
echo "Publishing image"
docker push registry.heroku.com/release-raccoon/web
echo "Deploying new executable"
heroku container:release web --app release-raccoon

popd

echo "Done"
