import grails.util.Holders
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.phantomjs.PhantomJSDriver
import org.openqa.selenium.remote.DesiredCapabilities

/*
    This is the geb configuration file

    @see http://www.gebish.org/manual/current/#configuration
 */
reportsDir = "target/geb-reports"
reportOnTestFailureOnly = true

driver = {
    String configuredDriver = Holders.applicationContext.getBean("grailsApplication")?.config?.grails?.de?.iteratec?.test?.geb?.driver
    switch (configuredDriver.toLowerCase()){
        case "chrome":
            return new ChromeDriver()
        case "firefox":
            return new FirefoxDriver()
        case "phantomjs":
            return new PhantomJSDriver(new DesiredCapabilities())
        default: return new PhantomJSDriver(new DesiredCapabilities())
    }
}
