package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.nio.file.Path;

/**
 * Page Object for the Naukri "My Profile" page, specifically the resume
 * upload/update widget.
 *
 * NOTE ON LOCATORS: as with LoginPage, naukri.com's DOM can change. The
 * resume upload control is generally a hidden <input type="file"> tied to
 * a visible "Update resume" button. Confirm/update these locators against
 * the live DOM before relying on this in production.
 */
public class ProfilePage extends BasePage {

    private static final String PROFILE_URL = "https://www.naukri.com/mnjuser/profile";

    private final By profileMenuButton = By.xpath("//div[contains(@class,'nI-gNb-drawer')] | //a[contains(@class,'nI-gNb-icon-img')] | //*[@aria-label='Profile']");
    private final By viewProfileLink = By.xpath("(//a[contains(@href, '/mnjuser/profile') or contains(@href, '/profile')][contains(normalize-space(), 'View Profile') or contains(normalize-space(), 'My Profile')] | //*[(self::a or self::button or @role='menuitem')][contains(normalize-space(), 'View Profile') or contains(normalize-space(), 'My Profile')])[1]");
    private final By updateResumeButton = By.xpath("//span[contains(text(),'Update resume')] | //a[contains(text(),'Update resume')]");
    private final By resumeFileInput = By.xpath("//input[@type='file']");
    private final By resumeUploadSuccessMsg = By.xpath("//*[contains(normalize-space(),'Uploaded on')]");
    private final By lastUpdatedLabel = By.xpath("//div[contains(@class,'lastUpdated') or contains(text(),'Last updated')]");

    public ProfilePage(WebDriver driver) {
        super(driver);
    }

    /** Opens the authenticated profile through the View Profile menu item. */
    public ProfilePage goToProfile() {
        driver.switchTo().defaultContent();
        try {
            clickViewProfile();
        } catch (TimeoutException e) {
            driver.get(PROFILE_URL);
        }
        wait.until(ExpectedConditions.urlContains("profile"));
        return this;
    }

    private void clickViewProfile() {
        if (driver.findElements(viewProfileLink).isEmpty()) {
            click(profileMenuButton);
        }
        var viewProfile = wait.until(ExpectedConditions.presenceOfElementLocated(viewProfileLink));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', inline:'center'});", viewProfile);
        try {
            wait.until(ExpectedConditions.elementToBeClickable(viewProfileLink)).click();
        } catch (WebDriverException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", viewProfile);
        }
    }

    /**
     * Uploads/re-uploads the resume file at the given absolute path.
     * The underlying <input type="file"> is usually present in the DOM
     * even when visually hidden behind a styled "Update resume" button,
     * so sendKeys works without needing to click the decorative button
     * first. If your build of the page requires a physical click to
     * reveal the input, uncomment the click(updateResumeButton) line.
     */
    public void uploadResume(String absoluteResumeFilePath) {
        for (WebElement updateButton : driver.findElements(updateResumeButton)) {
            if (updateButton.isDisplayed() && updateButton.isEnabled()) {
                updateButton.click();
                break;
            }
        }
        uploadFile(resumeFileInput, Path.of(absoluteResumeFilePath).toAbsolutePath().normalize().toString());
    }

    public boolean isUploadSuccessful() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(resumeUploadSuccessMsg));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getLastUpdatedText() {
        return isDisplayed(lastUpdatedLabel) ? getText(lastUpdatedLabel) : "";
    }
}
