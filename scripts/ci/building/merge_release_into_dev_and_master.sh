#!/bin/bash

git config remote.origin.fetch +refs/heads/*:refs/remotes/origin/*
git fetch --all
git checkout test/file
#git merge release
touch test
git add .
git commit -m "test push of travis"
git push