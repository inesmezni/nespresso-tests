package commonLibs;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

public class CommonDriver {

    private final WebDriver driver;

    public CommonDriver() {
        System.setProperty("webdriver.edge.driver", "drivers/msedgedriver.exe");

        EdgeOptions options = new EdgeOptions();
        // Masquer les signaux d'automatisation détectés par la protection anti-robot du site
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);
        // Éviter l'erreur ERR_HTTP2_PROTOCOL_ERROR
        options.addArguments("--disable-http2");
        options.addArguments("--lang=fr-FR");

        driver = new EdgeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
    }

    public void navigateToFirstUrl(String url) {
        driver.get(url.trim());
        hideWebdriverFlag();
    }

    public void navigateTo(String url) {
        driver.get(url.trim());
        hideWebdriverFlag();
    }

    private void hideWebdriverFlag() {
        ((JavascriptExecutor) driver).executeScript(
            "Object.defineProperty(navigator, 'webdriver', {get: () => undefined});");
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void closeBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }
}