# language: es
@new_user
Característica: Registro de usuario (New User) en DemoQA
  Quiero comprobar lo que sí se puede automatizar del alta de usuarios,
  dejando documentado por qué el alta completa no entra en la suite.

  @registro_validacion
  Escenario: El formulario de registro exige los campos obligatorios
    Dado que estoy en el formulario de registro
    Cuando envío el formulario sin completar ningún campo
    Entonces los campos obligatorios quedan marcados en el formulario de registro

  @registro_captcha
  Escenario: El alta de usuario sigue protegida por un reCAPTCHA
    Dado que estoy en el formulario de registro
    Entonces el formulario presenta un reCAPTCHA
