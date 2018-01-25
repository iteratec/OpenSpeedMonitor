package de.iteratec.osm.result

import de.iteratec.osm.measurement.schedule.Job
import de.iteratec.osm.util.ControllerUtils
import org.springframework.boot.autoconfigure.batch.BatchProperties
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus

import static org.springframework.http.HttpStatus.*
//TODO: This controller-templated was edited due to a scaffolding bug (https://github.com/grails3-plugins/scaffolding/issues/24). The dynamically scaffolded controllers cannot handle database exceptions

class ThresholdController {

    static scaffold = Threshold
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    /**
     * Creates a new threshold and saves the threshold in the database.
     *
     * @return The id of the new threshold.
     */
    def create() {
        Job job = Job.findById(Long.parseLong(params['job']))
        Measurand measurand = Measurand.valueOf(params['measurand']);
        MeasuredEvent measuredEvent =  MeasuredEvent.findById(Long.parseLong(params['measuredEvent']))
        Integer lowerBoundary = Integer.parseInt(params['lowerBoundary'])
        Integer upperBoundary = Integer.parseInt(params['upperBoundary'])

        Threshold threshold = new Threshold(job: job, measurand: measurand, measuredEvent: measuredEvent, lowerBoundary: lowerBoundary, upperBoundary: upperBoundary)

        if (!threshold.save(flush: true)) {
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST, "Fehler")
        } else {
            threshold.id = (threshold.save(flush: true)).id
            ControllerUtils.sendObjectAsJSON(response, ['thresholdId': threshold.id])
        }
    }

    /**
     * Deletes the threshold.
     *
     * @return http status
     */
    def deleteThreshold() {
        Long id = Long.parseLong(params['thresholdId'])
        Threshold threshold = Threshold.findById(id)
        if (threshold == null) {
            notFound()
            return
        }

        try {
            threshold.delete(flush: true)
            ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.OK, "")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'threshold.label', default: 'Threshold'), params.id])
            redirect(action: "show", id: params.id)
        }
    }

    /**
     * Updates the submitted threshold.
     *
     * @return The id of the threshold.
     */
    def update() {
        //get params
        Long id = Long.parseLong(params['thresholdId'])
        Measurand measurand = params['measurand'];
        MeasuredEvent measuredEvent =  MeasuredEvent.findById(Long.parseLong(params['measuredEvent']))
        Integer lowerBoundary = Integer.parseInt(params['lowerBoundary'])
        Integer upperBoundary = Integer.parseInt(params['upperBoundary'])


        //get threshold
        Threshold threshold = Threshold.findById(id)

        //update threshold
        threshold.measurand = measurand
        threshold.measuredEvent = measuredEvent
        threshold.lowerBoundary = lowerBoundary
        threshold.upperBoundary = upperBoundary

        //save changes
        if (!threshold.save(flush: true)) {
            //ControllerUtils.sendSimpleResponseAsStream(response, HttpStatus.BAD_REQUEST, jobGroup.errors.allErrors*.toString().toString())
        } else {
            threshold.id = (threshold.save(flush: true)).id
            ControllerUtils.sendObjectAsJSON(response, ['thresholdId': threshold.id])
        }
    }
}
