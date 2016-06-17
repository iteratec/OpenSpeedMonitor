import grails.util.Holders
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.firefox.FirefoxDriver

/*
    This is the geb configuration file

    @see http://www.gebish.org/manual/current/#configuration
 */
import org.openqa.selenium.phantomjs.PhantomJSDriver
import org.openqa.selenium.remote.DesiredCapabilities

reportsDir = "target/geb-reports"
reportOnTestFailureOnly = true

driver = {
    def configuredDriver = Holders.applicationContext.getBean("grailsApplication")?.config?.grails?.de?.iteratec?.test?.geb?.driver
    switch (configuredDriver){
        case "chrome":
            return new ChromeDriver()
        case "Firefox":
            return new FirefoxDriver()
        default: return new FirefoxDriver()
    }
}
