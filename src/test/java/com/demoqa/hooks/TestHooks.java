package com.demoqa.hooks;

import com.demoqa.config.TestConfig;
import com.demoqa.support.TestContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class TestHooks {
    private static final AtomicInteger ORDEN = new AtomicInteger();

    private final TestContext context;

    public TestHooks(TestContext context) { this.context = context; }

    @Before
    public void openBrowser(Scenario scenario) {
        context.config = new TestConfig();
        // Solo los escenarios que usan la cuenta real exigen credenciales.
        // Los casos negativos traen sus propios datos desde el escenario.
        if (scenario.getSourceTagNames().contains("@usuario_real")) {
            context.config.username();
            context.config.password();
        }
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1440,1000");
        if (context.config.headless()) options.addArguments("--headless=new");
        context.driver = new ChromeDriver(options);
        context.driver.manage().timeouts().pageLoadTimeout(context.config.pageLoadTimeout());
    }

    @After
    public void saveEvidenceAndClose(Scenario scenario) throws Exception {
        Path run = Path.of(System.getProperty("run.directory", "target/runs/manual-" + UUID.randomUUID()));
        try {
            Files.createDirectories(run.resolve("screenshots"));
            String filename = evidenceName(scenario);
            Files.writeString(run.resolve(filename + "-resultado.txt"),
                    "Caso: " + scenario.getName() + "\nResultado: " + scenario.getStatus() +
                    "\nAmbiente: " + System.getProperty("env", "qa") +
                    "\nFecha UTC: " + java.time.Instant.now() + "\n");
            if (context.driver != null) {
                try {
                    byte[] screenshot = ((TakesScreenshot) context.driver).getScreenshotAs(OutputType.BYTES);
                    Files.write(run.resolve("screenshots").resolve(filename + ".png"), screenshot);
                    scenario.attach(screenshot, "image/png", "Así terminó el caso");
                } catch (org.openqa.selenium.WebDriverException e) {
                    scenario.log("No pude obtener la captura: el navegador no estaba disponible.");
                    if (!scenario.isFailed()) throw e;
                }
            } else {
                scenario.log("No hay captura porque el navegador no llegó a abrirse. Revisa el error de preparación.");
            }
        } finally {
            if (context.driver != null) context.driver.quit();
        }
    }

    // Nombre legible para la evidencia. Lleva delante el número de orden para
    // que la carpeta se lea en el mismo orden en que corrieron los escenarios,
    // y no alfabéticamente, que es lo que confunde al revisar las capturas.
    private String evidenceName(Scenario scenario) {
        String sinAcentos = Normalizer.normalize(scenario.getName(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String slug = sinAcentos.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (slug.length() > 60) slug = slug.substring(0, 60);
        return String.format("%02d-%s", ORDEN.incrementAndGet(), slug);
    }
}
