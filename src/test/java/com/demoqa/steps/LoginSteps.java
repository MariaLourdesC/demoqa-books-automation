package com.demoqa.steps;

import com.demoqa.pages.LoginPage;
import com.demoqa.pages.ProfilePage;
import com.demoqa.support.TestContext;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LoginSteps {
    private final TestContext context;
    private LoginPage login;
    private ProfilePage profile;

    public LoginSteps(TestContext context) { this.context = context; }

    private void openLoginPage() {
        login = new LoginPage(context.driver, context.config);
        profile = new ProfilePage(context.driver, context.config);
        login.open();
    }

    @Dado("que tengo un usuario válido previamente creado y estoy en el login")
    public void openLoginWithValidUser() { openLoginPage(); }

    @Dado("que estoy en la página de login")
    public void openLoginForNegativeCase() { openLoginPage(); }

    @Cuando("ingreso mi usuario y contraseña")
    public void enterConfiguredCredentials() {
        login.enterCredentials(context.config.username(), context.config.password());
    }

    @Cuando("ingreso el usuario {string} y la contraseña {string}")
    public void enterGivenCredentials(String user, String secret) {
        login.enterCredentials(user, secret);
    }

    @Cuando("hago clic en Login")
    public void submit() { login.submit(); }

    @Cuando("hago clic en Login sin completar los campos")
    public void submitWithoutFilling() { login.submit(); }

    @Entonces("valido que se abre mi perfil con mi nombre de usuario")
    public void verifyProfile() {
        assertEquals("El perfil debe mostrar el usuario con el que ingresé.",
                context.config.username(), profile.displayedUser());
    }

    @Entonces("veo la opción para cerrar sesión")
    public void verifyLogout() {
        assertTrue("Debe aparecer la opción Logout.", profile.showsLogout());
    }

    @Entonces("veo el mensaje de error {string}")
    public void verifyErrorMessage(String expected) {
        assertEquals("El mensaje de error del login no es el esperado.", expected, login.errorMessage());
    }

    @Entonces("los campos obligatorios quedan marcados como inválidos")
    public void verifyInvalidFields() {
        assertTrue("Los campos vacíos no quedaron marcados como inválidos.", login.fieldsMarkedInvalid());
    }

    // Se ejecuta después de la aserción del mensaje: para entonces la aplicación
    // ya respondió, así que comprobar la URL no es una carrera contra el request.
    @Entonces("permanezco en el login sin acceder al perfil")
    public void verifyStillOnLogin() {
        String url = login.currentUrl();
        assertFalse("Un login inválido llegó al perfil. URL: " + url, url.contains("/profile"));
        assertTrue("Se esperaba seguir en el login y la URL fue: " + url, url.contains("/login"));
    }
}
