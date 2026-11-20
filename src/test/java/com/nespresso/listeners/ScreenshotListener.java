package com.nespresso.listeners;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.nespresso.test.NespressoTest;

/**
 * Livrable 9 : pour chaque test en échec, enregistre dans screenshots/
 *  - une capture d'écran (.png)
 *  - la trace de l'erreur (.txt)
 */
public class ScreenshotListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        Object instance = result.getInstance();
        if (!(instance instanceof NespressoTest)) {
            return;
        }
        WebDriver driver = ((NespressoTest) instance).getDriver();
        String name = result.getMethod().getMethodName() + "_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        try {
            Path folder = Paths.get("screenshots");
            Files.createDirectories(folder);

            // Capture d'écran
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), folder.resolve(name + ".png"), StandardCopyOption.REPLACE_EXISTING);

            // Trace de l'erreur
            StringWriter sw = new StringWriter();
            sw.append("Test : ").append(result.getMethod().getMethodName()).append('\n');
            sw.append("Description : ").append(String.valueOf(result.getMethod().getDescription())).append('\n');
            sw.append("URL : ").append(driver.getCurrentUrl()).append('\n');
            sw.append("Date : ").append(LocalDateTime.now().toString()).append("\n\n");
            if (result.getThrowable() != null) {
                result.getThrowable().printStackTrace(new PrintWriter(sw));
            }
            Files.writeString(folder.resolve(name + ".txt"), sw.toString());

            System.out.println(">>> Capture enregistrée : screenshots/" + name + ".png");
        } catch (Exception e) {
            System.out.println(">>> Capture impossible : " + e.getMessage());
        }
    }
}