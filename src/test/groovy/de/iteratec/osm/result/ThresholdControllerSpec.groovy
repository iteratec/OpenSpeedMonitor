package de.iteratec.osm.result

import grails.test.mixin.*
import spock.lang.*

@TestFor(ThresholdController)
@Mock(Threshold)
class ThresholdControllerSpec extends Specification {

    def populateValidParams(params) {
        assert params != null

        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
        assert false, "TODO: Provide a populateValidParams() implementation for this generated test suite"
    }

    void "Test the index action returns the correct model"() {

        when:"The index action is executed"
            controller.index()

        then:"The model is correct"
            !model.thresholdList
            model.thresholdCount == 0
    }

    void "Test the create action returns the correct model"() {
        when:"The create action is executed"
            controller.create()

        then:"The model is correctly created"
            model.threshold!= null
    }

    void "Test the save action correctly persists an instance"() {

        when:"The save action is executed with an invalid instance"
            request.contentType = FORM_CONTENT_TYPE
            request.method = 'POST'
            def threshold = new Threshold()
            threshold.validate()
            controller.save(threshold)

        then:"The create view is rendered again with the correct model"
            model.threshold!= null
            view == 'create'

        when:"The save action is executed with a valid instance"
            response.reset()
            populateValidParams(params)
            threshold = new Threshold(params)

            controller.save(threshold)

        then:"A redirect is issued to the show action"
            response.redirectedUrl == '/threshold/show/1'
            controller.flash.message != null
            Threshold.count() == 1
    }

    void "Test that the show action returns the correct model"() {
        when:"The show action is executed with a null domain"
            controller.show(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the show action"
            populateValidParams(params)
            def threshold = new Threshold(params)
            controller.show(threshold)

        then:"A model is populated containing the domain instance"
            model.threshold == threshold
    }

    void "Test that the edit action returns the correct model"() {
        when:"The edit action is executed with a null domain"
            controller.edit(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the edit action"
            populateValidParams(params)
            def threshold = new Threshold(params)
            controller.edit(threshold)

        then:"A model is populated containing the domain instance"
            model.threshold == threshold
    }

    void "Test the update action performs an update on a valid domain instance"() {
        when:"Update is called for a domain instance that doesn't exist"
            request.contentType = FORM_CONTENT_TYPE
            request.method = 'PUT'
            controller.update(null)

        then:"A 404 error is returned"
            response.redirectedUrl == '/threshold/index'
            flash.message != null

        when:"An invalid domain instance is passed to the update action"
            response.reset()
            def threshold = new Threshold()
            threshold.validate()
            controller.update(threshold)

        then:"The edit view is rendered again with the invalid instance"
            view == 'edit'
            model.threshold == threshold

        when:"A valid domain instance is passed to the update action"
            response.reset()
            populateValidParams(params)
            threshold = new Threshold(params).save(flush: true)
            controller.update(threshold)

        then:"A redirect is issued to the show action"
            threshold != null
            response.redirectedUrl == "/threshold/show/$threshold.id"
            flash.message != null
    }

    void "Test that the delete action deletes an instance if it exists"() {
        when:"The delete action is called for a null instance"
            request.contentType = FORM_CONTENT_TYPE
            request.method = 'DELETE'
            controller.delete(null)

        then:"A 404 is returned"
            response.redirectedUrl == '/threshold/index'
            flash.message != null

        when:"A domain instance is created"
            response.reset()
            populateValidParams(params)
            def threshold = new Threshold(params).save(flush: true)

        then:"It exists"
            Threshold.count() == 1

        when:"The domain instance is passed to the delete action"
            controller.delete(threshold)

        then:"The instance is deleted"
            Threshold.count() == 0
            response.redirectedUrl == '/threshold/index'
            flash.message != null
    }
}
