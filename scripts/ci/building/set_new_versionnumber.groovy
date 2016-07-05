//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// set new osm version in application.properties
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

File buildFile = new File('./build.gradle')

String appVersion = getVersionFromFile(buildFile)
println "AppVersion from application.properties is ${appVersion}"

List tokenizedVersion = appVersion.tokenize('.')
Integer major = Integer.valueOf(tokenizedVersion[0])
Integer minor = Integer.valueOf(tokenizedVersion[1])

String lastVersionPart = tokenizedVersion[2]
List tokenizedLastVersionPart = lastVersionPart.tokenize('-')
Integer patch = Integer.valueOf(tokenizedLastVersionPart[0])

String oldVersion = "${major}.${minor}.${patch}"
println "OldVersion is ${oldVersion}"

def bamboo_jira_version = System.getenv("bamboo_jira_version") ?: ""
String newVersion
if("${bamboo_jira_version}") {
  println 'Given release-version from jira: ' + bamboo_jira_version + '...'
  newVersion = "${bamboo_jira_version}"
} else {
  println 'None release-version given from jira...'
  newVersion = "${major}.${minor}.${patch}"
}
def bamboo_build_number = System.getenv("bamboo_buildNumber")
println "... and adding build-number ${bamboo_build_number} to version-number"

newVersion += "-build${bamboo_build_number}"

writeVersionToFile(buildFile,newVersion)

export BAMBOO_CI_APP_VERSION=newVersion

println "Updated version from ${oldVersion} to ${newVersion}"


def String getVersionFromFile(File file) {
  String result
  String line
  int n = file.length()
  file.withReader { r ->
    while( n-- > 0) {
      line = r.readLine()
      if(line.startsWith("version ")) {
        break
      }
    }

  }
  result = line.replace("version ","")
  result = result.replaceAll('\"',"")

  if(result == null || result.empty) {
    println "Found String is: " + result
    println "Could not find version-number in build.gradle"
    System.exit(1)
  }

  return result
}

def String writeVersionToFile(File file, String versionNumber) {
  String writeToFile = 'version "' + versionNumber + '"\n'
  def text = file.text
  def pattern = ~/version "\d\.\d\.\d\-build(\d+)"\n/

  file.withWriter { w ->
    w << text.replaceAll(pattern, writeToFile)
  }
}