package base;

import factory.BrowserFactory;
import helper.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * Every test class extends this. Handles driver setup/teardown around the
 * test class lifecycle and exposes the WebDriver via a ThreadLocal so that
 * parallel TestNG execution never leaks a driver instance across threads,
 * and so that ReportListener (which runs on the same test thread) can
 * always safely retrieve the *current* driver without a NullPointerException.
 */
public class BaseClass {

    // ThreadLocal so each TestNG thread (parallel="methods"/"classes") gets
    // its own isolated WebDriver instance.
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        var driver = new BrowserFactory().createDriver();
        driverThreadLocal.set(driver);
        driver.get(ConfigReader.get("url"));
    }

    /**
     * Thread-safe accessor used everywhere (page objects, listeners, test
     * classes) instead of passing the driver around manually. Returns null
     * if no driver has been initialised on the calling thread yet/anymore -
     * callers such as ReportListener MUST null-check rather than assume
     * a driver always exists.
     */
    public static WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        var driver = driverThreadLocal.get();
        if (driver != null) {
            driver.quit();
            driverThreadLocal.remove();
        }
    }
}
