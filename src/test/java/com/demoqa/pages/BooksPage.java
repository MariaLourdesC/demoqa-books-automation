package com.demoqa.pages;

import com.demoqa.config.TestConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.stream.Collectors;

public class BooksPage {
    private final WebDriver driver;
    private final TestConfig config;
    private final WebDriverWait wait;

    private final By searchBox = By.id("searchBox");
    // La tabla ya no es un ReactTable: es una <table> plana y el tbody solo
    // contiene filas reales, sin filas de relleno que haya que descartar.
    private final By rows = By.cssSelector("tbody tr");
    private final By titleLink = By.cssSelector("span[id^='see-book-'] a");
    private final By pagination = By.xpath("//span[starts-with(normalize-space(.),'Page ')]");

    public BooksPage(WebDriver driver, TestConfig config) {
        this.driver = driver;
        this.config = config;
        this.wait = new WebDriverWait(driver, config.waitTimeout());
    }

    public void open() {
        driver.get(config.baseUrl() + "/books");
        wait.until(ExpectedConditions.visibilityOfElementLocated(searchBox));
        // Esperamos el catálogo completo: así la búsqueda posterior es un cambio
        // observable y no una foto tomada antes de que cargaran los datos.
        wait.withMessage("El catálogo de libros no llegó a cargarse.")
                .until(d -> !d.findElements(rows).isEmpty());
    }

    public void search(String keyword) {
        WebElement box = wait.until(ExpectedConditions.elementToBeClickable(searchBox));
        box.clear();
        box.sendKeys(keyword);
    }

    // Ancla del caso positivo: esperamos a que el filtro haya terminado, es
    // decir a que todas las filas visibles coincidan con la palabra buscada.
    public boolean waitForResultsMatching(String keyword) {
        String needle = keyword.toLowerCase();
        return waitFor(d -> {
            List<WebElement> found = d.findElements(rows);
            return !found.isEmpty()
                    && found.stream().allMatch(r -> r.getText().toLowerCase().contains(needle));
        });
    }

    // Ancla del caso negativo: el catálogo arranca con filas, así que llegar a
    // cero es una transición real y no el estado previo a que cargue la tabla.
    public boolean waitForNoResults() {
        return waitFor(d -> d.findElements(rows).isEmpty());
    }

    public int resultCount() {
        return driver.findElements(rows).size();
    }

    public List<String> resultTitles() {
        return driver.findElements(titleLink).stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    public String paginationText() {
        List<WebElement> found = driver.findElements(pagination);
        return found.isEmpty() ? "(el paginador no está en la página)" : found.get(0).getText().trim();
    }

    private boolean waitFor(java.util.function.Function<WebDriver, Boolean> condition) {
        try {
            return wait.ignoring(StaleElementReferenceException.class).until(condition::apply);
        } catch (TimeoutException e) {
            return false;
        }
    }
}
