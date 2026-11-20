# Tests fonctionnels automatisés – Nespresso Tunisie

Projet Selenium WebDriver + TestNG + Maven qui automatise **20 tests fonctionnels** du site
[https://www.nespresso.com/tn/fr/](https://www.nespresso.com/tn/fr/).

- **Module :** Les fondamentaux du DevOps
- **Étudiante :** Ines Mezni (ISIL-3-1) – École Polytechnique Internationale
- **Dernière exécution :** 09/11/2026 – 20 tests, 20 Passed, 0 Failed, 0 Skipped

---

## 1. Prérequis

| Outil | Version utilisée | Vérification |
|---|---|---|
| Windows | 10 / 11 (64 bits) | — |
| JDK | 17 ou plus (testé avec 21.0.12) | `java -version` |
| Apache Maven | 3.9.x (testé avec 3.9.9) | `mvn -v` |
| Microsoft Edge | 157.0.4341.42 | Edge → Paramètres → À propos |
| msedgedriver | **même version que Edge** | `.\drivers\msedgedriver.exe --version` |
| Connexion Internet | — | Le site testé est en ligne |

Les dépendances (Selenium 4.27.0, TestNG 7.10.2) sont téléchargées automatiquement par Maven.

## 2. Installation

1. Copier le projet, par exemple dans `C:\MiniProjet_Selenium\nespresso-tests`.
2. Vérifier que le driver correspond à la version d'Edge. Sinon, le télécharger (remplacer `VERSION` par la version d'Edge) :

   ```powershell
   Invoke-WebRequest "https://msedgedriver.microsoft.com/VERSION/edgedriver_win64.zip" -OutFile "$env:TEMP\edgedriver.zip"
   Expand-Archive "$env:TEMP\edgedriver.zip" -DestinationPath "$env:TEMP\edgedriver" -Force
   Copy-Item "$env:TEMP\edgedriver\msedgedriver.exe" ".\drivers\msedgedriver.exe" -Force
   ```

3. Vérifier la compilation :

   ```powershell
   mvn clean test-compile
   ```

   Résultat attendu : `BUILD SUCCESS`.

## 3. Exécution

À la racine du projet (dossier qui contient `pom.xml`) :

```powershell
mvn clean test
```

Edge s'ouvre et enchaîne les 20 scénarios (environ 2 minutes). Ne pas utiliser la souris ni le clavier pendant l'exécution.

Résultat attendu :

