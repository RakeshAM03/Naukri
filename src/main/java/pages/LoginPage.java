package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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
    private final By consentButton = By.xpath("//button[normalize-space()='Got it' or normalize-space()='Accept' or normalize-space()='Agree']");
    // Present on naukri.com once a session is authenticated (top-right avatar/dropdown)
    private final By loggedInAvatar = By.xpath("//div[contains(@class,'nI-gNb-drawer')] | //a[contains(@class,'nI-gNb-icon-img')]");
    private static final String HOME_URL = "https://www.naukri.com/";

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    /** Opens the login form through the homepage Login link. */
    public LoginPage openLoginFlyout() {
        driver.get(HOME_URL);
        dismissConsentIfPresent();
        click(loginNavLink);
        switchToLoginFrameIfPresent();
        waitForVisibility(emailField);
        return this;
    }

    private void dismissConsentIfPresent() {
        for (WebElement consent : driver.findElements(consentButton)) {
            if (consent.isDisplayed() && consent.isEnabled()) {
                consent.click();
                return;
            }
        }
    }

    private void switchToLoginFrameIfPresent() {
        driver.switchTo().defaultContent();
        if (findLoginFrame(0)) {
            return;
        }
        driver.switchTo().defaultContent();
    }

    private boolean findLoginFrame(int depth) {
        if (!driver.findElements(emailField).isEmpty()) {
            return true;
        }
        if (depth >= 3) {
            return false;
        }
        for (WebElement frame : driver.findElements(By.tagName("iframe"))) {
            driver.switchTo().frame(frame);
            if (findLoginFrame(depth + 1)) {
                return true;
            }
            driver.switchTo().parentFrame();
        }
        return false;
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
        driver.switchTo().defaultContent();
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
        driver.switchTo().defaultContent();
        try {
            wait.until(currentDriver -> {
                if (!currentDriver.findElements(loginErrorMessage).isEmpty()
                        && currentDriver.findElement(loginErrorMessage).isDisplayed()) {
                    return false;
                }

                var currentUrl = currentDriver.getCurrentUrl().toLowerCase();
                var authenticatedUrl = currentUrl.contains("/mnjuser/")
                        || currentUrl.contains("/my-naukri")
                        || currentUrl.contains("/dashboard");
                var authenticatedElement = !currentDriver.findElements(loggedInAvatar).isEmpty()
                        || !currentDriver.findElements(By.xpath("//*[contains(normalize-space(), 'My Naukri') or contains(normalize-space(), 'View Profile')]")).isEmpty();
                return authenticatedUrl || authenticatedElement;
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
