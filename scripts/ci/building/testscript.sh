#!/bin/bash

git config --add remote.origin.fetch +refs/heads/*:refs/remotes/origin/*
git fetch --all
git checkout develop
git status
git merge feature/travisDeployKey
git remote set-url origin git@github.com:iteratec/OpenSpeedMonitor.git
git remote -v
git commit -m "merge feature branch"
git push
