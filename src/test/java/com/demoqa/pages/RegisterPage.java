package com.demoqa.pages;

import com.demoqa.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class RegisterPage {
    private final WebDriver driver;
    private final TestConfig config;
    private final WebDriverWait wait;

    private final List<By> requiredFields = List.of(
            By.id("firstname"), By.id("lastname"), By.id("userName"), By.id("password"));
    private final By register = By.id("register");
    private final By captcha = By.cssSelector("iframe[title='reCAPTCHA']");

    public RegisterPage(WebDriver driver, TestConfig config) {
        this.driver = driver;
        this.config = config;
        this.wait = new WebDriverWait(driver, config.waitTimeout());
    }

    public void open() {
        driver.get(config.baseUrl() + "/register");
        wait.until(ExpectedConditions.visibilityOfElementLocated(register));
    }

    public void submit() {
        wait.until(ExpectedConditions.elementToBeClickable(register)).click();
    }

    // Esta validación es del lado del cliente y se dispara antes del captcha,
    // por eso es la única parte del alta que se puede automatizar de punta a
    // punta. El resto del formulario queda detrás del reCAPTCHA.
    public boolean requiredFieldsMarkedInvalid() {
        try {
            return wait.until(d -> requiredFields.stream()
                    .allMatch(locator -> isInvalid(d.findElement(locator))));
        } catch (TimeoutException e) {
            return false;
        }
    }

    // Custodia del estudio de factibilidad: si DemoQA quitara el reCAPTCHA,
    // este paso falla y nos avisa de que conviene rever la decisión de no
    // automatizar el alta completa.
    public boolean showsCaptcha() {
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(captcha)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    private boolean isInvalid(WebElement field) {
        String classes = field.getAttribute("class");
        return classes != null && classes.contains("is-invalid");
    }
}
