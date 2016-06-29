import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.phantomjs.PhantomJSDriver

/*
    This is the geb configuration file

    @see http://www.gebish.org/manual/current/#configuration
 */
reportsDir = "target/geb-reports"
reportOnTestFailureOnly = true

environments {
    // ./gradlew -Dgeb.env=chrome integrationTest
    chrome {
        driver = { new ChromeDriver() }
    }

    // ./gradlew -Dgeb.env=firefox integrationTest
    firefox {
        driver = { new FirefoxDriver() }
    }

    // ./gradlew -Dgeb.env=phantomJs integrationTest
    phantomJs {
        driver = {
            def driverInstance = new PhantomJSDriver()
            driverInstance.manage().window().maximize()
            driverInstance
        }
    }
}