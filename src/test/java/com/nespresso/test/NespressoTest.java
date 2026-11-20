package com.nespresso.test;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.nespresso.pages.CartPage;
import com.nespresso.pages.CategoryPage;
import com.nespresso.pages.LoginPage;
import com.nespresso.pages.NespressoHomepage;
import com.nespresso.pages.ProductPage;

import commonLibs.CommonDriver;

import utils.TestData;

public class NespressoTest {

    private static final String URL = "https://www.nespresso.com/tn/fr/";

    private CommonDriver cmnDriver;
    private WebDriver driver;
    private NespressoHomepage homepage;
    private CategoryPage categoryPage;
    private ProductPage productPage;
    private CartPage cartPage;
    private LoginPage loginPage;

    @BeforeClass
    public void invokeBrowser() {
        cmnDriver = new CommonDriver();
        driver = cmnDriver.getDriver();
        cmnDriver.navigateToFirstUrl(URL);
        homepage = new NespressoHomepage(driver);
        categoryPage = new CategoryPage(driver);
        productPage = new ProductPage(driver);
        cartPage = new CartPage(driver);
        loginPage = new LoginPage(driver);
    }

    public WebDriver getDriver() {
        return driver;
    }

    // TC01 - Ouvrir la page d'accueil
    @Test(priority = 1)
    public void openHomepage() {
        String currentUrl = driver.getCurrentUrl();
        String title = driver.getTitle();
        System.out.println("URL : " + currentUrl);
        System.out.println("Titre : " + title);

        Assert.assertTrue(currentUrl.contains("nespresso.com/tn/fr"),
                "L'URL de la page d'accueil est incorrecte : " + currentUrl);
        Assert.assertTrue(title.toLowerCase().contains("nespresso"),
                "Le titre de la page ne contient pas « Nespresso » : " + title);
    }

    // TC02 - Accepter le bandeau cookies
    @Test(priority = 2)
    public void acceptCookies() {
        Assert.assertTrue(homepage.isCookieBannerDisplayed(),
                "Le bandeau cookies n'est pas affiché");
        homepage.acceptCookies();
        Assert.assertTrue(homepage.isCookieBannerClosed(),
                "Le bandeau cookies est toujours visible après le clic");
    }

    // TC03 - Rechercher un produit existant
    @Test(priority = 3)
    public void searchExistingProduct() {
                String keyword = TestData.get("recherche.existante");
        homepage.searchProduct(keyword);
        Assert.assertTrue(homepage.isSearchResultDisplayed(keyword),
                "Aucun résultat affiché pour la recherche « " + keyword + " »");
    }

    // TC04 - Rechercher un produit inexistant
    @Test(priority = 4)
    public void searchUnknownProduct() {
                homepage.searchProduct(TestData.get("recherche.inexistante"));
        Assert.assertTrue(homepage.isNoSearchResultDisplayed(),
                "Le message « aucun résultat » n'est pas affiché");
    }

    // TC05 - Ouvrir la catégorie Capsules Original
    @Test(priority = 5)
    public void openOriginalCapsules() {
        cmnDriver.navigateTo(URL + "coffee-capsules/original");
        System.out.println("TC05 : " + categoryPage.getPageTitle());
        Assert.assertTrue(categoryPage.isCategoryDisplayed("coffee-capsules/original"),
                "La catégorie Capsules Original ne s'affiche pas avec des produits");
    }

    // TC06 - Ouvrir la catégorie Capsules Vertuo
    @Test(priority = 6)
    public void openVertuoCapsules() {
        cmnDriver.navigateTo(URL + "coffee-capsules/vertuo");
        System.out.println("TC06 : " + categoryPage.getPageTitle());
        Assert.assertTrue(categoryPage.isCategoryDisplayed("coffee-capsules/vertuo"),
                "La catégorie Capsules Vertuo ne s'affiche pas avec des produits");
    }

    // TC07 - Ouvrir la catégorie Machines
    @Test(priority = 7)
    public void openMachinesCategory() {
        cmnDriver.navigateTo(URL + "coffee-machines/original");
        System.out.println("TC07 : " + categoryPage.getPageTitle());
        Assert.assertTrue(categoryPage.isCategoryDisplayed("coffee-machines"),
                "La catégorie Machines ne s'affiche pas avec des produits");
    }

    // TC08 - Ouvrir la catégorie Accessoires
    @Test(priority = 8)
    public void openAccessoriesCategory() {
        cmnDriver.navigateTo(URL + "accessories");
        System.out.println("TC08 : " + categoryPage.getPageTitle());
        Assert.assertTrue(categoryPage.isCategoryDisplayed("accessories"),
                "La catégorie Accessoires ne s'affiche pas avec des produits");
    }

