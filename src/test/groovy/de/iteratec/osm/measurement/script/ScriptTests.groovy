package de.iteratec.osm.measurement.script

import spock.lang.Specification

class ScriptTests extends Specification{

    def "Test that scripts without parameter doesnt change on parse"(){
        given:
        String scriptContent = """setEventName aTest
                                  navigate http://testsite.de
                                  //a comment
                                  setEventName anotherTest
                                  navigate https://anothertestsite.de"""
        Script script = new Script(navigationScript: scriptContent)

        when:
        String parsedNavigationScript = script.getParsedNavigationScript([:])

        then:
        parsedNavigationScript == scriptContent
    }

    def "Test that scripts with parameter are parsed correclty"(){
        given:
        String scriptContent = """setEventName aTest
                                  navigate http://testsite.de/\${parameter}
                                  //a comment
                                  setEventName anotherTest
                                  navigate https://anothertestsite.de/\${anotherParameter"""
        Script script = new Script(navigationScript: scriptContent)

        when:
        String parsedNavigationScript = script.getParsedNavigationScript([parameter:"path","anotherParameter":"anotherPath"])

        then:
        parsedNavigationScript == scriptContent.replace("\${parameter}","path").replace("\${anotherParameter}","anotherPath")
    }
}
