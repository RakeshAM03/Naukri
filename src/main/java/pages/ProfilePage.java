package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

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

    private final By updateResumeButton = By.xpath("//span[contains(text(),'Update resume')] | //a[contains(text(),'Update resume')]");
    private final By resumeFileInput = By.xpath("//input[@type='file']");
    private final By resumeUploadSuccessMsg = By.xpath("//*[contains(normalize-space(),'Uploaded on')]");
    private final By lastUpdatedLabel = By.xpath("//div[contains(@class,'lastUpdated') or contains(text(),'Last updated')]");

    public ProfilePage(WebDriver driver) {
        super(driver);
    }

    /** Navigates directly to the profile page (post-login session required). */
    public ProfilePage goToProfile() {
        driver.get(PROFILE_URL);
        wait.until(ExpectedConditions.urlContains("profile"));
        return this;
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
        // click(updateResumeButton); // uncomment if the file input isn't present until the button is clicked
        uploadFile(resumeFileInput, absoluteResumeFilePath);
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
