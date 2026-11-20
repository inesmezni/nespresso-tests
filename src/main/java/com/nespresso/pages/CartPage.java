package com.nespresso.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CartPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Pour comparer les textes sans tenir compte des majuscules en XPath
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZÀÂÉÈÊ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyzàâéèê";

    private final By pageBody = By.tagName("body");
    private final By qtyInput = By.cssSelector(
        "input.qty, input[data-role='cart-item-qty'], input[name*='qty'], input[type='number']");
    private final By plusButton = By.cssSelector(
        "button.increase, button[class*='plus'], button[aria-label*='ugmenter'], button[data-action*='increase']");
    private final By updateButton = By.xpath(
        "//button[(@name='update_cart_action' and not(@value='empty_cart'))"
        + " or contains(translate(normalize-space(.),'" + UPPER + "','" + LOWER + "'),'mettre à jour')]");

    // Suppression d'un article (si le site en propose une)
    private final By deleteButton = By.cssSelector(
        "a.action-delete, .action.action-delete, a[title*='Supprimer'], button[title*='Supprimer'], [aria-label*='Supprimer']");

    // « Vider le panier » + confirmation (cas du site Nespresso Tunisie)
    private final By emptyCartButton = By.xpath(
        "//*[@id='empty_cart_button' or ((self::a or self::button)"
        + " and contains(translate(normalize-space(.),'" + UPPER + "','" + LOWER + "'),'vider le panier'))]");
    private final By confirmButton = By.xpath(
        "//*[contains(@class,'modal') and contains(@class,'_show')]//button[contains(@class,'action-accept')"
        + " or contains(@class,'action-primary')"
        + " or contains(translate(normalize-space(.),'" + UPPER + "','" + LOWER + "'),'ok')"
        + " or contains(translate(normalize-space(.),'" + UPPER + "','" + LOWER + "'),'oui')"
        + " or contains(translate(normalize-space(.),'" + UPPER + "','" + LOWER + "'),'confirm')]");

    public CartPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    // TC12 : le produit ajouté apparaît dans le panier
    public boolean isProductInCart(String productName) {
        try {
            return wait.until(d -> {
                String text = d.findElement(pageBody).getText().toLowerCase();
                return text.contains(productName.toLowerCase()) && !text.contains("aucun article");
            });
        } catch (TimeoutException e) {
            diagnostic("TC12");
            return false;
        }
    }

    // TC13 : augmente la quantité de 1 et vérifie la nouvelle valeur
    public boolean increaseQuantity() {
        try {
            int before = getQuantity();
            System.out.println("Quantité avant : " + before);
            WebElement plus = findVisible(plusButton, 3);
            if (plus != null) {
                clickSafely(plus);
            } else {
                WebElement input = findVisible(qtyInput, 10);
                input.clear();
                input.sendKeys(String.valueOf(before + 1));
                WebElement update = findVisible(updateButton, 3);
                if (update != null) {
                    clickSafely(update);
                } else {
                    input.sendKeys(Keys.ENTER);
                }
            }
            boolean ok = wait.until(d -> getQuantity() == before + 1);
            System.out.println("Quantité après : " + getQuantity());
            return ok;
        } catch (Exception e) {
            diagnostic("TC13");
            return false;
        }
    }

    // TC14 : supprime le produit (bouton Supprimer s'il existe, sinon « Vider le panier » + confirmation)
    public void removeProduct() {
        WebElement delete = findVisible(deleteButton, 3);
        if (delete != null) {
            System.out.println("Suppression : bouton Supprimer");
            clickSafely(delete);
        } else {
            WebElement empty = findVisible(emptyCartButton, 10);
            if (empty == null) {
                diagnostic("TC14");
                throw new TimeoutException("Aucun bouton « Supprimer » ni « Vider le panier » visible");
            }
            System.out.println("Suppression : bouton « " + empty.getText().trim() + " »");
            clickSafely(empty);
        }

        // Valider la fenêtre de confirmation si elle apparaît
        WebElement confirm = findVisible(confirmButton, 5);
        if (confirm != null) {
            System.out.println("Confirmation : bouton « " + confirm.getText().trim() + " »");
            clickSafely(confirm);
        }
    }

    // TC14 : le panier est vide
    public boolean isCartEmpty() {
        try {
            return wait.until(d -> {
                String text = d.findElement(pageBody).getText().toLowerCase();
                return text.contains("aucun article") || text.contains("panier est vide");
            });
        } catch (TimeoutException e) {
            diagnostic("TC14");
            return false;
        }
    }

    private int getQuantity() {
        try {
            WebElement input = findVisible(qtyInput, 5);
            return input == null ? -1 : Integer.parseInt(input.getDomProperty("value").trim());
        } catch (Exception e) {
            return -1;
        }
    }

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