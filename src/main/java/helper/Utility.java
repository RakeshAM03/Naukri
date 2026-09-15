package helper;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Misc reusable utilities: screenshot capture, timestamping, directory helpers.
 */
public class Utility {

    private Utility() {
    }

    /**
     * Captures a screenshot of the current browser state and saves it under
     * the configured screenshots directory. Returns the absolute file path,
     * or null if capture failed (never throws - callers, especially
     * listeners, must not blow up because a screenshot couldn't be taken).
     */
    public static String captureScreenshot(WebDriver driver, String testName) {
        if (!(driver instanceof TakesScreenshot screenshotDriver)) {
            System.err.println("Driver for '" + testName + "' does not support screenshots (null or non-screenshot driver).");
            return null;
        }
        try {
            var screenshotDir = ConfigReader.get("screenshotPath", "screenshots/");
            Files.createDirectories(Paths.get(screenshotDir));

            var timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            var fileName = "%s_%s.png".formatted(testName, timestamp);
            var src = screenshotDriver.getScreenshotAs(OutputType.FILE);
            var dest = Paths.get(screenshotDir, fileName);
            Files.copy(src.toPath(), dest);
            return dest.toAbsolutePath().toString();
        } catch (IOException e) {
            System.err.println("Screenshot capture failed for '" + testName + "': " + e.getMessage());
            return null;
        }
    }

    public static String currentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
