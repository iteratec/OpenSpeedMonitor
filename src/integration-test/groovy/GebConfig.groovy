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
    DesiredCapabilities caps = new DesiredCapabilities()
    caps.setCapability('phantomjs.binary.path','./node_modules/phantomjs-prebuilt/bin/phantomjs')

    def driverInstance = new PhantomJSDriver(caps)
    driverInstance.manage().window().maximize()
    driverInstance
}


environments {

    // ./gradlew -Dgeb.env=chrome integrationTest
    chrome {
        driver = {
            def driverInstance = new ChromeDriver()
            driverInstance.manage().window().maximize()
            driverInstance
        }
    }

    // ./gradlew -Dgeb.env=firefox integrationTest
    firefox {
        driver = {
            def driverInstance = new FirefoxDriver()
            driverInstance.manage().window().maximize()
            driverInstance
        }
    }

}