    // TC09 - Sélectionner un produit (depuis la catégorie Accessoires ouverte par TC08)
    @Test(priority = 9, dependsOnMethods = "openAccessoriesCategory")
    public void selectProduct() {
        productPage.selectFirstProduct("accessories");
        Assert.assertTrue(productPage.isProductTitleDisplayed(),
                "La fiche produit ne s'affiche pas (titre absent)");
    }

    // TC10 - Vérifier le prix du produit
    @Test(priority = 10, dependsOnMethods = "selectProduct")
    public void productPriceDisplayed() {
        Assert.assertTrue(productPage.isPriceDisplayed(),
                "Aucun prix en TND n'est affiché sur la fiche produit");
    }

    // TC11 - Ajouter le produit au panier
    @Test(priority = 11, dependsOnMethods = "selectProduct")
    public void addProductToCart() {
        productPage.addToCart();
        Assert.assertTrue(productPage.isAddToCartConfirmed(),
                "Aucune confirmation après l'ajout au panier");
    }

    // TC12 - Vérifier le contenu du panier
    @Test(priority = 12, dependsOnMethods = "addProductToCart")
    public void verifyCart() {
        cmnDriver.navigateTo(URL + "checkout/cart/");
        Assert.assertTrue(cartPage.isProductInCart(productPage.getProductName()),
                "Le produit « " + productPage.getProductName() + " » n'est pas dans le panier");
    }

    // TC13 - Modifier la quantité dans le panier
    @Test(priority = 13, dependsOnMethods = "verifyCart")
    public void updateCartQuantity() {
        Assert.assertTrue(cartPage.increaseQuantity(),
                "La quantité du produit n'a pas été mise à jour");
    }

    // TC14 - Supprimer le produit du panier
    @Test(priority = 14, dependsOnMethods = "verifyCart")
    public void removeProductFromCart() {
        cartPage.removeProduct();
        Assert.assertTrue(cartPage.isCartEmpty(),
                "Le panier n'est pas vide après la suppression");
    }

        // TC15 - Ouvrir le formulaire de connexion (lien « Sign In / Register »)
    @Test(priority = 15)
    public void openLoginPage() {
        loginPage.clickSignIn();
        Assert.assertTrue(loginPage.isLoginFormDisplayed(),
                "Le formulaire de connexion (e-mail / mot de passe) n'est pas affiché");
    }

        // TC16 - Connexion refusée sans validation du reCAPTCHA (identifiants fictifs)
    @Test(priority = 16)
    public void invalidLogin() {
        loginPage.openLoginForm(URL + "customer/account/login/");
        Assert.assertTrue(loginPage.isRecaptchaDisplayed(),
                "Le reCAPTCHA n'est pas affiché sur le formulaire de connexion");
                loginPage.login(TestData.get("login.email.fictif"), TestData.get("login.password.fictif"));
        Assert.assertTrue(loginPage.isErrorDisplayed("obligatoire", "incorrect", "invalide", "captcha"),
                "Aucun message d'erreur après la tentative de connexion");
        Assert.assertTrue(loginPage.isStillOnLoginPage(),
                "L'utilisateur a quitté la page de connexion : la connexion n'a pas été refusée");
    }

    // TC17 - Connexion avec des champs vides
    @Test(priority = 17)
    public void emptyLoginFields() {
        loginPage.openLoginForm(URL + "customer/account/login/");
        loginPage.login("", "");
        Assert.assertTrue(loginPage.isErrorDisplayed("obligatoire", "requis", "required", "renseigner"),
                "Aucun message « champ obligatoire » avec des champs vides");
    }

    // TC18 - E-mail au format invalide
    @Test(priority = 18)
    public void invalidEmailFormat() {
        loginPage.openLoginForm(URL + "customer/account/login/");
                loginPage.login(TestData.get("login.email.mauvais.format"), TestData.get("login.password.mauvais.format"));
        Assert.assertTrue(loginPage.isErrorDisplayed("valide", "valid", "@"),
                "Aucun message de format invalide pour un e-mail sans @");
    }

        // TC19 - Ouvrir la page des boutiques (menu « Nos Boutiques »)
    @Test(priority = 19)
    public void openStoreLocator() {
        homepage.openStoreLocator();
        Assert.assertTrue(homepage.isStoreLocatorDisplayed(),
                "La page des boutiques ne s'affiche pas");
    }

    // TC20 - Ouvrir le lien « Livraison et Retours » du pied de page
    @Test(priority = 20)
    public void footerShippingLink() {
        homepage.openShippingReturns();
        Assert.assertTrue(homepage.isShippingReturnsDisplayed(),
                "La page « Livraison et Retours » ne s'affiche pas");
    }

    @AfterClass(alwaysRun = true)
    public void closeBrowser() {
        if (cmnDriver != null) {
            cmnDriver.closeBrowser();
        }
    }
}