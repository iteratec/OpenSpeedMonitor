package geb.de.iteratec.osm.measurement.environment

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.security.User
import geb.CustomUrlGebReportingSpec
import geb.IgnoreGebLiveTest
import geb.pages.de.iteratec.osm.measurement.environment.*
import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import org.openqa.selenium.Keys
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Stepwise
/**
 * Tests the CRUD View of {@link Browser}*/
@Integration
@Rollback
@Stepwise
class BrowserGebSpec extends CustomUrlGebReportingSpec {

    private final String browserName = "a geb test browser"

    @Shared
    int browserId

    void "test user gets to browser list when logged in"() {
        given: "User is logged in"
        User.withNewTransaction {
            OsmConfiguration.build()
            createAdminUser()
        }
        doLogin()

        when: "user tries to navigate to browser controller"
        go "/browser/index?lang=en"

        then: "user gets to browser list page"
        at BrowserIndexPage
    }

    void "test createBrowser with invalid input shows error message"() {
        when: "user navigates to browserCreatePage"
        to BrowserCreatePage

        and: "does not fill all required fields"
        createBrowserButton.click()

        then: "an error message is shown on create page"
        at BrowserCreatePage
        errorMessageBox.isDisplayed()
        !errorMessageBoxText.isEmpty()
    }

    void "test createBrowser with valid input"() {
        when: "user navigates to browserCreatePage"
        to BrowserCreatePage

        and: "does fill form correctly"
        browserNameTextField << browserName
        createBrowserButton.click()
        // save browser id for following tests
        browserId = Integer.parseInt(browser.getCurrentUrl().split("/").last())

        then: "user gets to show page of new created browser"
        at BrowserShowPage

        and: "browser id is shown in success div"
        successDiv.isDisplayed()
        successDivText.contains(browserId.toString())
    }

    void "test showBrowser shows correct data"() {
        when: "user navigates to detail page of a browser"
        go "/browser/show/" + browserId
        then: "the browser data is shown"
        at BrowserShowPage
        name == browserName
    }

    void "test editBrowser with invalid data"() {
        when: "user edits browser"
        go "/browser/edit/" + browserId
        at BrowserEditPage

        and: "fills form incorrectly"
        nameTextField << Keys.chord(Keys.CONTROL, "a")
        nameTextField << Keys.chord(Keys.DELETE)
        saveButton.click()

        then: "error message is shown"
        errorMessageBox.isDisplayed()
        !errorMessageBoxText.isEmpty()
    }

    void "test editBrowser"() {
        when: "user navigates to edit page"
        go "/browser/edit/" + browserId

        then: "form is prefilled"
        at BrowserEditPage
        nameTextField.value() == browserName

        when: "user inserts new name"
        String newBrowserName = "a new geb test browser name"
        nameTextField << Keys.chord(Keys.CONTROL, "a")
        nameTextField << newBrowserName

        and: "clicks reset button"
        resetButton.click()

        then: "fields are resetted"
        nameTextField.value() == browserName

        when: "user edits browser"
        nameTextField << Keys.chord(Keys.CONTROL, "a")
        nameTextField << newBrowserName
        saveButton.click()

        then: "user gets to details of edited browser"
        at BrowserShowPage
        browserId == Integer.parseInt(browser.getCurrentUrl().split("/").last())
        successDiv.isDisplayed()
        successDivText.contains(browserId.toString())
        name == newBrowserName
    }

    void "test addBrowserAlias redirects to correct page"() {
        when: "user is on browser edit page"
        go "/browser/edit/" + browserId
        at BrowserEditPage

        and: "clicks addBrowserAlias button"
        addBrowserAliasButton.click()

        then: "user gets to browserAlias crud view"
        waitFor {
            at BrowserAliasCreatePage
        }
    }

    @IgnoreIf(IgnoreGebLiveTest)
    void "test pagination"() {
        when: "there are more than 10 browsers"
        List<Long> browserIDs = createManyBrowsers(250)

        and: "user navigates to index page"
        to BrowserIndexPage

        then: "only one hundred browsers are shown"
        waitFor {browserTableRows.size() == 100}

        and: "there is pagination"
        def pageCount = (int) (250 / 100) + 1
        pageButtons.size() == pageCount + 2 // next and previous button

        cleanup:
        deleteBrowsers(browserIDs)
    }

    private List<Long> createManyBrowsers(int count) {
        List<Long> browserIDs = []

        Browser.withNewTransaction {
            count.times {
                browserIDs << Browser.build(name: "gebBrowser" + it).id
            }
        }

        return browserIDs
    }

    private void deleteBrowsers(List<Long> browserIDs) {
        Browser.withNewTransaction {
            browserIDs.each {
                Browser.findById(it).delete(flush: true, failOnError: true)
            }
        }
    }

    void cleanupSpec() {
        doLogout()
        User.withNewTransaction {
            OsmConfiguration.first().delete()
        }
    }
}
