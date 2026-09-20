# language: es
@login
Característica: Login en DemoQA Books
  Quiero ingresar con mi usuario ya creado
  para poder ver mi perfil.

  @login_exitoso @usuario_real
  Escenario: Login exitoso con un usuario previamente creado
    Dado que tengo un usuario válido previamente creado y estoy en el login
    Cuando ingreso mi usuario y contraseña
    Y hago clic en Login
    Entonces valido que se abre mi perfil con mi nombre de usuario
    Y veo la opción para cerrar sesión

  @login_fallido
  Esquema del escenario: Login fallido con credenciales inválidas
    Dado que estoy en la página de login
    Cuando ingreso el usuario "<usuario>" y la contraseña "<contraseña>"
    Y hago clic en Login
    Entonces veo el mensaje de error "Invalid username or password!"
    Y permanezco en el login sin acceder al perfil

    Ejemplos:
      | usuario                  | contraseña   |
      | usuario_inexistente_9931 | Cualquiera1@ |
      | test1                    | Incorrecta1@ |

  @login_fallido @campos_vacios
  Escenario: Login fallido al enviar el formulario con los campos vacíos
    Dado que estoy en la página de login
    Cuando hago clic en Login sin completar los campos
    Entonces los campos obligatorios quedan marcados como inválidos
    Y permanezco en el login sin acceder al perfil
