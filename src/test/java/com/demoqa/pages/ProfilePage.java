package com.demoqa.pages;

import com.demoqa.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ProfilePage {
    private final WebDriver driver;
    private final TestConfig config;
    private final WebDriverWait wait;
    private final By username = By.id("userName-value");
    private final By logout = By.xpath("//button[normalize-space()='Logout']");

    public ProfilePage(WebDriver driver, TestConfig config) {
        this.driver = driver;
        this.config = config;
        this.wait = new WebDriverWait(driver, config.waitTimeout());
    }

    // Devolvemos el valor observado y dejamos la aserción en el step: así el
    // reporte muestra el esperado contra el obtenido y no un timeout opaco.
    public String displayedUser() {
        wait.withMessage("El perfil no llegó a mostrar el nombre de usuario.")
                .until(ExpectedConditions.urlContains("/profile"));
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(username)).getText().trim();
        } catch (TimeoutException e) {
            return "(el perfil no mostró ningún usuario)";
        }
    }

    public boolean showsLogout() {
        try {
            return wait.until(ExpectedConditions.elementToBeClickable(logout)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }
}
