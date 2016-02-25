def version_number = System.getenv("bamboo_ci_app_version") ?: ""

if(version_number) {
  println 'Found version-number: ' + version_number
  git add --all
  git commit -m "[$bamboo_buildResultKey] version update"
  git pull --rebase
  git push
  git log --pretty=format:'%H' -n 1 > ./latest-commit-hash.info
} else {
    println 'No version-number found: ' + version_number
}
