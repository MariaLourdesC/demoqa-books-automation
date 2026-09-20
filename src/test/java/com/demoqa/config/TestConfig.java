package com.demoqa.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class TestConfig {
    private final Properties settings = new Properties();
    private final Map<String, String> credentials = new HashMap<>();
    private final String environment = System.getProperty("env", "qa");

    public TestConfig() {
        if (!environment.matches("[a-zA-Z0-9_-]+")) {
            throw new IllegalArgumentException("El nombre del ambiente no es válido.");
        }
        try (InputStream input = getClass().getResourceAsStream("/config/" + environment + ".properties")) {
            if (input == null) throw new IllegalStateException("No existe configuración para el ambiente " + environment);
            settings.load(input);
            Path local = Path.of("config", environment + ".local.properties");
            if (Files.exists(local)) {
                // Se conserva la contraseña tal cual: sin interpretar barras ni signos igual.
                for (String line : Files.readAllLines(local)) {
                    if (line.isBlank() || line.stripLeading().startsWith("#")) continue;
                    int separator = line.indexOf('=');
                    if (separator > 0) credentials.put(line.substring(0, separator).trim(), line.substring(separator + 1));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("No pude cargar la configuración del ambiente.", e);
        }
    }

    public String baseUrl() { return settings.getProperty("base.url").replaceAll("/+$", ""); }
    public Duration waitTimeout() { return Duration.ofSeconds(Long.parseLong(settings.getProperty("wait.seconds"))); }
    public Duration pageLoadTimeout() { return Duration.ofSeconds(Long.parseLong(settings.getProperty("page.load.seconds"))); }
    public boolean headless() { return Boolean.parseBoolean(System.getProperty("headless", "false")); }
    public String environment() { return environment; }
    public String username() { return credential("DEMOQA_USERNAME", "username"); }
    public String password() { return credential("DEMOQA_PASSWORD", "password"); }

    private String credential(String variable, String key) {
        String value = System.getenv(variable);
        if (value == null) value = credentials.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Falta " + key + ". Completa config/" + environment + ".local.properties o la variable " + variable + ".");
        }
        return value;
    }
}
