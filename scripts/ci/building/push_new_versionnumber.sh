#!/bin/bash

set -e

if [ -z $bamboo_ci_app_version ]; then
  echo "No version-number found: ${bamboo_ci_app_version}"
  exit 1
else
  GIT_SSH_COMMAND="ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no"
  echo "Found version-number: ${bamboo_ci_app_version}"

  git config user.email "jenkins@seu.hh.iteratec.de"
  git config user.name "jenkins@seu"
  git config push.default simple

  git add --all :/
  git commit -m "[${bamboo_ci_app_version}] version update"

  git status
  git pull --rebase
  git push

  git tag $bamboo_ci_app_version
  git pull --rebase
  git push --tags


  # git remote add github $bamboo_planRepository_repositoryUrl
  # GIT_SSH_COMMAND="ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no" git pull --rebase github $bamboo_planRepository_branch
  # GIT_SSH_COMMAND="ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no" git push github $bamboo_planRepository_branch
fi
