package com.nespresso.pages;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By signInLink = By.xpath(
        "//*[self::a or self::span or self::button][contains(normalize-space(.),'Sign In')"
        + " or contains(normalize-space(.),'Se connecter') or contains(normalize-space(.),'Connexion')]");
    private final By passwordField = By.cssSelector(
        "#pass, input[name='login[password]'], input[type='password']");
    // Champs recherchés À L'INTÉRIEUR du formulaire du mot de passe
    private final By emailInForm = By.cssSelector(
        "#email, input[name='login[username]'], input[type='email'], input[name*='email'], input[name*='username']");
    private final By submitInForm = By.cssSelector(
        "#send2, button[name='send'], button[type='submit']");
    private final By errorElements = By.cssSelector(
        ".mage-error, div[id$='-error'], .message-error, .messages .message, .field-error, [role='alert'], [class*='error']");
    private final By pageBody = By.tagName("body");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    // TC15 : clic sur « Sign In / Register » (ouvre un panneau de connexion)
    public void clickSignIn() {
        WebElement link = findVisible(signInLink, 10);
        if (link == null) {
            throw new TimeoutException("Lien « Sign In / Register » introuvable");
        }
        System.out.println("Clic sur : " + link.getText().trim());
        clickSafely(link);
    }

    public boolean isLoginFormDisplayed() {
        boolean ok = findVisible(passwordField, 15) != null && getVisibleEmailField() != null;
        if (!ok) {
            diagnostic("TC15");
        }
        return ok;
    }

    // TC16 à TC18 : ouvre la page de connexion dédiée (formulaire propre, sans ancien message)
    public void openLoginForm(String loginUrl) {
        driver.get(loginUrl);
        ((JavascriptExecutor) driver).executeScript(
            "Object.defineProperty(navigator, 'webdriver', {get: () => undefined});");
        if (findVisible(passwordField, 20) == null) {
            diagnostic("ouverture de la page de connexion");
            throw new TimeoutException("Formulaire de connexion introuvable sur " + loginUrl);
        }
    }

    // Saisit l'e-mail et le mot de passe (vides si "") dans le MÊME formulaire, puis valide
    public void login(String email, String password) {
        WebElement passwordInput = findVisible(passwordField, 10);
        WebElement form = passwordInput.findElement(By.xpath("./ancestor::form"));
        WebElement emailInput = firstVisibleIn(form, emailInForm);

        fill(emailInput, email);
        fill(passwordInput, password);

        WebElement submit = firstVisibleIn(form, submitInForm);
        if (submit != null) {
            System.out.println("Bouton : " + submit.getText().trim());
            clickSafely(submit);
        } else {
            passwordInput.sendKeys(Keys.ENTER);
        }
    }

    // Attend un message d'erreur contenant l'un des mots-clés (site ou validation HTML5 du navigateur)
    public boolean isErrorDisplayed(String... keywords) {
        try {
            String found = wait.until(d -> {
                StringBuilder sb = new StringBuilder();
                for (WebElement el : d.findElements(errorElements)) {
                    try {
                        if (el.isDisplayed()) {
                            sb.append(el.getText()).append(' ');
                        }
                    } catch (StaleElementReferenceException ignored) {
                    }
                }
                for (By field : new By[] { emailInForm, passwordField }) {
                    for (WebElement in : d.findElements(field)) {
                        try {
                            if (in.isDisplayed()) {
                                Object msg = ((JavascriptExecutor) d)
                                        .executeScript("return arguments[0].validationMessage;", in);
                                if (msg != null) {
                                    sb.append(msg).append(' ');
                                }
                            }
                        } catch (StaleElementReferenceException ignored) {
                        }
                    }
                }
                String text = sb.toString().toLowerCase();
                for (String k : keywords) {
                    if (text.contains(k.toLowerCase())) {
                        return text.trim();
                    }
                }
                return null;
            });
            System.out.println("Message d'erreur : " + found.substring(0, Math.min(200, found.length())));
            return true;
        } catch (TimeoutException e) {
            diagnostic("connexion");
            return false;
        }
    }
    
        // TC16 : le reCAPTCHA (« I'm not a robot ») est affiché sur le formulaire
    public boolean isRecaptchaDisplayed() {
        By recaptcha = By.cssSelector("iframe[src*='recaptcha'], iframe[title*='reCAPTCHA'], .g-recaptcha");
        boolean ok = findVisible(recaptcha, 10) != null;
        System.out.println("reCAPTCHA affiché : " + ok);
        return ok;
    }

    // TC16 : la connexion est refusée → on reste sur la page de connexion
    public boolean isStillOnLoginPage() {
        String url = driver.getCurrentUrl();
        System.out.println("URL après tentative de connexion : " + url);
        return url.contains("customer/account/login");
    }
    // ---------- Outils ----------
    

    // Vide le champ, tape la valeur, et la réécrit en JavaScript si le site l'a effacée
    private void fill(WebElement input, String value) {
        clickSafely(input);
        input.clear();
        if (!value.isEmpty()) {
            input.sendKeys(value);
            String typed = input.getDomProperty("value");
            if (typed == null || !typed.equals(value)) {
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = arguments[1];"
                    + "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));"
                    + "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
                    input, value);
            }
        }
    }

    private WebElement getVisibleEmailField() {
        WebElement passwordInput = findVisible(passwordField, 5);
        if (passwordInput == null) {
            return null;
        }
        try {
            WebElement form = passwordInput.findElement(By.xpath("./ancestor::form"));
            return firstVisibleIn(form, emailInForm);
        } catch (Exception e) {
            return null;
        }
    }

    private WebElement firstVisibleIn(WebElement container, By locator) {
        List<WebElement> elements = container.findElements(locator);
        for (WebElement el : elements) {
            try {
                if (el.isDisplayed()) {
                    return el;
                }
            } catch (StaleElementReferenceException ignored) {
            }
        }
        return null;
    }

    private WebElement findVisible(By locator, int timeoutSeconds) {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds)).until(d -> {
                for (WebElement el : d.findElements(locator)) {
                    try {
                        if (el.isDisplayed()) {
                            return el;
                        }
                    } catch (StaleElementReferenceException ignored) {
                    }
                }
                return null;
            });
        } catch (TimeoutException e) {
            return null;
        }
    }

    private void clickSafely(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
        try {
            element.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    private void diagnostic(String test) {
        System.out.println("=== DIAGNOSTIC " + test + " === URL : " + driver.getCurrentUrl());
        String text = driver.findElement(pageBody).getText();
        System.out.println("Texte : " + text.substring(0, Math.min(800, text.length())));
    }
}