package factory;

import helper.ConfigReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

/**
 * Responsible solely for instantiating a configured WebDriver instance.
 * Driver binaries are resolved automatically via WebDriverManager - no
 * manual chromedriver.exe / geckodriver management needed.
 */
public class BrowserFactory {

    public WebDriver createDriver() {
        var browser = ConfigReader.get("browser", "chrome").toLowerCase();
        var headless = ConfigReader.getBoolean("headless");

        WebDriver driver = switch (browser) {
            case "firefox" -> {
                WebDriverManager.firefoxdriver().setup();
                var options = new FirefoxOptions();
                if (headless) {
                    options.addArguments("-headless");
                }
                yield new FirefoxDriver(options);
            }
            case "edge" -> {
                WebDriverManager.edgedriver().setup();
                var options = new EdgeOptions();
                if (headless) {
                    options.addArguments("--headless=new");
                }
                yield new EdgeDriver(options);
            }
            case "chrome" -> {
                WebDriverManager.chromedriver().setup();
                var options = new ChromeOptions();
                options.addArguments("--remote-allow-origins=*", "--disable-notifications", "start-maximized");
                if (headless) {
                    options.addArguments("--headless=new", "--window-size=1920,1080");
                }
                yield new ChromeDriver(options);
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported browser '%s' - expected chrome, firefox, or edge".formatted(browser));
        };

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigReader.getInt("implicitWait")));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getInt("pageLoadTimeout")));
        if (!headless) {
            driver.manage().window().maximize();
        }

        return driver;
    }
}
