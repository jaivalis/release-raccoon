#!/usr/bin/env bash

#################################################################################################
# Deploys a given version image on heroku                                                       #
# Pre-steps:                                                                                    #
#	- logged in to heroku:                                                                        #
#	      heroku login                                                                            #
#	- logged in to heroku docker registry:                                                        #
#	      docker login --username=_ --password=$(heroku auth:token) registry.heroku.com           #
# Execute this script from the root of the project:                                             #
#	                                                                                              #
# $> ./build/deploy-native-dockerhub.sh                                                         #
#################################################################################################

# exit when any command fails
set -e

export LATEST_TAG=0.4.1

echo "Pulling ${LATEST_TAG} from dockerhub"
docker pull jaivalis/release-raccoon:"${LATEST_TAG}"-native

echo "Re-tagging image ${LATEST_TAG}-native..."
docker tag jaivalis/release-raccoon:"${LATEST_TAG}"-native registry.heroku.com/backend-release-raccoon/web

echo "Pushing to registry.heroku.com/backend-release-raccoon/web"
docker push registry.heroku.com/backend-release-raccoon/web

echo "Deploying new executable"
heroku container:release web --app backend-release-raccoon

echo "Done"
