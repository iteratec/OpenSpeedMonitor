package geb.de.iteratec.osm.csi

import de.iteratec.osm.csi.Page
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.security.User
import geb.CustomUrlGebReportingSpec
import geb.IgnoreGebLiveTest
import geb.pages.de.iteratec.osm.csi.PageCreatePage
import geb.pages.de.iteratec.osm.csi.PageEditPage
import geb.pages.de.iteratec.osm.csi.PageIndexPage
import geb.pages.de.iteratec.osm.csi.PageShowPage
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.openqa.selenium.Keys
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Stepwise
/**
 * Tests the CRUD View of @link{de.iteratec.osm.measurement.environment.Page}*/
@Integration
@Rollback
@Stepwise
class PageGebSpec extends CustomUrlGebReportingSpec {

    public static final pageName = "a geb test page"

    @Shared
    int pageId

    void "test user gets to page list when logged in"() {
        given: "User is logged in"
        User.withNewTransaction {
            TestDataUtil.createOsmConfig()
            TestDataUtil.createAdminUser()
        }
        doLogin()

        when: "user tries to navigate to page controller"
        go "/page/index?lang=en"

        then: "user gets to page list page"
        at PageIndexPage
    }

    void "test createPage with invalid input shows error message"() {
        when: "user navigates to pageCreatePage"
        to PageCreatePage

        and: "does not fill all required fields"
        createPageButton.click()

        then: "an error message is shown on create page"
        at PageCreatePage
        errorMessageBox.isDisplayed()
        !errorMessageBoxText.isEmpty()
    }

    void "test createPage with valid input"() {
        when: "user navigates to pageCreatePage"
        to PageCreatePage

        and: "does fill form correctly"
        pageNameTextField << pageName
        createPageButton.click()
        // save page id for following tests
        pageId = Integer.parseInt(browser.getCurrentUrl().split("/").last())

        then: "user gets to show page of new created page"
        at PageShowPage

        and: "page id is shown in success div"
        successDiv.isDisplayed()
        successDivText.contains(pageId.toString())
    }

    void "test showPage shows correct data"() {
        when: "user navigates to detail page of a page"
        go "/page/show/" + pageId
        then: "the page data is shown"
        at PageShowPage
        name == pageName
    }

    void "test editPage with invalid data"() {
        when: "user edits page"
        go "/page/edit/" + pageId
        at PageEditPage

        and: "fills form incorrectly"
        nameTextField << Keys.chord(Keys.CONTROL, "a")
        nameTextField << Keys.chord(Keys.DELETE)
        saveButton.click()

        then: "error message is shown"
        errorMessageBox.isDisplayed()
        !errorMessageBoxText.isEmpty()
    }

    void "test editPage"() {
        when: "user navigates to edit page"
        go "/page/edit/" + pageId

        then: "form is prefilled"
        at PageEditPage
        nameTextField.value() == pageName

        when: "user inserts new name"
        String newPageName = "a new geb test page name"
        nameTextField << Keys.chord(Keys.CONTROL, "a")
        nameTextField << newPageName

        and: "clicks reset button"
        resetButton.click()

        then: "fields are resetted"
        nameTextField.value() == pageName

        when: "user edits page"
        nameTextField << Keys.chord(Keys.CONTROL, "a")
        nameTextField << newPageName
        saveButton.click()

        then: "user gets to details of edited page"
        at PageShowPage
        pageId == Integer.parseInt(browser.getCurrentUrl().split("/").last())
        successDiv.isDisplayed()
        successDivText.contains(pageId.toString())
        name == newPageName
    }

    @IgnoreIf(IgnoreGebLiveTest)
    void "test pagination"() {
        when: "there are more than 10 pages"
        List<Long> pageIDs = createManyPages(250)

        and: "user navigates to index page"
        to PageIndexPage

        then: "only one hundred pages are shown"
        waitFor {pageTableRows.size() == 100}

        and: "there is pagination"
        def pageCount = (int) (250 / 100) + 1
        pageButtons.size() == pageCount + 2 // next and previous button

        cleanup:
        deletePages(pageIDs)
    }

    private List<Long> createManyPages(int count) {
        List<Long> pageIDs = []

        Page.withNewTransaction {
            count.times {
                pageIDs << TestDataUtil.createPage("gebPage" + it).id
            }
        }

        return pageIDs
    }

    private void deletePages(List<Long> pageIDs) {
        Page.withNewTransaction {
            pageIDs.each {
                Page.findById(it).delete(flush: true, failOnError: true)
            }
        }
    }

    def cleanupSpec() {
        doLogout()
    }
}
