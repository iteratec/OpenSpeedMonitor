#!/bin/bash

echo "prepare some variables"
echo "########################################'"
echo "bamboo_planRepository_branchName=$bamboo_planRepository_branchName"

if [ "${bamboo_planRepository_branchName}" = "release" ]; then

  echo "start bamboo job to merge develop into release"
  echo "########################################'"
  remote=origin

  echo "git remote remove $remote"
  git remote remove $remote

  remote_url=https://$bamboo_git_USER_NAME:$bamboo_git_PASSWORD@github.com/iteratec/OpenSpeedMonitor.git
  echo "set remote $remote to '$remote_url'"
  git remote add -f $remote $remote_url

  git config user.email 'osm@iteratec.de'
  git config user.name 'bamboo iteratec'

  echo "########################################'"
  echo "git merge develop"
  git merge develop
else
  echo 'Wrong branch. Committing only into the release branch.'
fi