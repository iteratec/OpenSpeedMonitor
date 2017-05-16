package geb.pages.de.iteratec.osm

class LandingPage extends I18nGebPage {


    static url = getUrl("/")

    static at = {
        title == getI18nMessage("default.product.title")
    }

    static content = {
        mainMenuEntryUser { $(".nav.navbar-nav.bottom li a", 0) }
        logoutLink { $("a[href='/logout/index']", 0) }
    }

    public void logoutActualUser(){
        mainMenuEntryUser.click()
        logoutLink.click()
    }
}
