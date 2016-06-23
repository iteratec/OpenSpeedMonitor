package geb.pages.de.iteratec.osm

import de.iteratec.osm.util.I18nService
import geb.Page
import grails.util.Holders

/**
 * Sets the language of the site to english and allocates i18n service method
 */
class I18nGebPage extends Page {

    I18nService i18nService = Holders.applicationContext.getBean("i18nService")
    private static final String localeString = "en"
    private static final Locale locale = new Locale(localeString)

    static def getUrl(String url) {
        return url + "?lang=" + localeString
    }

    protected String getI18nMessage(String key) {
        i18nService.msgInLocale(key, locale)
    }

    protected String getI18nMessage(String key, List args) {
        i18nService.msgInLocale(key, locale, "", args)
    }
}
