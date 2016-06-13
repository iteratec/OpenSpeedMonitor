#!/bin/bash
set -e

if [ -z $bamboo_ci_app_version ]; then
  echo "No version-number found: ${bamboo_ci_app_version}"
  exit 1
else
  echo "Found version-number: ${bamboo_ci_app_version}"

  remote=origin

  git remote remove $remote

  remote_url=https://$bamboo_git_USER_NAME:$bamboo_git_PASSWORD@github.com/IteraSpeed/OpenSpeedMonitor.git
  echo "set remote $remote to '$remote_url'"
  git remote add -f $remote $remote_url

  git config user.email "wpt@iteratec.de"
  git config user.name "bamboo iteratec"

  # the following commit message is referenced by regex in bamboo to exclude these commits
  # while picking up changes (configured in bamboo repositories advanced settings)
  git commit -am "[${bamboo_ci_app_version}] version update"

  git pull --rebase $remote release
  if [ -z $bamboo_jira_version ]; then
    git push $remote HEAD:refs/heads/$bamboo_repository_git_branch
  else
    git tag "${bamboo_ci_app_version}"
    git push --tags $remote HEAD:refs/heads/release
  fi

fi