package de.iteratec.osm.frontendBuild

import groovy.io.FileType
import groovy.json.JsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class Grails2FrontendI18nTask extends DefaultTask {

    final Map<String,String> grails2FrontendBundles = [
            "messages.properties": "en.json",
            "messages_de.properties": "de.json"
    ]
    String pathGrailsI18nFolder = "./grails-app/i18n/"
    String pathFrontendI18nFolder = "./src/main/resources/public/i18n/"
    String frontendKeyPrefix = "frontend."

    @TaskAction
    void generate() {

        clearFrontendI18n()

        new File(pathGrailsI18nFolder).eachFileMatch(FileType.FILES, ~/.*.properties/) { File grailsMsgBundleFile ->

            String frontendBundleName = grails2FrontendBundles[grailsMsgBundleFile.getName()]
            if (frontendBundleName){
                Properties grailsMsgBundle = new Properties()
                grailsMsgBundle.load(grailsMsgBundleFile.newReader("UTF-8"))
                File frontendBundle = new File(pathFrontendI18nFolder + frontendBundleName)
                frontendBundle << new JsonBuilder(grailsMsgBundle.findAll {it.key.startsWith(frontendKeyPrefix)})
                        .toPrettyString()
            }

        }
    }
    private clearFrontendI18n(){
        File frontendI18nFolder = new File(pathFrontendI18nFolder)
        frontendI18nFolder.eachFileMatch(FileType.FILES, ~/.*.json/){File frontendBundle ->
            frontendBundle.delete()
        }
    }
}