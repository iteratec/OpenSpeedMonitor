import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy
import grails.util.BuildSettings
import grails.util.Environment

import static ch.qos.logback.classic.Level.*

def appenders = []
// See http://logback.qos.ch/manual/groovy.html for details on configuration
def targetDir = BuildSettings.TARGET_DIR

if (Environment.getCurrent() == Environment.PRODUCTION && targetDir) {
    def catalinaBase = System.properties.getProperty('catalina.base')
    if (!catalinaBase) catalinaBase = '.'   // just in case
    def logFolder = "${catalinaBase}/logs/"
    appender('CONSOLE', ConsoleAppender) {
        encoder(PatternLayoutEncoder) {
            pattern = "%logger %m%n"
        }
        filter(ThresholdFilter) {
            level = ERROR
        }
    }
    appenders << "CONSOLE"

    appender("osmAppender", RollingFileAppender) {
        file = "${logFolder}/OpenSpeedMonitor.log"
        append = true
        rollingPolicy(TimeBasedRollingPolicy) {
            FileNamePattern = "${logFolder}/OpenSpeedMonitor-%d{yyyy-MM-dd}.zip"
        }

        encoder(PatternLayoutEncoder) {
            pattern = "[%d{dd.MM.yyyy HH:mm:ss,SSS}] [THREAD ID=%t] %-5p %logger : %m%n"
        }
        filter(ThresholdFilter) {
            level = ERROR
        }

    }
    appenders << "osmAppender"

    appender("osmAppenderDetails", RollingFileAppender) {
        file = "${logFolder}/OpenSpeedMonitorDetails.log"
        append = true
        rollingPolicy(FixedWindowRollingPolicy ) {
            FileNamePattern = "${logFolder}/OpenSpeedMonitorDetails%i.log.zip"
            minIndex = 1
            maxIndex = 20
        }
        triggeringPolicy(SizeBasedTriggeringPolicy){
            maxFileSize= '20MB'
        }

        encoder(PatternLayoutEncoder) {
            pattern = "[%d{dd.MM.yyyy HH:mm:ss,SSS}] [THREAD ID=%t] %-5p %logger : %m%n"
        }
        filter(ThresholdFilter) {
            level = DEBUG
        }
    }
    appender('asyncOsmAppenderDetails', AsyncAppender){
        discardingThreshold=0
        appenderRef('osmAppenderDetails')

    }
    appenders << "asyncOsmAppenderDetails"

    // our packages
    logger("grails.app.controllers.de.iteratec.osm", ALL,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("grails.app.services.de.iteratec.osm", ALL,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("grails.app.domain.de.iteratec.osm", ALL,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("grails.app.filters.de.iteratec.osm", ALL,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("grails.app.conf.de.iteratec.osm", ALL,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("grails.app.taglib.de.iteratec.osm", ALL,["osmAppender", "asyncOsmAppenderDetails"], false)
    // other packages
    logger("liquibase", ALL,["asyncOsmAppenderDetails"], false)
    logger("grails.app", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("org.grails.commons", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("org.grails.web.mapping", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("org.grails.web.mapping.filter", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("org.grails.web.pages", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    //      GSP
    logger("org.grails.web.servlet", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    //      controllers
    logger("org.grails.web.sitemesh", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    //      plugins
    logger("org.grails.plugins'", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("org.springframework", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("net.sf.ehcache.hibernate", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("org.grails.orm.hibernate", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("org.hibernate.SQL", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("org.hibernate.transaction", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
}

if (Environment.isDevelopmentMode() && targetDir) {
    appender('CONSOLE', ConsoleAppender) {
        encoder(PatternLayoutEncoder) {
            pattern = "%logger %m%n"
        }
        filter(ThresholdFilter) {
            level = WARN
        }
    }
    appenders << "CONSOLE"



    appender("osmAppender", RollingFileAppender) {
        file = "logs/OpenSpeedMonitor.log"
        append = true
        rollingPolicy(TimeBasedRollingPolicy) {
            FileNamePattern = "logs/OpenSpeedMonitor-%d{yyyy-MM-dd}.zip"
        }

        encoder(PatternLayoutEncoder) {
            pattern = "[%d{dd.MM.yyyy HH:mm:ss,SSS}] [THREAD ID=%t] %-5p %logger : %m%n"
        }
        filter(ThresholdFilter) {
            level = ERROR
        }

    }
    appenders << "osmAppender"


    appender("osmAppenderDetails", RollingFileAppender) {
        file = "logs/OpenSpeedMonitorDetails.log"
        append = true
        rollingPolicy(FixedWindowRollingPolicy ) {
            FileNamePattern = "logs/OpenSpeedMonitorDetails%i.log.zip"
            minIndex = 1
            maxIndex = 20
        }
        triggeringPolicy(SizeBasedTriggeringPolicy){
            maxFileSize= '20MB'
        }

        encoder(PatternLayoutEncoder) {
            pattern = "[%d{dd.MM.yyyy HH:mm:ss,SSS}] [THREAD ID=%t] %-5p %logger : %m%n"
        }
        filter(ThresholdFilter) {
            level = DEBUG
        }
    }
    appender('asyncOsmAppenderDetails', AsyncAppender){
        discardingThreshold=0
        appenderRef('osmAppenderDetails')

    }
    appenders << "asyncOsmAppenderDetails"

    // our packages
    logger("grails.app.controllers.de.iteratec.osm", ALL,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("grails.app.services.de.iteratec.osm", ALL,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("grails.app.domain.de.iteratec.osm", ALL,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("grails.app.filters.de.iteratec.osm", ALL,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("grails.app.conf.de.iteratec.osm", ALL,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("grails.app.taglib.de.iteratec.osm", ALL,["osmAppender", "asyncOsmAppenderDetails"], false)
    // other packages
    logger("liquibase", ALL,["asyncOsmAppenderDetails"], false)
    logger("grails.app", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("org.grails.commons", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("org.grails.web.mapping", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("org.grails.web.mapping.filter", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("org.grails.web.pages", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    //      GSP
    logger("org.grails.web.servlet", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    //      controllers
    logger("org.grails.web.sitemesh", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    //      plugins
    logger("org.grails.plugins'", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("org.springframework", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("net.sf.ehcache.hibernate", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("org.grails.orm.hibernate", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("org.hibernate.SQL", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
    logger("org.hibernate.transaction", ERROR,["osmAppender", "asyncOsmAppenderDetails"], false)
}
if (Environment.getCurrent() == Environment.TEST && targetDir) {
    appender('CONSOLE', ConsoleAppender) {
        encoder(PatternLayoutEncoder) {
            pattern = "%logger %m%n"
        }
        filter(ThresholdFilter) {
            level = ERROR
        }
    }
    appenders << "CONSOLE"

    logger("grails.app", INFO)
    logger("org.grails.commons",INFO)
    logger("org.grails.web.mapping",INFO)
    logger("org.grails.web.mapping.filter", INFO)
    logger("org.grails.web.pages", INFO)
    logger("org.grails.web.servlet", INFO)
    logger("org.grails.web.servlet",INFO)
    logger("org.grails.web.sitemesh", INFO)
    logger("org.grails.plugins'", INFO)
    logger("org.springframework", INFO)
    logger("net.sf.ehcache.hibernate", INFO)
    logger("org.grails.orm.hibernate", INFO)
    logger("org.hibernate.SQL", INFO)


    root(DEBUG, appenders)
}
