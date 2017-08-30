package geb.pages.de.iteratec.osm.wizards

import geb.pages.de.iteratec.osm.I18nGebPage

/**
 * Page of wizard to setup first {@link de.iteratec.osm.measurement.environment.WebPageTestServer} on initial
 * start of application.
 *
 * @author nkuhn
 */
class InfrastructureSetupPage extends I18nGebPage {

    static url = getUrl("/infrastructureSetup")

    static at = {
        title == getI18nMessage("de.iteratec.osm.ui.setupwizards.infra.title")
    }

    void selectCustomServer(){
        serverSelect.click()
        serverSelect.find("option").find{it.value() == "custom"}.click()
    }

    boolean isWebPageTestServerSelected(){
        serverSelect.value() == "www.webpagetest.org"
    }

    boolean isCustomServerSelected(){
        serverSelect.value() == "custom"
    }

    boolean isSubmitEnabled(){
        //The submit button uses a class to be disabled, so we can't just use @disabled
        !submit.classes().contains("disabled")
    }

    boolean hasError(def object){
        object.parent().find("span").classes().contains("fa-times")
    }

    static content = {
        loginForm { $("#setServersForm") }
        serverSelect { loginForm.find("#serverSelect") }
        serverName { loginForm.find("#serverName") }
        serverUrl { loginForm.find("#serverUrl") }
        serverApiKey { loginForm.find("#serverApiKey") }
        submit { loginForm.find("#finishButton") }
        cancel { loginForm.find("#cancelSetup") }
    }

}
