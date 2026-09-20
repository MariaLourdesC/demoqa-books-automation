package com.demoqa.steps;

import com.demoqa.pages.RegisterPage;
import com.demoqa.support.TestContext;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

import static org.junit.Assert.assertTrue;

public class RegisterSteps {
    private final TestContext context;
    private RegisterPage register;

    public RegisterSteps(TestContext context) { this.context = context; }

    @Dado("que estoy en el formulario de registro")
    public void openRegister() {
        register = new RegisterPage(context.driver, context.config);
        register.open();
    }

    @Cuando("envío el formulario sin completar ningún campo")
    public void submitEmpty() {
        register.submit();
    }

    @Entonces("los campos obligatorios quedan marcados en el formulario de registro")
    public void verifyRequiredFields() {
        assertTrue("El formulario no marcó como obligatorios los cuatro campos vacíos.",
                register.requiredFieldsMarkedInvalid());
    }

    // Si esto falla, el reCAPTCHA ya no está y hay que revisar el estudio de
    // factibilidad: el alta completa podría volverse automatizable.
    @Entonces("el formulario presenta un reCAPTCHA")
    public void verifyCaptcha() {
        assertTrue("Ya no se detecta el reCAPTCHA: revisar docs/factibilidad-new-user.md.",
                register.showsCaptcha());
    }
}
