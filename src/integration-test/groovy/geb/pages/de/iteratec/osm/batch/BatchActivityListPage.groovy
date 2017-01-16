package geb.pages.de.iteratec.osm.batch

import geb.pages.de.iteratec.osm.I18nGebPage

class BatchActivityListPage extends I18nGebPage{
    static url = getUrl("/batchActivity/list")

    static at = {
        title == "Batch Activities List"
    }

    static content = {
        showOnlyActiveCheckbox{$("#filterBatchesByActiveCheckbox")}
        batchActivityTableRows{$("#batchActivityTable tbody tr")}
    }
}
