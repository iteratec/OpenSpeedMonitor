#!/bin/bash

echo "prepare some variables"
echo "bamboo_osm_branch_to_merge=$bamboo_osm_branch_to_merge"
echo "########################################'"
echo "start bamboo job to merge $bamboo_osm_branch_to_merge into release"
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
git tag -d $(git tag -l)
git fetch --all
echo "git checkout release"
git checkout release

target_branch="develop"

if [[ "$bamboo_osm_branch_to_merge" != "develop" ]]; then
    target_branch="origin/$bamboo_osm_branch_to_merge"
fi

echo "git merge $target_branch"
git merge $target_branch
