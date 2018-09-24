#!/bin/bash

git config remote.origin.fetch +refs/heads/*:refs/remotes/origin/*
git fetch --all
git checkout develop
git merge release
git push https://iteraspeed:$GITHUB_USER_TOKEN@github.com/iteratec/OpenSpeedMonitor.git
git checkout master
git merge release
git push https://iteraspeed:$GITHUB_USER_TOKEN@github.com/iteratec/OpenSpeedMonitor.git