package listeners;

import base.BaseClass;
import com.aventstack.chaintest.plugins.ChainTestListener;
import helper.Utility;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;

import java.io.File;

/**
 * TestNG listener wired in via testng.xml <listeners>.
 *
 * Responsibilities:
 *  - On failure: capture a screenshot from the CURRENT test thread's driver
 *    (via BaseClass.getDriver(), which is backed by a ThreadLocal, so this
 *    never throws NullPointerException even under parallel execution, or
 *    if setup failed before a driver was ever created on this thread) and
 *    embed it directly into the ChainTest HTML report entry for that test.
 *  - Extends ChainTestListener (com.aventstack.chaintest.plugins.ChainTestListener)
 *    so all of ChainTest's own lifecycle handling (step logging, pass/fail
 *    status, report generation) continues to work; this class only adds
 *    the screenshot-on-failure behaviour on top via onTestFailure.
 *
 * A JUnit-XML backup is produced automatically by the Surefire plugin
 * (see pom.xml) from the same test run, independent of this listener,
 * which Azure DevOps' "Publish Test Results" pipeline task can consume
 * natively without any extra plugin.
 */
public class ReportListener extends ChainTestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        // Let ChainTest's own listener record the failure status/log first.
        super.onTestFailure(result);

        var testName = result.getMethod().getMethodName();

        // Thread-safe retrieval - never null-pointers even if the driver
        // was never initialised (e.g. failure happened in @BeforeClass).
        WebDriver driver = BaseClass.getDriver();

        if (driver == null) {
            log("No WebDriver available on this thread - skipping screenshot for failed test: " + testName);
            return;
        }

        var screenshotPath = Utility.captureScreenshot(driver, testName);

        if (screenshotPath != null) {
            try {
                log("Test failed: " + testName + " - attaching screenshot.");
                embed(new File(screenshotPath), "image/png");
            } catch (Throwable t) {
                // Reporting must never mask/replace the real test failure -
                // log and continue rather than rethrow.
                System.err.println("Could not embed screenshot into ChainTest report: " + t.getMessage());
            }
        } else {
            log("Screenshot capture failed for test: " + testName);
        }
    }
}
