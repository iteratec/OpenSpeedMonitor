//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// set new osm version in application.properties
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

Properties props = new Properties()
File propsFile = new File('../../application.properties')
props.load(propsFile.newDataInputStream())

String appVersion = props.getProperty('app.version')
println "AppVersion from application.properties is ${appVersion}"

List tokenizedVersion = appVersion.tokenize('.')
Integer major = Integer.valueOf(tokenizedVersion[0])
Integer minor = Integer.valueOf(tokenizedVersion[1])

String lastVersionPart = tokenizedVersion[2]
List tokenizedLastVersionPart = tokenizedVersion[2].tokenize('-')
Integer patch = Integer.valueOf(tokenizedLastVersionPart[0])

String oldVersion = "${major}.${minor}.${patch}"
println "OldVersion is ${oldVersion}"

def bamboo_jira_version = System.getenv("bamboo_jira_version") ?: ""
String newVersion = ""
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

props.setProperty('app.version', newVersion)
props = props.sort()
props.store(propsFile.newWriter(), null)

println "Updated version from ${oldVersion} to ${newVersion}"
