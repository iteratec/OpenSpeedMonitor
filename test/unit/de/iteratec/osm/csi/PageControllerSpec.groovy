package de.iteratec.osm.csi



import grails.test.mixin.*
import spock.lang.*

@TestFor(PageController)
@Mock(Page)
class PageControllerSpec extends Specification {

    def populateValidParams(params) {
        assert params != null
        // TODO: Populate valid properties like...
        //params["name"] = 'someValidName'
    }

    void "Test the save action correctly persists an instance"() {

        when:"The save action is executed with an invalid instance"
            request.contentType = FORM_CONTENT_TYPE
            def page = new Page()
            page.validate()
            controller.save(page)

        then:"The create view is rendered again with the correct model"
            model.pageInstance!= null
            view == 'create'

        when:"The save action is executed with a valid instance"
            response.reset()
            populateValidParams(params)
            page = new Page(params)

            controller.save(page)

        then:"A redirect is issued to the show action"
            response.redirectedUrl == '/page/show/1'
            controller.flash.message != null
            Page.count() == 1
    }

    void "Test that the show action returns the correct model"() {
        when:"The show action is executed with a null domain"
            controller.show(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the show action"
            populateValidParams(params)
            def page = new Page(params)
            controller.show(page)

        then:"A model is populated containing the domain instance"
            model.pageInstance == page
    }

    void "Test that the edit action returns the correct model"() {
        when:"The edit action is executed with a null domain"
            controller.edit(null)

        then:"A 404 error is returned"
            response.status == 404

        when:"A domain instance is passed to the edit action"
            populateValidParams(params)
            def page = new Page(params)
            controller.edit(page)

        then:"A model is populated containing the domain instance"
            model.pageInstance == page
    }

    void "Test the update action performs an update on a valid domain instance"() {
        when:"Update is called for a domain instance that doesn't exist"
            request.contentType = FORM_CONTENT_TYPE
            controller.update(null)

        then:"A 404 error is returned"
            response.redirectedUrl == '/page/index'
            flash.message != null


        when:"An invalid domain instance is passed to the update action"
            response.reset()
            def page = new Page()
            page.validate()
            controller.update(page)

        then:"The edit view is rendered again with the invalid instance"
            view == 'edit'
            model.pageInstance == page

        when:"A valid domain instance is passed to the update action"
            response.reset()
            populateValidParams(params)
            page = new Page(params).save(flush: true)
            controller.update(page)

        then:"A redirect is issues to the show action"
            response.redirectedUrl == "/page/show/$page.id"
            flash.message != null
    }

    void "Test that the delete action deletes an instance if it exists"() {
        when:"The delete action is called for a null instance"
            request.contentType = FORM_CONTENT_TYPE
            controller.delete(null)

        then:"A 404 is returned"
            response.redirectedUrl == '/page/index'
            flash.message != null

        when:"A domain instance is created"
            response.reset()
            populateValidParams(params)
            def page = new Page(params).save(flush: true)

        then:"It exists"
            Page.count() == 1

        when:"The domain instance is passed to the delete action"
            controller.delete(page)

        then:"The instance is deleted"
            Page.count() == 0
            response.redirectedUrl == '/page/index'
            flash.message != null
    }
}
