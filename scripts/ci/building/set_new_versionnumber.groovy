//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// set new osm version in application.properties
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
import hudson.model.*
File buildFile = new File('./build.gradle')

String appVersion = getVersionFromFile(buildFile)
println "AppVersion from application.properties is ${appVersion}"

List tokenizedVersion = appVersion.tokenize('.')
Integer major = Integer.valueOf(tokenizedVersion[0])
Integer minor = Integer.valueOf(tokenizedVersion[1])
Integer patch = Integer.valueOf(tokenizedVersion[2])


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
if( !System.getenv("bamboo_planRepository_branchName").equals("release") ){
    newVersion += "-SNAPSHOT"
}
writeVersionToFile(buildFile,newVersion)

File versionPropertiesFile = new File('./version.properties')
String propertiesToWrite = "app.version=${newVersion}"
versionPropertiesFile.write(propertiesToWrite)

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
  def pattern = ~/version "\d\.\d\.\d\"\n/

  file.withWriter { w ->
    w << text.replaceAll(pattern, writeToFile)
  }
}