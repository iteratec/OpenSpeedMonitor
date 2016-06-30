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
    def driverInstance = new PhantomJSDriver(new DesiredCapabilities())
    driverInstance.manage().window().maximize()
    driverInstance
}


environments {

    // ./gradlew -Dgeb.env=chrome integrationTest
    chrome {
        driver = { new ChromeDriver() }
    }

    // ./gradlew -Dgeb.env=firefox integrationTest
    firefox {
        driver = { new FirefoxDriver() }
    }

}