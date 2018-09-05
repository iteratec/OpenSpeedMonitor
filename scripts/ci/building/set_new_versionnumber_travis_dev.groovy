//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// set new osm version in build.gradle (and temporary version.properties for travis build)
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
        def pattern = ~/\nversion "\d\.\d\.\d((_beta){1}(_[\d]+)?)?\"\n/
        buildFile.withWriter { w ->
            w << text.replaceAll(pattern, writeToFile)
        }
    }
}

class VersionProperties {

    File propertiesFile = new File('./version.properties')
    StringBuilder versionProperties = new StringBuilder()

    void appendLine(String lineToAppend) {
        versionProperties.append("${lineToAppend}\n")
    }

    void write() {
        propertiesFile.write(versionProperties.toString())
    }
}

String getNewVersion(GradleBuildFile buildFile) {
    String oldVersion = buildFile.getVersion()
    println "Old AppVersion from build file is ${oldVersion}"

    println 'None release-version given from jira...'
    newVersion = "${oldVersion}"

    return newVersion
}

public String appendBuildNumber(String newVersion) {
    def travis_build_number = System.getenv("TRAVIS_BUILD_NUMBER")
    println "... and adding build-number ${travis_build_number} to version-number"

    newVersion += "-build${travis_build_number}"
    newVersion += "-SNAPSHOT"
    return newVersion
}

String getMajorVersionFrom(String version) {
    return version.tokenize('.')[0]
}

String getMinorVersionFrom(String version) {
    return version.tokenize('.')[1]
}

String getPatchVersionFrom(String version) {
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
newVersion = appendBuildNumber(newVersion)
versionProperties.appendLine("app.version=${newVersion}")

buildFile.setVersion(newVersion)
versionProperties.write()

println "Updated version from ${oldVersion} to ${newVersion}"
