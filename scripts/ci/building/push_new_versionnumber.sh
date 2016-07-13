#!/bin/bash
set -e

if [ -z $bamboo_jira_version ] && [ -z $jira_version_manually ]; then
    echo 'No new version to commit and push since we are not pushing the build numbers anymore'
else
    if [ "${bamboo_planRepository_branchName}" == "release" ]; then
      echo "Found jira version-number in release branch: ${bamboo_jira_version}"

      # manually set jira version overwrites auto jira version
      if [ -n "$bamboo_jira_version" ]; then
        jira_version=$bamboo_jira_version
      fi
      if [ -n "$jira_version_manually" ]; then
        jira_version=$jira_version_manually
      fi
      echo "jira_version=$jira_version"

      remote=origin

      git remote remove $remote

      remote_url=https://$bamboo_git_USER_NAME:$bamboo_git_PASSWORD@github.com/iteratec/OpenSpeedMonitor.git
      echo "set remote $remote to '$remote_url'"
      git remote add -f $remote $remote_url

      git config user.email 'osm@iteratec.de'
      git config user.name 'bamboo iteratec'

      # the following commit message is referenced by regex in bamboo to exclude these commits
      # while picking up changes (configured in bamboo repositories advanced settings)
      git commit -am "[${jira_version}] version update"

      git pull --rebase $remote release

      git tag "Release ${jira_version}"
      git push --tags $remote HEAD:refs/heads/release
    else
      echo 'Wrong branch. Committing only into the release branch.'
    fi
fi