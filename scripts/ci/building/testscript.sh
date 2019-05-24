#!/bin/bash

# openssl aes-256-cbc -K $encrypted_0e6b9eee7eaa_key -iv $encrypted_0e6b9eee7eaa_iv -in travis_deploy_github.enc -out travis_deploy_github -d

git config remote.origin.fetch +refs/heads/*:refs/remotes/origin/*
git fetch --all
git add build.gradle
git commit -m "this is a test for the deployment key"
git push
git checkout develop
git status
git merge feature/travisDeployKey
git commit -m "merge feature branch"
git push
