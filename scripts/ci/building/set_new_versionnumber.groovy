//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// set new osm version in build.gradle (and temporary version.properties for bamboo build)
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

class GradleBuildFile {

    File buildFile

    String getVersion() {
        String result
        String line
        int n = buildFile.length()
        buildFile.withReader { r ->
            while (n-- > 0) {
                line = r.readLine()
                if (line.startsWith("version ")) {
                    break
                }
            }

        }
        result = line.replace("version ", "")
        result = result.replaceAll('\"', "")

        if (result == null || result.empty) {
            println "Found String is: " + result
            println "Could not find version-number in build.gradle"
            System.exit(1)
        }
        return result
    }

    void setVersion(String versionNumber) {
        String writeToFile = '\nversion "' + versionNumber + '"\n'
        def text = buildFile.text
        def pattern = ~/\nversion "\d\.\d\.\d[_beta]*\"\n/
        buildFile.withWriter { w ->
            w << text.replaceAll(pattern, writeToFile)
        }
    }
}

class VersionProperties{

    File propertiesFile = new File('./version.properties')
    StringBuilder versionProperties = new StringBuilder()

    void appendLine(String lineToAppend){
        versionProperties.append("${lineToAppend}\n")
    }
    void write(){
        propertiesFile.write(versionProperties.toString())
    }
}

String getNewVersion(GradleBuildFile buildFile){

    String oldVersion = buildFile.getVersion()
    println "Old AppVersion from build file is ${oldVersion}"

    def bamboo_jira_version = System.getenv("bamboo_jira_version") ?: ""
    def bamboo_jira_version_manually = System.getenv("bamboo_jira_version_manually") ?: ""
    String newVersion
    if ( !bamboo_jira_version && !bamboo_jira_version_manually ) {
        println 'None release-version given from jira...'
        newVersion = "${oldVersion}"
    }
    if (bamboo_jira_version) {
        println 'Given release-version from jira: ' + bamboo_jira_version + '...'
        newVersion = bamboo_jira_version
    }
    if (bamboo_jira_version_manually) {
        println 'Given release-version from manual run: ' + bamboo_jira_version_manually + '...'
        newVersion = bamboo_jira_version_manually
    }

    return newVersion

}

public void appendBuildNumber(newVersion) {

    def bamboo_build_number = System.getenv("bamboo_buildNumber")
    println "... and adding build-number ${bamboo_build_number} to version-number"

    newVersion += "-build${bamboo_build_number}"
    if (!System.getenv("bamboo_planRepository_branchName").equals("release")) {
        newVersion += "-SNAPSHOT"
    }

}

String getMajorVersionFrom(String version){
    return version.tokenize('.')[0]
}
String getMinorVersionFrom(String version){
    return version.tokenize('.')[1]
}
String getPatchVersionFrom(String version){
    return version.tokenize('.')[2].tokenize('_')[0]
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// script:
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

GradleBuildFile buildFile = new GradleBuildFile(buildFile: new File('./build.gradle'))
VersionProperties versionProperties = new VersionProperties()

String newVersion = getNewVersion(buildFile)

versionProperties.appendLine("app.version.major=${getMajorVersionFrom(newVersion)}")
versionProperties.appendLine("app.version.minor=${getMajorVersionFrom(newVersion)}.${getMinorVersionFrom(newVersion)}")
versionProperties.appendLine("app.version.patch=${getMajorVersionFrom(newVersion)}.${getMinorVersionFrom(newVersion)}.${getPatchVersionFrom(newVersion)}")

String oldVersion = buildFile.getVersion()
appendBuildNumber(newVersion)
versionProperties.appendLine("app.version=${newVersion}")

buildFile.setVersion(newVersion)
versionProperties.write()

println "Updated version from ${oldVersion} to ${newVersion}"