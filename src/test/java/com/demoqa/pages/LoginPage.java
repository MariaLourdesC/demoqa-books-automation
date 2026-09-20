package com.demoqa.pages;

import com.demoqa.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginPage {
    public static final String SIN_MENSAJE = "(no apareció ningún mensaje de error)";

    private final WebDriver driver;
    private final TestConfig config;
    private final WebDriverWait wait;
    private final By username = By.id("userName");
    private final By password = By.id("password");
    private final By login = By.id("login");
    private final By error = By.id("name");

    public LoginPage(WebDriver driver, TestConfig config) {
        this.driver = driver;
        this.config = config;
        this.wait = new WebDriverWait(driver, config.waitTimeout());
    }

    public void open() {
        driver.get(config.baseUrl() + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(username));
        wait.until(ExpectedConditions.visibilityOfElementLocated(password));
    }

    public void enterCredentials(String user, String secret) {
        var userField = wait.until(ExpectedConditions.elementToBeClickable(username));
        userField.clear();
        userField.sendKeys(user);
        var passwordField = wait.until(ExpectedConditions.elementToBeClickable(password));
        passwordField.clear();
        passwordField.sendKeys(secret);
    }

    public void submit() {
        wait.until(ExpectedConditions.elementToBeClickable(login)).click();
    }

    // Ancla de sincronización del caso negativo: esperamos a que la aplicación
    // responda antes de afirmar nada sobre la URL. Si no aparece, devolvemos un
    // texto reconocible para que la aserción falle con un mensaje legible.
    public String errorMessage() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(error)).getText().trim();
        } catch (TimeoutException e) {
            return SIN_MENSAJE;
        }
    }

    public boolean fieldsMarkedInvalid() {
        try {
            return wait.until(d -> isInvalid(d.findElement(username)) && isInvalid(d.findElement(password)));
        } catch (TimeoutException e) {
            return false;
        }
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    private boolean isInvalid(WebElement field) {
        String classes = field.getAttribute("class");
        return classes != null && classes.contains("is-invalid");
    }
}
