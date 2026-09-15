package base;

import helper.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Parent class for all Page Objects. Wraps common Selenium interactions
 * (click, type, wait, isDisplayed, ...) behind explicit-wait-safe methods
 * so individual page classes stay thin and declarative.
 */
public class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getInt("explicitWait")));
    }

    protected WebElement waitForVisibility(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void click(By locator) {
        waitForClickable(locator).click();
    }

    protected void type(By locator, String text) {
        var element = waitForVisibility(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(By locator) {
        return waitForVisibility(locator).getText();
    }

    protected boolean isDisplayed(By locator) {
        try {
            return waitForVisibility(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected void uploadFile(By locator, String absoluteFilePath) {
        // File inputs are typically not "clickable" in the visible sense -
        // presence is enough since sendKeys populates the underlying <input type=file>.
        var fileInput = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        fileInput.sendKeys(absoluteFilePath);
    }

    protected void waitForPageTitleContains(String partialTitle) {
        wait.until(ExpectedConditions.titleContains(partialTitle));
    }
}
