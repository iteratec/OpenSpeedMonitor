#!/bin/bash
set -e

echo "bamboo_jira_version=$bamboo_jira_version"
echo "bamboo_jira_version_manually=$bamboo_jira_version_manually"
echo "bamboo_planRepository_branchName=$bamboo_planRepository_branchName"

touch ./push_new_versionnumber_out

if [ -z $bamboo_jira_version ] && [ -z $bamboo_jira_version_manually ]; then
    echo 'No new version to commit and push since we are not pushing the build numbers anymore'
else
    if [ "${bamboo_planRepository_branchName}" = "release" ]; then
      echo "Found jira version-number in release branch: ${bamboo_jira_version}"

      # manually set jira version overwrites auto jira version
      if [ -n "$bamboo_jira_version" ]; then
        jira_version=$bamboo_jira_version
      fi
      if [ -n "$bamboo_jira_version_manually" ]; then
        jira_version=$bamboo_jira_version_manually
      fi
      echo "jira_version=$jira_version"

      remote=origin

      echo "git remote remove $remote"
      git remote remove $remote > ./push_new_versionnumber_out

      remote_url=https://$bamboo_git_USER_NAME:$bamboo_git_PASSWORD@github.com/iteratec/OpenSpeedMonitor.git
      echo "set remote $remote to '$remote_url'"
      git remote add -f $remote $remote_url >> ./push_new_versionnumber_out

      git config user.email 'osm@iteratec.de'
      git config user.name 'bamboo iteratec'

      # the following commit message is referenced by regex in bamboo to exclude these commits
      # while picking up changes (configured in bamboo repositories advanced settings)
      echo "git commit -am '[${jira_version}] version update'"
      git status
      git commit -am "[${jira_version}] version update" > ./push_new_versionnumber_out

      echo "pull --rebase $remote release"
      git pull --rebase $remote release >> ./push_new_versionnumber_out

      echo "git tag 'v${jira_version}'"
      git tag "v${jira_version}" >> ./push_new_versionnumber_out
      echo "git push --tags $remote HEAD:refs/heads/release"
      git push --tags $remote HEAD:refs/heads/release >> ./push_new_versionnumber_out

      echo "push_new_versionnumber_out:"
      cat ./push_new_versionnumber_out
    else
      echo 'Wrong branch. Committing only into the release branch.'
    fi
fi