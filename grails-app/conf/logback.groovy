import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.RollingFileAppender
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
            pattern = "%c{2} %m%n"
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
            pattern = "[%d{dd.MM.yyyy HH:mm:ss,SSS}] [THREAD ID=%t] %-5p %c{2} (line %L): %m%n"
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
            minIndex = 10
        }
        triggeringPolicy(SizeBasedTriggeringPolicy){
            maxFileSize= '20MB'
        }

        encoder(PatternLayoutEncoder) {
            pattern = "[%d{dd.MM.yyyy HH:mm:ss,SSS}] [THREAD ID=%t] %-5p %c{2} (line %L): %m%n"
        }
        filter(ThresholdFilter) {
            level = DEBUG
        }
    }
//    appenders << "osmAppenderDetails"

    appender('asyncOsmAppenderDetails', AsyncAppender){
        discardingThreshold=0
        appenderRef('osmAppenderDetails')

    }
    appenders << "asyncOsmAppenderDetails"
    logger("grails.app", ALL,["osmAppender"])
    logger("grails.app", ALL,["asyncOsmAppenderDetails"])
    logger("liquibase", ALL,["asyncOsmAppenderDetails"])
    logger("org.grails.commons", ERROR,["osmAppender"])
    logger("org.grails.web.mapping", ERROR,["osmAppender"])
    logger("org.grails.web.mapping.filter", ERROR,["osmAppender"])
    logger("org.grails.web.pages", ERROR,["osmAppender"])
    logger("org.grails.web.servlet", ERROR,["osmAppender"])
    //GSP
    logger("org.grails.web.servlet", ERROR,["osmAppender"])
    //controllers
    logger("org.grails.web.sitemesh", ERROR,["osmAppender"])
    //plugins
    logger("org.grails.plugins'", ERROR,["osmAppender"])
    logger("org.springframework", ERROR,["osmAppender"])
    logger("net.sf.ehcache.hibernate", ERROR,["osmAppender"])
    logger("org.grails.orm.hibernate", ERROR,["osmAppender"])
    logger("org.hibernate.SQL", ERROR,["osmAppender"])
    logger("org.hibernate.transaction", ERROR,["osmAppender"])


    logger("org.grails.commons", ERROR,["asyncOsmAppenderDetails"])
    logger("org.grails.web.mapping", ERROR,["asyncOsmAppenderDetails"])
    logger("org.grails.web.mapping.filter", ERROR,["asyncOsmAppenderDetails"])
    logger("org.grails.web.pages", ERROR,["asyncOsmAppenderDetails"])
    logger("org.grails.web.servlet", ERROR,["asyncOsmAppenderDetails"])
    logger("org.grails.web.sitemesh", ERROR,["asyncOsmAppenderDetails"])
    logger("org.grails.plugins", ERROR,["asyncOsmAppenderDetails"])
    logger("org.springframework", ERROR,["asyncOsmAppenderDetails"])
    logger("net.sf.ehcache.hibernate", ERROR,["asyncOsmAppenderDetails"])
    logger("org.grails.orm.hibernate", ERROR,["asyncOsmAppenderDetails"])
    logger("org.hibernate.SQL", ERROR,["asyncOsmAppenderDetails"])
    logger("org.hibernate.transaction", ERROR,["asyncOsmAppenderDetails"])
    root(DEBUG, appenders)
}

if (Environment.isDevelopmentMode() && targetDir) {
    appender('CONSOLE', ConsoleAppender) {
        encoder(PatternLayoutEncoder) {
            pattern = "%c{2} %m%n"
        }
        filter(ThresholdFilter) {
            level = ERROR
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
            pattern = "[%d{dd.MM.yyyy HH:mm:ss,SSS}] [THREAD ID=%t] %-5p %c{2} : %m%n"
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
            minIndex = 10
        }
        triggeringPolicy(SizeBasedTriggeringPolicy){
            maxFileSize= '20MB'
        }

        encoder(PatternLayoutEncoder) {
            pattern = "[%d{dd.MM.yyyy HH:mm:ss,SSS}] [THREAD ID=%t] %-5p %c{2} : %m%n"
        }
        filter(ThresholdFilter) {
            level = DEBUG
        }
    }
//    appenders << "osmAppenderDetails"

    appender('asyncOsmAppenderDetails', AsyncAppender){
        discardingThreshold=0
        appenderRef('osmAppenderDetails')

    }
    appenders << "asyncOsmAppenderDetails"
    logger("grails.app", ALL,["osmAppender"])
    logger("grails.app", ALL,["asyncOsmAppenderDetails"])
    logger("liquibase", ALL,["asyncOsmAppenderDetails"])
    logger("org.grails.commons", ERROR,["osmAppender"])
    logger("org.grails.web.mapping", ERROR,["osmAppender"])
    logger("org.grails.web.mapping.filter", ERROR,["osmAppender"])
    logger("org.grails.web.pages", ERROR,["osmAppender"])
    logger("org.grails.web.servlet", ERROR,["osmAppender"])
    //GSP
    logger("org.grails.web.servlet", ERROR,["osmAppender"])
    //controllers
    logger("org.grails.web.sitemesh", ERROR,["osmAppender"])
    //plugins
    logger("org.grails.plugins'", ERROR,["osmAppender"])
    logger("org.springframework", ERROR,["osmAppender"])
    logger("net.sf.ehcache.hibernate", ERROR,["osmAppender"])
    logger("org.grails.orm.hibernate", ERROR,["osmAppender"])
    logger("org.hibernate.SQL", ERROR,["osmAppender"])
    logger("org.hibernate.transaction", ERROR,["osmAppender"])


    logger("org.grails.commons", ERROR,["asyncOsmAppenderDetails"])
    logger("org.grails.web.mapping", ERROR,["asyncOsmAppenderDetails"])
    logger("org.grails.web.mapping.filter", ERROR,["asyncOsmAppenderDetails"])
    logger("org.grails.web.pages", ERROR,["asyncOsmAppenderDetails"])
    logger("org.grails.web.servlet", ERROR,["asyncOsmAppenderDetails"])
    logger("org.grails.web.sitemesh", ERROR,["asyncOsmAppenderDetails"])
    logger("org.grails.plugins", ERROR,["asyncOsmAppenderDetails"])
    logger("org.springframework", ERROR,["asyncOsmAppenderDetails"])
    logger("net.sf.ehcache.hibernate", ERROR,["asyncOsmAppenderDetails"])
    logger("org.grails.orm.hibernate", ERROR,["asyncOsmAppenderDetails"])
    logger("org.hibernate.SQL", ERROR,["asyncOsmAppenderDetails"])
    logger("org.hibernate.transaction", ERROR,["asyncOsmAppenderDetails"])
    root(DEBUG, appenders)
}
if (Environment.getCurrent() == Environment.TEST && targetDir) {
    appender('CONSOLE', ConsoleAppender) {
        encoder(PatternLayoutEncoder) {
            pattern = "%c{2} %m%n"
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