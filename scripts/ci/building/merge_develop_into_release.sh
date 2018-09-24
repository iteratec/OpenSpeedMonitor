#!/bin/bash

echo "prepare some variables"
echo "########################################'"
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
echo "git fetch release"
git config remote.origin.fetch +refs/heads/*:refs/remotes/origin/*
git fetch --all
echo "git checkout release"
git checkout release
echo "git merge develop"
git merge develop