```
Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 4. Structure du projet

```
nespresso-tests/
├── drivers/
│   └── msedgedriver.exe            Driver Microsoft Edge
├── testdata/
│   └── testdata.csv                Jeux de données (mots-clés, identifiants fictifs)
├── screenshots/                    Captures PNG + traces TXT des tests en échec
├── src/main/java/
│   ├── commonLibs/CommonDriver.java     Ouverture / fermeture d'Edge
│   ├── utils/TestData.java              Lecture de testdata.csv
│   └── com/nespresso/pages/             Page Objects
│       ├── NespressoHomepage.java       Cookies, recherche, boutiques, pied de page
│       ├── CategoryPage.java            Pages de catégories
│       ├── ProductPage.java             Fiche produit, ajout au panier
│       ├── CartPage.java                Panier
│       └── LoginPage.java               Connexion
├── src/test/java/com/nespresso/
│   ├── test/NespressoTest.java          Les 20 tests (@Test priority 1 à 20)
│   └── listeners/ScreenshotListener.java  Capture automatique en cas d'échec
├── pom.xml                         Dépendances Maven + plugin Surefire
├── testng.xml                      Suite TestNG + listener
└── README.md
```

Le projet suit le **Page Object Model** : une classe par page du site (sélecteurs et actions), et une classe de test qui ne contient que les scénarios et les assertions.

## 5. Les 20 tests

| ID | Test | Méthode |
|---|---|---|
| TC01 | Ouvrir la page d'accueil | `openHomepage` |
| TC02 | Accepter le bandeau cookies | `acceptCookies` |
| TC03 | Rechercher un produit existant (« Vertuo ») | `searchExistingProduct` |
| TC04 | Rechercher un produit inexistant | `searchUnknownProduct` |
| TC05 | Catégorie Capsules Original | `openOriginalCapsules` |
| TC06 | Catégorie Capsules Vertuo | `openVertuoCapsules` |
| TC07 | Catégorie Machines | `openMachinesCategory` |
| TC08 | Catégorie Accessoires | `openAccessoriesCategory` |
| TC09 | Sélectionner un produit | `selectProduct` |
| TC10 | Vérifier le prix | `productPriceDisplayed` |
| TC11 | Ajouter au panier | `addProductToCart` |
| TC12 | Vérifier le panier | `verifyCart` |
| TC13 | Modifier la quantité | `updateCartQuantity` |
| TC14 | Vider le panier | `removeProductFromCart` |
| TC15 | Ouvrir le formulaire de connexion | `openLoginPage` |
| TC16 | Connexion refusée sans reCAPTCHA | `invalidLogin` |
| TC17 | Connexion avec champs vides | `emptyLoginFields` |
| TC18 | E-mail au format invalide | `invalidEmailFormat` |
| TC19 | Page des boutiques | `openStoreLocator` |
| TC20 | Lien « Livraison et Retours » | `footerShippingLink` |

TC10 et TC11 dépendent de TC09, TC12 de TC11, TC13 et TC14 de TC12 (`dependsOnMethods`) : si un test échoue, ceux qui en dépendent sont marqués **Skipped**.

## 6. Jeux de données

Fichier `testdata/testdata.csv` (format `cle;valeur`), lu par la classe `utils.TestData` :

| Clé | Valeur | Test |
|---|---|---|
| `recherche.existante` | Vertuo | TC03 |
| `recherche.inexistante` | PRODUIT_INEXISTANT_TEST_987654 | TC04 |
| `login.email.fictif` / `login.password.fictif` | adresse-inexistante@example.com / MotDePasseIncorrect123! | TC16 |
| `login.email.mauvais.format` / `login.password.mauvais.format` | email_sans_arobase / MotDePasse123! | TC18 |

Pour changer une donnée, modifier uniquement le CSV : le code n'a pas à être recompilé.

## 7. Résultats et rapports

| Résultat | Emplacement |
|---|---|
| Rapport HTML TestNG | `target/surefire-reports/index.html` |
| Rapport HTML résumé | `target/surefire-reports/emailable-report.html` |
| Captures d'écran des échecs | `screenshots/<test>_<date>.png` |
| Traces des erreurs | `screenshots/<test>_<date>.txt` |

> `mvn clean` supprime le dossier `target`. Copier les rapports HTML ailleurs avant de relancer si on veut les conserver.

## 8. Points techniques

- **Protection anti-robot :** le site bloque les navigateurs automatisés (`ERR_HTTP2_PROTOCOL_ERROR`). `CommonDriver` configure Edge (`EdgeOptions`) pour masquer les indicateurs d'automatisation et désactiver HTTP/2.
- **Attentes explicites :** toutes les vérifications utilisent `WebDriverWait`. Les seules pauses (`Thread.sleep`) servent à montrer le défilement des catégories à l'écran.
- **reCAPTCHA :** la connexion exige un reCAPTCHA qui ne peut pas être automatisé. TC16 vérifie donc que la connexion est refusée sans validation du reCAPTCHA. Aucune commande ni aucun vrai compte n'est utilisé.

## 9. Dépannage

| Message | Cause | Solution |
|---|---|---|
| `SessionNotCreatedException ... only supports Microsoft Edge version XXX` | Driver différent de la version d'Edge | Télécharger le driver de la bonne version (section 2) |
| `Tests run: 0` | `testng.xml` absent de la racine | Le placer à côté de `pom.xml` |
| `ERR_HTTP2_PROTOCOL_ERROR` dans Edge | Blocage anti-robot temporaire | Attendre 5 à 10 minutes puis relancer |
| `mvn n'est pas reconnu` | PATH non rechargé | Fermer et rouvrir VS Code / PowerShell |
| `Unable to find CDP implementation matching 157` | Avertissement Selenium sans effet | Aucune action |

## 10. Livrables

| N° | Livrable | Emplacement |
|---|---|---|
| 1 | Cahier des charges | `docs/1_Cahier_des_charges.pdf` |
| 2 | Plan de test | `docs/2_Plan_de_test.docx` |
| 3 | Cahier de tests | `docs/3_Cahier_de_tests.xlsx` |
| 4 | Projet Maven | `pom.xml`, `src/` |
| 5 | Configuration TestNG | `testng.xml` |
| 6 | Scripts de tests | `src/test/java/com/nespresso/test/NespressoTest.java` et `src/main/java/com/nespresso/pages/` |
| 7 | Jeux de données | `testdata/testdata.csv` |
| 8 | Rapport d'exécution | `docs/8_Rapport_execution/emailable-report.html` |
| 9 | Gestion des erreurs | `screenshots/` |
| 10 | Documentation technique | `README.md` |
| 11 | Rapport final | `docs/11_Rapport_final.pdf` |