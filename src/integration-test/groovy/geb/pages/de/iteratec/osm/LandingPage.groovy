package geb.pages.de.iteratec.osm

class LandingPage extends I18nGebPage {


    static url = getUrl("/")

    static at = {
        title == getI18nMessage("default.product.title")
    }


    static content = {

    }
}
