package geb.pages.de.iteratec.osm.wizards

import geb.pages.de.iteratec.osm.I18nGebPage

/**
 * Page of wizard to setup first {@link de.iteratec.osm.measurement.environment.WebPageTestServer} on initial
 * start of application.
 *
 * @author nkuhn
 */
class InfrastructureSetupPage extends I18nGebPage{

    static url = getUrl("/")

    static at = {
        title == getI18nMessage("de.iteratec.osm.ui.setupwizards.infra.title")
    }

}
