package geb.de.iteratec.osm.measurement.environment

import de.iteratec.osm.OsmConfiguration
import de.iteratec.osm.measurement.environment.Browser
import de.iteratec.osm.measurement.environment.BrowserAlias
import de.iteratec.osm.security.User
import geb.CustomUrlGebReportingSpec
import geb.IgnoreGebLiveTest
import geb.pages.de.iteratec.osm.measurement.environment.BrowserAliasCreatePage
import geb.pages.de.iteratec.osm.measurement.environment.BrowserAliasEditPage
import geb.pages.de.iteratec.osm.measurement.environment.BrowserAliasIndexPage
import geb.pages.de.iteratec.osm.measurement.environment.BrowserAliasShowPage
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.openqa.selenium.Keys
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Stepwise
/**
 * Tests the CRUD View of {@link BrowserAlias}*
 * Most tests can't be run against live instance because an existing browserAlias is precondition
 */
@Integration
@Rollback
@Stepwise
class BrowserAliasGebSpec extends CustomUrlGebReportingSpec {

    private static final String aliasName = "gebTest Browser Alias"

    @Shared
    Browser existingBrowser
    @Shared
    Long browserAliasId

    def setupData() {
        Browser.withNewTransaction {
            existingBrowser = Browser.build(name: "a geb test browserAlias")
        }
    }

    def cleanupData() {
        BrowserAlias.findById(browserAliasId)?.delete(flush: true)
        existingBrowser.delete(flush: true)
    }

    void cleanupSpec() {
        doLogout()
    }

    void "test user gets to browserAlias list when logged in"() {
        given: "User is logged in"
        User.withNewTransaction {
            if(OsmConfiguration.count()<1) OsmConfiguration.build()
            createAdminUser()
        }
        doLogin()

        when: "user tries to navigate to browserAlias controller"
        go "/browserAlias/index?lang=en"

        then: "user gets to browserAlias list page"
        at BrowserAliasIndexPage
    }

    void "test createBrowserAlias with invalid input shows error message"() {
        when: "user navigates to browserAliasCreatePage"
        to BrowserAliasCreatePage

        and: "does not fill all required fields"
        createBrowserAliasButton.click()

        then: "an error message is shown on create page"
        at BrowserAliasCreatePage
        errorMessageBox.isDisplayed()
        !errorMessageBoxText.isEmpty()
    }

    @IgnoreIf(IgnoreGebLiveTest)
    void "test createBrowserAlias with valid input"() {
        when: "user navigates to browserAliasCreatePage"
        setupData()
        to BrowserAliasCreatePage

        and: "does fill form correctly"
        browserAliasTextField << aliasName
        selectBrowserByID(existingBrowser.id.toString())
        createBrowserAliasButton.click()

        // save browserAlias id for following tests
        browserAliasId = Integer.parseInt(browser.getCurrentUrl().split("/").last())

        then: "user gets to show page of new created browserAlias"
        at BrowserAliasShowPage

        and: "browserAlias id is shown in success div"
        successDiv.isDisplayed()
        successDivText.contains(browserAliasId.toString())
    }

    @IgnoreIf(IgnoreGebLiveTest)
    void "test showBrowserAlias shows correct data"() {
        when: "user navigates to detail page of a browserAlias"
        go "/browserAlias/show/" + browserAliasId

        then: "the browserAlias data is shown"
        at BrowserAliasShowPage
        alias.contains(aliasName)
        browserName.contains(existingBrowser.name)
    }

    @IgnoreIf(IgnoreGebLiveTest)
    void "test editBrowserAlias with invalid data"() {
        when: "user edits browserAlias"
        go "/browserAlias/edit/" + browserAliasId
        at BrowserAliasEditPage

        and: "fills form incorrectly"
        aliasTextField << Keys.chord(Keys.CONTROL, "a")
        aliasTextField << Keys.chord(Keys.DELETE)
        saveButton.click()

        then: "error message is shown"
        errorMessageBox.isDisplayed()
        !errorMessageBoxText.isEmpty()
    }

    @IgnoreIf(IgnoreGebLiveTest)
    void "test editBrowserAlias"() {
        when: "user navigates to edit page"
        go "/browserAlias/edit/" + browserAliasId

        then: "form is prefilled"
        at BrowserAliasEditPage
        aliasTextField.value() == aliasName
        browserDropdown.attr("innerHTML").contains(existingBrowser.name)

        when: "user inserts new name"
        String newBrowserAliasName = "a new geb test browserAlias name"
        aliasTextField << Keys.chord(Keys.CONTROL, "a")
        aliasTextField << newBrowserAliasName

        and: "clicks reset button"
        resetButton.click()

        then: "fields are resetted"
        aliasTextField.value() == aliasName

        when: "user edits browserAlias"
        aliasTextField << Keys.chord(Keys.CONTROL, "a")
        aliasTextField << newBrowserAliasName
        saveButton.click()

        then: "user gets to details of edited browserAlias"
        waitFor {
            at BrowserAliasShowPage
        }
        browserAliasId == Integer.parseInt(browser.getCurrentUrl().split("/").last())
        successDiv.isDisplayed()
        successDivText.contains(browserAliasId.toString())
        alias.contains(newBrowserAliasName)
    }

    @IgnoreIf(IgnoreGebLiveTest)
    void "test delete browserAlias"() {
        given: "user is at detail page of browserAlias"
        go "/browserAlias/show/" + browserAliasId
        at BrowserAliasShowPage

        when: "user clicks delete button"
        deleteButton.click()
        waitFor(5.0) {
            deleteConfirmationDialog.isDisplayed()
        }

        then: "a modal confirmation dialog is show"
        deleteConfirmationDialog.isDisplayed()

        when: "user confirms"
        waitFor {
            deleteConfirmButton.displayed
        }
        waitFor {
            deleteConfirmButton.click()
        }
        waitFor(10.0) {
            at BrowserAliasIndexPage
        }

        then: "user gets to index page"
        successDiv.isDisplayed()
        successDivText.contains(browserAliasId.toString())

        cleanup:
        cleanupData()
    }

    @IgnoreIf(IgnoreGebLiveTest)
    void "test pagination"() {
        when: "there are more than 10 browserAliass"
        List<Long> browserAliasIDs = createManyBrowserAliases(250)

        and: "user navigates to index page"
        to BrowserAliasIndexPage

        then: "only one hundred browserAliases are shown"
        waitFor {browserAliasTableRows.size() == 100}

        and: "there is pagination"
        def pageCount = (int) (250 / 100) + 1
        pageButtons.size() == pageCount + 2 // next and previous button

        cleanup:
        deleteBrowserAliases(browserAliasIDs)
    }

    private List<Long> createManyBrowserAliases(int count) {
        List<Long> browserAliasIDs = []

        BrowserAlias.withNewTransaction {
            count.times {
                String aliasNameString = "gebBrowserAlias" + it
                browserAliasIDs << new BrowserAlias(alias: aliasNameString, browser: existingBrowser).save(failOnError: true, flush: true).id
            }
        }

        return browserAliasIDs
    }

    private void deleteBrowserAliases(List<Long> browserAliasIDs) {
        BrowserAlias.withNewTransaction {
            browserAliasIDs.each {
                BrowserAlias.findById(it).delete(flush: true, failOnError: true)
            }
        }
    }


}
