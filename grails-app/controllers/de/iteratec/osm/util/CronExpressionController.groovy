package de.iteratec.osm.util

import de.iteratec.osm.annotations.RestAction
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat
import org.quartz.CronExpression
import org.springframework.http.HttpStatus

class CronExpressionController extends ExceptionHandlerController {

    @RestAction
    def nextExecutionTime(String cronExpression) {
        if (!cronExpression) {
            String errorMessage = message(code: "cron.expression.empty", default: "Empty cron expression").toString()
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST, errorMessage)
            return
        }
        cronExpression = "0 " + cronExpression
        try {
            CronExpression parsedExpression = new CronExpression(cronExpression)
            Date nextExecution = parsedExpression.getNextValidTimeAfter(new Date())
            String isoString = ISODateTimeFormat.dateTimeNoMillis().print(new DateTime(nextExecution.getTime(), DateTimeZone.UTC))
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.OK, isoString)
        } catch (ignored) {
            String errorMessage = message(code: "cron.expression.invalid", default: "Invalid cron expression").toString()
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST, errorMessage)
        }
    }
}
