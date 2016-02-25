git add --all
git commit -m "[$bamboo.buildResultKey] version update"
git pull --rebase
git push
git log --pretty=format:'%H' -n 1 > ./latest-commit-hash.info
