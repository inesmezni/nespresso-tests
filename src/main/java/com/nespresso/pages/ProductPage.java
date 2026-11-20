package com.nespresso.pages;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.StaleElementReferenceException;

public class ProductPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By productTitle = By.cssSelector("h1, .page-title, [itemprop='name']");
    private final By pageBody = By.tagName("body");
    private final By addToCartButton = By.xpath(
        "//button[@id='product-addtocart-button' or contains(translate(normalize-space(.),'AJOUTER','ajouter'),'ajouter')]");

    // Prix au format "TND49.90"
    private static final Pattern PRICE_PATTERN = Pattern.compile("TND ?[0-9]+([.,][0-9]+)?");

    // Nom du produit ouvert (utilisé pour vérifier le panier)
    private String productName = "";

    public ProductPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    // Clique sur le premier PRODUIT de la catégorie : on part d'un prix affiché,
    // puis on remonte jusqu'à la vignette du produit pour trouver son lien
    public void selectFirstProduct(String categoryPath) {
        String categoryUrl = driver.getCurrentUrl();
        WebElement productLink = wait.until(d -> (WebElement) ((JavascriptExecutor) d).executeScript(
            "const path = '/' + arguments[0] + '/';" +
            "const prices = [...document.querySelectorAll('body *')].filter(e =>" +
            "  e.children.length === 0 && /TND ?[0-9]/.test(e.textContent)" +
            "  && e.getBoundingClientRect().height > 0);" +
            "for (const price of prices) {" +
            "  let node = price;" +
            "  for (let i = 0; i < 8 && node; i++) {" +
            "    node = node.parentElement;" +
            "    if (!node) break;" +
            "    const link = [...node.querySelectorAll('a[href*=\"' + path + '\"]')].find(a => {" +
            "      const r = a.getBoundingClientRect();" +
            "      const depth = a.href.split('?')[0].split('/').filter(Boolean).length;" +
            "      return r.width > 0 && r.height > 0 && depth > 5;" +
            "    });" +
            "    if (link) { link.scrollIntoView({block: 'center'}); return link; }" +
            "  }" +
            "}" +
            "return null;", categoryPath));

        System.out.println("Produit sélectionné : " + productLink.getDomProperty("href"));
        clickSafely(productLink);
        wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(categoryUrl)));
    }

    // TC09 : le titre du produit est visible et non vide
    public boolean isProductTitleDisplayed() {
        try {
            WebElement title = wait.until(d -> {
                for (WebElement el : d.findElements(productTitle)) {
                    if (el.isDisplayed() && !el.getText().isBlank()) {
                        return el;
                    }
                }
                return null;
            });
            productName = title.getText().trim();
            System.out.println("Titre du produit : " + productName);
            return true;
        } catch (TimeoutException e) {
            System.out.println("=== DIAGNOSTIC TC09 === URL : " + driver.getCurrentUrl());
            printPageText();
            return false;
        }
    }

    public String getProductName() {
        return productName;
    }

    // TC10 : un prix "TND..." est affiché après le titre du produit
    public boolean isPriceDisplayed() {
        try {
            String price = wait.until(d -> {
                String text = d.findElement(pageBody).getText();
                int start = productName.isEmpty() ? 0 : Math.max(0, text.indexOf(productName));
                Matcher m = PRICE_PATTERN.matcher(text.substring(start));
                return m.find() ? m.group() : null;
            });
            System.out.println("Prix du produit : " + price);
            return true;
        } catch (TimeoutException e) {
            System.out.println("=== DIAGNOSTIC TC10 === URL : " + driver.getCurrentUrl());
            return false;
        }
    }

        // TC11 : cliquer sur « Ajouter au panier »
    public void addToCart() {
        // 1. Attendre que le bouton soit VISIBLE
        WebElement button = wait.until(d -> {
            for (WebElement b : d.findElements(addToCartButton)) {
                try {
                    if (b.isDisplayed()) {
                        return b;
                    }
                } catch (StaleElementReferenceException ignored) {
                }
            }
            return null;
        });

        // 2. Attendre qu'il soit ACTIVÉ (le site le désactive pendant le chargement de la page)
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> button.isEnabled());
        } catch (TimeoutException e) {
            System.out.println("Bouton encore désactivé après 15 s : clic en JavaScript");
        }

        System.out.println("Bouton cliqué : " + button.getText());
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", button);
        clickSafely(button);
    }

    // TC11 : le compteur du panier est > 0 ou un message de confirmation s'affiche
    public boolean isAddToCartConfirmed() {
        try {
            return wait.until(d -> {
                Number count = (Number) ((JavascriptExecutor) d).executeScript(
                    "const n = [...document.querySelectorAll('.counter-number, .counter.qty, [class*=\"counter\"]')]" +
                    "  .map(e => parseInt(e.textContent.replace(/[^0-9]/g, ''))).filter(x => !isNaN(x));" +
                    "return n.length ? Math.max(...n) : 0;");
                String text = d.findElement(pageBody).getText().toLowerCase();
                boolean ok = count.intValue() > 0
                        || text.contains("vous avez ajouté")
                        || text.contains("ajouté à votre panier")
                        || text.contains("ajouté au panier");
                if (ok) {
                    System.out.println("Compteur du panier : " + count);
                }
                return ok;
            });
        } catch (TimeoutException e) {
            System.out.println("=== DIAGNOSTIC TC11 === URL : " + driver.getCurrentUrl());
            printPageText();
            return false;
        }
    }

    private void clickSafely(WebElement element) {
        try {
            element.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    private void printPageText() {
        String text = driver.findElement(pageBody).getText();
        System.out.println("Texte : " + text.substring(0, Math.min(600, text.length())));
    }
}