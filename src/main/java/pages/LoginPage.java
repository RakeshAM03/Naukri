package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;

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

    private final By loginNavLink = By.xpath("//a[normalize-space()='Login'] | //button[normalize-space()='Login']");
    private final By emailField = By.xpath("(//input[@id='usernameField' or @type='email' or contains(translate(@placeholder, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'email')])[1]");
    private final By passwordField = By.xpath("(//input[@id='passwordField' or @type='password' or contains(translate(@placeholder, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'password')])[1]");
    private final By loginSubmitButton = By.cssSelector("button.loginButton, button[type='submit']");
    private final By loginErrorMessage = By.className("erp-msg");
    // Present on naukri.com once a session is authenticated (top-right avatar/dropdown)
    private final By loggedInAvatar = By.xpath("//div[contains(@class,'nI-gNb-drawer')] | //a[contains(@class,'nI-gNb-icon-img')]");
    private static final String DIRECT_LOGIN_URL = "https://www.naukri.com/nlogin/login.php";

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    /** Opens the login flyout from the homepage nav bar. */
    public LoginPage openLoginFlyout() {
        driver.get(DIRECT_LOGIN_URL);
        try {
            waitForVisibility(emailField);
        } catch (TimeoutException e) {
            driver.navigate().back();
            click(loginNavLink);
            waitForVisibility(emailField);
        }
        return this;
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
