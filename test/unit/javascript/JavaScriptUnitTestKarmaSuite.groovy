package javascript

import org.junit.runner.RunWith;
import de.is24.util.karmatestrunner.junit.KarmaTestSuiteRunner;

@RunWith(KarmaTestSuiteRunner.class)
@KarmaTestSuiteRunner.KarmaConfigPath("test/unit/javascript/karma.conf.js")
@KarmaTestSuiteRunner.KarmaProcessName("./node_modules/karma/bin/karma")
public class JavaScriptUnitTestKarmaSuite {
}
