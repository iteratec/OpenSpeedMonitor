package geb.pages.de.iteratec.osm.measurement.environment.script

import geb.pages.de.iteratec.osm.I18nGebPage

/**
 * @author nkuhn
 */
class ScriptListPage extends I18nGebPage{

    static url = getUrl("/script/list")

    static at = {
        title == getI18nMessage("de.iteratec.isocsi.scripts", [getI18nMessage("de.iteratec.isocsi.scripts")])
    }

    static content = {
        createButton { $("a.btn.btn-primary.pull-right") }
        allScripts{$(".scriptLabel")}
    }

}
