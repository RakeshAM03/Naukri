package pages;

import base.BasePage;
import helper.ConfigReader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

/**
 * Page Object for the Naukri.com login flyout (accessible via the "Login"
 * link in the top nav on the homepage).
 *
 * NOTE ON LOCATORS: naukri.com is a live third-party site whose DOM/CSS
 * can change without notice. The locators below reflect the commonly
 * documented structure of the login form (id="usernameField" /
 * id="passwordField" / login submit button) as of this writing. Before
 * relying on this in production, open the login flyout in the browser
 * DevTools and confirm/update these By locators against the current markup.
 */
public class LoginPage extends BasePage {

    private final By loginNavLink = By.cssSelector("a[href*='nLogin/Login.php'], a[href*='/nlogin/login']");
    private final By emailField = By.cssSelector("input[placeholder='Enter Email ID / Username'], input[placeholder*='Email'], input[placeholder*='Username'], #usernameField, input[type='email']");
    private final By passwordField = By.cssSelector("input[placeholder='Enter Password'], input[placeholder*='password' i], #passwordField, input[type='password']");
    private final By loginSubmitButton = By.cssSelector("button.loginButton, button[type='submit']");
    private final By loginErrorMessage = By.className("erp-msg");
    private final By consentButton = By.xpath("//button[contains(translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'accept') or contains(translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'agree') or contains(translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'got it')]");
    // Present on naukri.com once a session is authenticated (top-right avatar/dropdown)
    private final By loggedInAvatar = By.xpath("//div[contains(@class,'nI-gNb-drawer')] | //a[contains(@class,'nI-gNb-icon-img')]");
    private static final String DIRECT_LOGIN_URL = "https://login.naukri.com/nLogin/Login.php";

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    /** Opens the login flyout from the homepage nav bar. */
    public LoginPage openLoginFlyout() {
        driver.get(ConfigReader.get("url"));
        try {
            dismissConsentIfPresent();
            click(loginNavLink);
            switchToLoginFrameIfPresent();
            waitForVisibility(emailField);
        } catch (WebDriverException e) {
            driver.get(DIRECT_LOGIN_URL);
            switchToLoginFrameIfPresent();
            waitForVisibility(emailField);
        }
        return this;
    }

    private void switchToLoginFrameIfPresent() {
        driver.switchTo().defaultContent();
        if (!driver.findElements(emailField).isEmpty()) {
            return;
        }
        for (WebElement frame : driver.findElements(By.tagName("iframe"))) {
            driver.switchTo().defaultContent();
            driver.switchTo().frame(frame);
            if (!driver.findElements(emailField).isEmpty()) {
                return;
            }
        }
        driver.switchTo().defaultContent();
    }

    private void dismissConsentIfPresent() {
        try {
            WebElement consent = new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(3))
                    .until(ExpectedConditions.elementToBeClickable(consentButton));
            consent.click();
        } catch (TimeoutException ignored) {
            // No consent prompt is present.
        }
    }

    public LoginPage enterEmail(String email) {
        type(emailField, email);
        return this;
    }

    public LoginPage enterPassword(String password) {
        type(passwordField, password);
        return this;
    }

    public void clickLogin() {
        click(loginSubmitButton);
    }

    /**
     * Convenience method wrapping the full happy-path login sequence.
     */
    public void login(String email, String password) {
        openLoginFlyout();
        enterEmail(email);
        enterPassword(password);
        clickLogin();
    }

    public boolean isLoginErrorDisplayed() {
        return isDisplayed(loginErrorMessage);
    }

    public String getLoginErrorText() {
        return getText(loginErrorMessage);
    }

    /**
     * Waits for a post-login indicator (nav avatar/drawer) to confirm the
     * session actually authenticated, rather than assuming success just
     * because no exception was thrown on click.
     */
    public boolean isLoginSuccessful() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(loggedInAvatar));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
