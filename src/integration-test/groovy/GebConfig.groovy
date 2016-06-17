

import org.openqa.selenium.chrome.ChromeDriver

/*
    This is the geb configuration file

    @see http://www.gebish.org/manual/current/#configuration
 */
import org.openqa.selenium.phantomjs.PhantomJSDriver
import org.openqa.selenium.remote.DesiredCapabilities

reportsDir = "target/geb-reports"
reportOnTestFailureOnly = true

// Use phantomJS as the default
driver = {
    new PhantomJSDriver(new DesiredCapabilities())
}

environments {
    // run as “grails -Dgeb.env=chrome test-app”
    chrome {
        driver = { new ChromeDriver() }
    }
}