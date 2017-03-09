package geb.pages.de.iteratec.osm.measurement.environment.script

import geb.pages.de.iteratec.osm.I18nGebPage

/**
 * @author nkuhn
 */
class ScriptCreatePage extends I18nGebPage{

    static url = getUrl("/script/create")

    static at = {
        title == getI18nMessage("default.create.label", [getI18nMessage("de.iteratec.iss.script")])
    }

    static content = {
        createButton { $("#saveButton") }
        nameInput { $("#label") }
        dangerAlerts { $(".alert.alert-danger ul li") }
    }

}
