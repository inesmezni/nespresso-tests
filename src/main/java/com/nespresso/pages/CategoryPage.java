package com.nespresso.pages;

import java.time.Duration;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CategoryPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By pageBody = By.tagName("body");

    // Prix d'un produit au format "TND28.90"
    private static final Pattern PRICE_PATTERN = Pattern.compile("tnd ?[0-9]");
    // Compteur affiché au-dessus de la liste : "7 Machines", "24 produits"...
    private static final Pattern COUNT_PATTERN =
            Pattern.compile("[0-9]+ (machines?|produits?|capsules?|accessoires?|articles?|cafés?)");

    public CategoryPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    // Vérifie que la catégorie est ouverte et affiche des produits
    public boolean isCategoryDisplayed(String urlPart) {
        try {
            wait.until(ExpectedConditions.urlContains(urlPart));
            boolean ok = wait.until(d -> {
                String text = d.findElement(pageBody).getText().toLowerCase();
                boolean hasPrice = PRICE_PATTERN.matcher(text).find();
                boolean hasCount = COUNT_PATTERN.matcher(text).find();
                return (hasPrice || hasCount) && !text.contains("impossible d’accéder");
            });
            scrollToProducts();
            return ok;
        } catch (TimeoutException e) {
            System.out.println("=== DIAGNOSTIC catégorie " + urlPart + " ===");
            System.out.println("URL : " + driver.getCurrentUrl());
            String text = driver.findElement(pageBody).getText();
            System.out.println("Texte : " + text.substring(0, Math.min(600, text.length())));
            return false;
        }
    }

    // Remonte en haut de la page, puis descend doucement jusqu'aux produits
    private void scrollToProducts() {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // 1. Partir du haut de la page
        js.executeScript("window.scrollTo(0, 0);");
        pause(1000);

        // 2. Calculer la position du premier prix (format "TND28.90")
        long target = ((Number) js.executeScript(
            "const els = [...document.querySelectorAll('body *')]" +
            "  .filter(e => e.children.length === 0 && /TND ?[0-9]/.test(e.textContent)" +
            "               && e.getBoundingClientRect().height > 0);" +
            "if (!els.length) return 700;" +
            "const r = els[0].getBoundingClientRect();" +
            "return Math.max(0, Math.round(r.top + window.scrollY - window.innerHeight / 2));"
        )).longValue();

        // 3. Descendre progressivement (150 px toutes les 150 ms)
        long y = 0;
        while (y < target) {
            y = Math.min(y + 150, target);
            js.executeScript("window.scrollTo(0, arguments[0]);", y);
            pause(150);
        }

        // 4. Rester un moment sur les produits
        pause(1500);
    }

    private void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }

    public String getPageTitle() {
        return driver.getTitle();
    }
}