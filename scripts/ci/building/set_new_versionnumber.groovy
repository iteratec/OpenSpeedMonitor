//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// set new osm version in build.gradle (and temporary version.properties for bamboo build)
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

class GradleBuildFile {

    File buildFile

    public String getVersion() {
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

    public void setVersion(String versionNumber) {
        String writeToFile = 'version "' + versionNumber + '"\n'
        def text = buildFile.text
        def pattern = ~/^version "\d\.\d\.\d\[_beta]*"\n/
        buildFile.withWriter { w ->
            w << text.replaceAll(pattern, writeToFile)
        }
    }
}

GradleBuildFile buildFile = new GradleBuildFile(buildFile: new File('./build.gradle'))

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
def bamboo_build_number = System.getenv("bamboo_buildNumber")
println "... and adding build-number ${bamboo_build_number} to version-number"

newVersion += "-build${bamboo_build_number}"
if (!System.getenv("bamboo_planRepository_branchName").equals("release")) {
    newVersion += "-SNAPSHOT"
}
buildFile.setVersion(newVersion)

File versionPropertiesFile = new File('./version.properties')
String propertiesToWrite = "app.version=${newVersion}"
versionPropertiesFile.write(propertiesToWrite)

println "Updated version from ${oldVersion} to ${newVersion}"