import org.openqa.selenium.Dimension
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxDriver
/*
    This is the geb configuration file

    @see http://www.gebish.org/manual/current/#configuration
 */
reportsDir = "target/geb-reports"
reportOnTestFailureOnly = true

driver = {
    ChromeOptions options = new ChromeOptions()
    options.addArguments('--headless', '--disable-gpu', '--lang=en-us', '--window-size=1000x1000')
    def driverInstance = new ChromeDriver(options)
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
