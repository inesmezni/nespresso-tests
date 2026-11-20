package com.nespresso.pages;

import java.time.Duration;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class NespressoHomepage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Bandeau cookies (OneTrust)
    private final By cookieBanner = By.id("onetrust-banner-sdk");
    private final By acceptCookiesButton = By.id("onetrust-accept-btn-handler");

    // Recherche
    private final By searchOpenLink = By.xpath("//*[normalize-space(text())='Chercher']");
    private final By searchInput = By.id("search");
    private final By pageBody = By.tagName("body");
 
        // Menu « Nos Boutiques » et lien du pied de page « Livraison et Retours »
    private final By storeLocatorLink = By.xpath(
        "//a[contains(normalize-space(.),'Nos Boutiques') or contains(@href,'shopfinder')]");
    private final By shippingReturnsLink = By.xpath(
        "//a[contains(normalize-space(.),'Livraison et Retours') or contains(@href,'shipping-returns')]");
    
    // Prix d'un produit au format "TND129.00" (et pas "140 TND" du bandeau)
    private static final Pattern PRICE_PATTERN = Pattern.compile("tnd ?[0-9]");

    public NespressoHomepage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    // ---------- Cookies ----------
    public boolean isCookieBannerDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(acceptCookiesButton)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void acceptCookies() {
        wait.until(ExpectedConditions.elementToBeClickable(acceptCookiesButton)).click();
    }

    public boolean isCookieBannerClosed() {
        try {
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(cookieBanner));
        } catch (TimeoutException e) {
            return false;
        }
    }

    // ---------- Recherche ----------
    public void searchProduct(String keyword) {
        WebElement input = findVisible(searchInput, 3);
        if (input == null) {
            // Le champ est caché : on clique sur le lien « Chercher » VISIBLE
            WebElement openLink = findVisible(searchOpenLink, 15);
            if (openLink == null) {
                throw new TimeoutException("Aucun lien « Chercher » visible sur la page");
            }
            try {
                openLink.click();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", openLink);
            }
            input = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        }
        input.clear();
        input.sendKeys(keyword);
        input.sendKeys(Keys.ENTER);
    }

    public boolean isSearchResultDisplayed(String keyword) {
        try {
            wait.until(ExpectedConditions.urlContains("catalogsearch"));
            // Attendre « résultats trouvés » ou un prix, plus le mot-clé, sans « aucun résultat »
            return wait.until(d -> {
                String text = d.findElement(pageBody).getText().toLowerCase();
                boolean hasResultHeader = text.contains("résultats trouvés");
                boolean hasPrice = PRICE_PATTERN.matcher(text).find();
                return (hasResultHeader || hasPrice)
                        && text.contains(keyword.toLowerCase())
                        && !text.contains("aucun résultat");
            });
        } catch (TimeoutException e) {
            // Diagnostic : afficher l'URL et le début du texte de la page
            System.out.println("=== DIAGNOSTIC TC03 ===");
            System.out.println("URL : " + driver.getCurrentUrl());
            String text = driver.findElement(pageBody).getText();
            System.out.println("Texte : " + text.substring(0, Math.min(800, text.length())));
            return false;
        }
    }

    public boolean isNoSearchResultDisplayed() {
        try {
            wait.until(ExpectedConditions.urlContains("catalogsearch"));
            return wait.until(d -> d.findElement(pageBody).getText().toLowerCase().contains("aucun résultat"));
        } catch (TimeoutException e) {
            return false;
        }
    }

   
     // ---------- Boutiques (TC19) ----------
    public void openStoreLocator() {
        WebElement link = findVisible(storeLocatorLink, 15);
        if (link == null) {
            throw new TimeoutException("Lien « Nos Boutiques » introuvable");
        }
        System.out.println("Clic sur : " + link.getText().trim());
        clickSafely(link);
    }

    public boolean isStoreLocatorDisplayed() {
        try {
            boolean ok = wait.until(d -> {
                String text = d.findElement(pageBody).getText().toLowerCase();
                return !d.getCurrentUrl().contains("customer/account/login")
                        && (text.contains("boutique nespresso") || text.contains("la marsa") || text.contains("adresse"));
            });
            System.out.println("TC19 : " + driver.getTitle() + " | " + driver.getCurrentUrl());
            return ok;
        } catch (TimeoutException e) {
            diagnostic("TC19");
            return false;
        }
    }

    // ---------- Pied de page : Livraison et Retours (TC20) ----------
    public void openShippingReturns() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        WebElement link = findVisible(shippingReturnsLink, 15);
        if (link == null) {
            throw new TimeoutException("Lien « Livraison et Retours » introuvable dans le pied de page");
        }
        System.out.println("Clic sur : " + link.getText().trim());
        clickSafely(link);
    }

    public boolean isShippingReturnsDisplayed() {
        try {
            boolean ok = wait.until(d -> {
                String text = d.findElement(pageBody).getText().toLowerCase();
                return (d.getCurrentUrl().contains("shipping-returns") || text.contains("livraison standard"))
                        && text.contains("retours");
            });
            System.out.println("TC20 : " + driver.getTitle() + " | " + driver.getCurrentUrl());
            return ok;
        } catch (TimeoutException e) {
            diagnostic("TC20");
            return false;
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
        System.out.println("Texte : " + text.substring(0, Math.min(600, text.length())));
    }

    // Renvoie le premier élément VISIBLE correspondant au sélecteur, ou null après le délai
    private WebElement findVisible(By locator, int timeoutSeconds) {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds)).until(d -> {
                for (WebElement el : d.findElements(locator)) {
                    if (el.isDisplayed()) {
                        return el;
                    }
                }
                return null;
            });
        } catch (TimeoutException e) {
            return null;
        }
    }
}