package geb.pages.de.iteratec.osm.measurement.environment

import de.iteratec.osm.measurement.environment.BrowserAlias
import geb.pages.de.iteratec.osm.I18nGebPage

class BrowserAliasCreatePage extends I18nGebPage{
    static url = getUrl("/browserAlias/create")

    static at = {
        title == getI18nMessage("default.create.label", [BrowserAlias.simpleName])
    }
}
