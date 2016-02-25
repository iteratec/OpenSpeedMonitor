//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// set new osm version in application.properties
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

Properties props = new Properties()
File propsFile = new File('../application.properties')
props.load(propsFile.newDataInputStream())

String appVersion = props.getProperty('app.version')
List tokenizedVersion = appVersion.tokenize('.')
Integer major = Integer.valueOf(tokenizedVersion[0])
Integer minor = Integer.valueOf(tokenizedVersion[1])
Integer patch = Integer.valueOf(tokenizedVersion[2])
String oldVersion = "${major}.${minor}.${patch}"

String newVersion = ""
if("${bamboo.jira.version}") {
  newVersion = "${bamboo.jira.version}"
} else {
  patch++
  newVersion = "${major}.${minor}.${patch}"
}

props.setProperty('app.version', newVersion)
props = props.sort()
props.store(propsFile.newWriter(), null)

println "Updated version from ${oldVersion} to ${newVersion}"
