import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.ThresholdFilter
import grails.util.BuildSettings

import static ch.qos.logback.classic.Level.*

def appenders = []
// See http://logback.qos.ch/manual/groovy.html for details on configuration
def targetDir = BuildSettings.TARGET_DIR

def catalinaBase = System.properties.getProperty('catalina.base')
if (!catalinaBase) catalinaBase = '.'   // just in case
def logFolder = "${catalinaBase}/logs"

def logToFile = Boolean.getBoolean('logToFile')
def logHibernateDetails = Boolean.getBoolean('logHibernateDetails')
def defaultLevel = System.properties.getProperty('logLevel')

Level thresholdLevel = INFO

if(defaultLevel) {
    try {
        thresholdLevel = Level.valueOf(defaultLevel)
    }
    catch (Exception e) {
        e.printStackTrace()
    }
}

initAppenders(appenders, thresholdLevel, logFolder)

def consoleLog = ["CONSOLE"]
def fileLog = ["osmAppender", "asyncOsmAppenderDetails"]
def detailLog = ["asyncOsmAppenderDetails"]
def hibernateStats = ["osmHibernateStatsAppender"]

def defaultLogConfig = [
        ["de.iteratec.osm", ALL],
        ["de.iteratec.osm.da", ALL],
        ["grails.app", ERROR],
        ["org.grails.commons", ERROR],
        ["org.grails.web.mapping", ERROR],
        ["org.grails.web.mapping.filter", ERROR],
        ["org.grails.web.pages", ERROR],
        ["org.grails.web.servlet", ERROR],
        ["org.grails.web.sitemesh", ERROR],
        ["org.grails.plugins", ERROR],
        ["org.springframework", ERROR],
        ["net.sf.ehcache.hibernate", ERROR],
        ["org.grails.orm.hibernate", ERROR],
        ["org.hibernate.SQL", ERROR],
        ["org.hibernate.transaction", ERROR],
        ["org.hibernate", WARN],
        ["org.grails.datastore.gorm", ERROR]
]

def consoleLogConfig = [
    (consoleLog) : [
            *defaultLogConfig,
            ["liquibase", INFO]]
]
applyLoggers(consoleLogConfig)

if (logToFile && targetDir) {
    def fileLogConfig = [
        (fileLog) : [*defaultLogConfig],
        (detailLog) : [
                ["liquibase", ALL],
                ["com.p6spy", ALL]]
    ]
    applyLoggers(fileLogConfig)

    def hibernateLogConfig = [
        (hibernateStats) : [
                ['grails.app.controllers.org.grails.plugins.LogHibernateStatsInterceptor', DEBUG],
                ['org.hibernate.stat', DEBUG]]
    ]
    if(logHibernateDetails) {
        applyLoggers(hibernateLogConfig)
    }
}

root(INFO, appenders)

def applyLoggers(Map config) {
    config.keySet().forEach{key ->
        config[key].forEach{val ->
            logger(val[0], val[1], key, false);
        }
    }
}

def initAppenders(List appenders, Level thresholdLevel, String logFolder) {
    appender('CONSOLE', ConsoleAppender) {
        encoder(PatternLayoutEncoder) {
            pattern = "%logger %m%n"
        }
        filter(ThresholdFilter) {
            level = thresholdLevel
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
            level = thresholdLevel
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

    appender("osmHibernateStatsAppender", RollingFileAppender) {
        file = "${logFolder}/OpenSpeedMonitorHibernateStats.log"
        append = true
        rollingPolicy(TimeBasedRollingPolicy) {
            FileNamePattern = "logs/OpenSpeedMonitorHibernateStats-%d{yyyy-MM-dd}.zip"
        }
        encoder(PatternLayoutEncoder) {
            pattern = "[%d{dd.MM.yyyy HH:mm:ss,SSS}] [THREAD ID=%t] %-5p %logger : %m%n"
        }
        filter(ThresholdFilter) {
            level = DEBUG
        }
    }
    appenders << "osmHibernateStatsAppender"
}
