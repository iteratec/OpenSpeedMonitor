/*
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* 	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import grails.util.Environment

grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.work.dir = "target"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.fork = [
    // configure settings for compilation JVM, note that if you alter the Groovy version forked compilation is required
    // compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
    // configure settings for the test-app JVM, uses the daemon by default
    test: [maxMemory: 2048, minMemory: 256, debug: false, maxPerm: 256, daemon:true],
    // configure settings for the run-app JVM
    run: [maxMemory: 2048, minMemory: 256, debug: false, maxPerm: 256, forkReserve:false],
    // configure settings for the run-war JVM
    war: [maxMemory: 2048, minMemory: 256, debug: false, maxPerm: 256, forkReserve:false],
    // configure settings for the Console UI JVM
    console: [maxMemory: 2048, minMemory: 256, debug: false, maxPerm: 256]
]
// in intellij a grails bug inhibits test runs in forked mode, so we disabled it
// see http://codedevstuff.blogspot.de/2014/03/run-forked-tests-in-grails-on-intellij.html
grails.project.fork = [
		test: false,
		run: false
]

//grails.project.dependency.resolver = "maven" // or ivy

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve

    repositories {
        inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenLocal()
        mavenCentral()
        flatDir name:'local jars', dirs:'./lib/'

        // uncomment these (or add new ones) to enable remote dependency resolution from public Maven repositories
        mavenRepo "http://repo.grails.org/grails/plugins/"
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
//      mavenRepo "http://seu.hh.iteratec.de:8082/artifactory/"
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.
		runtime(
			'mysql:mysql-connector-java:5.1.26',
			'net.sf.supercsv:super-csv:2.1.0'
		)
		compile (
			'org.mockito:mockito-all:1.9.5',
			'net.sf.supercsv:super-csv:2.1.0'
		)
		build (
			'net.sf.supercsv:super-csv:2.1.0'
		)
		 test (
			'org.mockito:mockito-all:1.9.5',
			'org.hamcrest:hamcrest-all:1.3',
			'co.freeside:betamax:1.1.2'
		)
    }

	plugins {
		//runtime//////////////////////////////////////////////////////////////////
		runtime (
			':twitter-bootstrap:2.3.2.2',
			":hibernate:3.6.10.10",
			":jquery:1.11.1",
			":database-migration:1.3.8",
			":quartz:1.0.1",
			":console:1.5.1",
		)
		//build//////////////////////////////////////////////////////////////////
		build (
			":tomcat:7.0.52.1",
			":release:3.0.1"
		)
        //compile//////////////////////////////////////////////////////////////////
		compile (
			":joda-time:1.5",
			':scaffolding:2.0.2',
			':cache:1.1.1',
			':spring-security-core:1.2.7.3',
			':quartz:1.0.1',
			':rest:0.8',
			':jmx:0.9',
			':taggable:1.0.1',
			':cookie:1.0.1',
            ':codenarc:0.22',
			":asset-pipeline:2.3.9",
			":less-asset-pipeline:2.3.0"
		)
		compile( ':jquery-ui:1.10.4'){
			excludes "jquery"
		}
//		provided ":less-asset-pipeline:2.3.0"
        //test//////////////////////////////////////////////////////////////////
        test ":code-coverage:2.0.3-3"
	}

	codenarc {
	    processTestUnit = false
	    processTestIntegration = false
	    propertiesFile = 'codenarc.properties'
	    ruleSetFiles = "file:grails-app/conf/CodeNarcRules.groovy"
	    reports = {
	        RedisReport('xml') {                    // The report name "MyXmlReport" is user-defined; Report type is 'xml'
	            outputFile = 'target/codenarc.xml'  // Set the 'outputFile' property of the (XML) Report
	            title = 'Grails Redis Plugin'             // Set the 'title' property of the (XML) Report
	        }
		}
	}

}
