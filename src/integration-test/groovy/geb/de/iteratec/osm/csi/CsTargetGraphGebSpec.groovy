package geb.de.iteratec.osm.csi

import de.iteratec.osm.csi.CsTargetGraph
import de.iteratec.osm.csi.CsTargetValue
import de.iteratec.osm.csi.TestDataUtil
import de.iteratec.osm.security.User
import geb.CustomUrlGebReportingSpec
import geb.IgnoreGebLiveTest
import geb.pages.de.iteratec.osm.csi.CsTargetGraphCreatePage
import geb.pages.de.iteratec.osm.csi.CsTargetGraphEditPage
import geb.pages.de.iteratec.osm.csi.CsTargetGraphIndexPage
import geb.pages.de.iteratec.osm.csi.CsTargetGraphShowPage
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.openqa.selenium.Keys
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Stepwise

/**
 * Tests the CRUD View of {@link de.iteratec.osm.csi.CsTargetGraph}* Most TestCases can't be run agains liveInstance beacause there have to be some CsTargetValues
 */
@Integration
@Rollback
@Stepwise
class CsTargetGraphGebSpec extends CustomUrlGebReportingSpec {

    private static final targetGraphLabel = "geb test target graph"
    private static final targetGraphDescription = "a geb test cs target graph description"
    @Shared
    private CsTargetValue pointOne
    @Shared
    private CsTargetValue pointTwo
    @Shared
    Long csTargetGraphId

    void cleanupSpec() {
        doLogout()
    }

    def setupData() {
        CsTargetValue.withNewTransaction {
            pointOne = new CsTargetValue(date: new Date().minus(2), csInPercent: 30).save(failOnError: true)
            pointTwo = new CsTargetValue(date: new Date(), csInPercent: 20).save(failOnError: true)
        }
    }

    def cleanupData() {
        CsTargetGraph.findById(csTargetGraphId)?.delete(flush: true)
        pointOne.delete()
        pointTwo.delete()
    }

    void "test user gets to csTargetGraph list when logged in"() {
        given: "User is logged in"
        User.withNewTransaction {
            TestDataUtil.createOsmConfig()
            TestDataUtil.createAdminUser()
        }
        doLogin()

        when: "user tries to navigate to csTargetGraph controller"
        go "/csTargetGraph/index?lang=en"

        then: "user gets to csTargetGraph list csTargetGraph"
        at CsTargetGraphIndexPage
    }

    void "test createCsTargetGraph with invalid input shows error message"() {
        when: "user navigates to csTargetGraphCreatePage"
        to CsTargetGraphCreatePage

        and: "does not fill all required fields"
        createCsTargetGraphButton.click()

        then: "an error message is shown on create csTargetGraph"
        at CsTargetGraphCreatePage
        errorMessageBox.isDisplayed()
        !errorMessageBoxText.isEmpty()
    }

    @IgnoreIf(IgnoreGebLiveTest)
    void "test createCsTargetGraph with valid input"() {
        when: "user navigates to csTargetGraphCreatePage"
        setupData()
        to CsTargetGraphCreatePage

        and: "does fill form correctly"
        labelTextField << targetGraphLabel
        descriptionTextField << targetGraphDescription
        selectPointTwo()
        createCsTargetGraphButton.click()
        // save csTargetGraph id for following tests
        csTargetGraphId = Integer.parseInt(browser.getCurrentUrl().split("/").last())

        then: "user gets to detail page new created csTargetGraph"
        at CsTargetGraphShowPage

        and: "csTargetGraph id is shown in success div"
        successDiv.isDisplayed()
        successDivText.contains(csTargetGraphId.toString())
    }

    @IgnoreIf(IgnoreGebLiveTest)
    void "test showCsTargetGraph shows correct data"() {
        when: "user navigates to detail page of a csTargetGraph"
        go "/csTargetGraph/show/" + csTargetGraphId

        then: "the csTargetGraph data is shown"
        at CsTargetGraphShowPage
        labelText == targetGraphLabel
        descriptionText == targetGraphDescription
    }

    @IgnoreIf(IgnoreGebLiveTest)
    void "test editCsTargetGraph with invalid data"() {
        when: "user edits csTargetGraph"
        go "/csTargetGraph/edit/" + csTargetGraphId
        at CsTargetGraphEditPage

        and: "fills form incorrectly"
        labelTextField << Keys.chord(Keys.CONTROL, "a")
        labelTextField << Keys.chord(Keys.DELETE)
        saveButton.click()

        then: "error message is shown"
        errorMessageBox.isDisplayed()
        !errorMessageBoxText.isEmpty()
    }

    @IgnoreIf(IgnoreGebLiveTest)
    void "test editCsTargetGraph"() {
        when: "user navigates to edit csTargetGraph"
        go "/csTargetGraph/edit/" + csTargetGraphId

        then: "form is prefilled"
        at CsTargetGraphEditPage
        labelTextField.value() == targetGraphLabel
        descriptionTextField.value() == targetGraphDescription

        when: "user inserts new name"
        String newCsTargetGraphLabel = "a new geb test csTargetGraph label"
        labelTextField << Keys.chord(Keys.CONTROL, "a")
        labelTextField << newCsTargetGraphLabel

        and: "clicks reset button"
        resetButton.click()

        then: "fields are resetted"
        labelTextField.value() == targetGraphLabel

        when: "user edits csTargetGraph"
        labelTextField << Keys.chord(Keys.CONTROL, "a")
        labelTextField << newCsTargetGraphLabel
        saveButton.click()

        then: "user gets to details of edited csTargetGraph"
        at CsTargetGraphShowPage
        csTargetGraphId == Integer.parseInt(browser.getCurrentUrl().split("/").last())
        successDiv.isDisplayed()
        successDivText.contains(csTargetGraphId.toString())
        labelText == newCsTargetGraphLabel
    }

    @IgnoreIf(IgnoreGebLiveTest)
    void "test delete csTargetGraph"() {
        given: "user is at detail page of csTargetGraph"
        go "/csTargetGraph/show/" + csTargetGraphId
        at CsTargetGraphShowPage

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

        then: "user gets to index csTargetGraph"
        waitFor {
            at CsTargetGraphIndexPage
        }
    }

    @IgnoreIf(IgnoreGebLiveTest)
    void "test pagination"() {
        when: "there are more than 10 csTargetGraphs"
        List<Long> csTargetGraphIDs = createManyCsTargetGraphs(250)

        and: "user navigates to index csTargetGraph"
        to CsTargetGraphIndexPage

        then: "only one hundred csTargetGraphs are shown"
        csTargetGraphTableRows.size() == 100

        and: "there is pagination"
        def csTargetGraphCount = (int) (250 / 100) + 1
        pageButtons.size() == csTargetGraphCount + 2 // next and previous button

        cleanup:
        deleteCsTargetGraphs(csTargetGraphIDs)
        cleanupData()
    }

    private List<Long> createManyCsTargetGraphs(int count) {
        List<Long> csTargetGraphIDs = []

        CsTargetGraph.withNewTransaction {
            count.times {
                String graphLabel = "gebTest csTargetGraph" + it
                csTargetGraphIDs << new CsTargetGraph(
                        label: graphLabel,
                        pointOne: pointOne,
                        pointTwo: pointTwo,
                        defaultVisibility: false
                ).save(failOnError: true).id
            }
        }

        return csTargetGraphIDs
    }

    private void deleteCsTargetGraphs(List<Long> csTargetGraphIDs) {
        CsTargetGraph.withNewTransaction {
            csTargetGraphIDs.each {
                CsTargetGraph.findById(it).delete(flush: true, failOnError: true)
            }
        }
    }


}
