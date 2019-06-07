#!/bin/bash

git config --add remote.origin.fetch +refs/heads/*:refs/remotes/origin/*
git fetch --all
git remote set-url origin git@github.com:iteratec/OpenSpeedMonitor.git
git remote -v
git add .
git commit -m "Committing package-lock.json and build.gradle of release"
git push
git checkout develop
git merge release
git push
git checkout master
git merge release
git push