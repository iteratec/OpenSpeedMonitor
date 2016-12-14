import org.openqa.selenium.Dimension
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
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
    caps.setCapability("takesScreenshot", true)

    def driverInstance = new PhantomJSDriver(caps)
    driverInstance.manage().window().setSize(new Dimension(1000,1000)) // width of bootstrap 3  viewport of col-md-* is 970px
    driverInstance
}


environments {

    // ./gradlew -Dgeb.env=chrome integrationTest
    chrome {
        driver = {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--lang=en-us")
            def driverInstance = new ChromeDriver(options)
            driverInstance.manage().window().setSize(new Dimension(1000,1000))// width of bootstrap 3  viewport of col-md-* is 970px
            driverInstance
        }
    }

    // ./gradlew -Dgeb.env=firefox integrationTest
    firefox {
        driver = {
            def driverInstance = new FirefoxDriver()
            driverInstance.manage().window().setSize(new Dimension(1000,1000))// width of bootstrap 3  viewport of col-md-* is 970px
            driverInstance
        }
    }

}
