# DemoQA Books - automatización con Java y Selenium

## Qué estoy validando

Por ahora tengo cubierto el login, en sus dos caras.

El **login exitoso**: ingreso con un usuario que ya está creado y valido que se abre su perfil, que aparece su nombre de usuario y que está la opción para cerrar sesión. Este caso no crea usuarios; para ejecutarlo necesito un usuario válido de DemoQA.

El **login fallido**, en las tres formas en que puede fallar:

- Con un usuario que no existe.
- Con un usuario que sí existe pero la contraseña equivocada.
- Enviando el formulario con los campos vacíos.

Las dos primeras las agrupé en un mismo esquema porque el sitio las trata igual. La tercera va aparte, y más abajo explico por qué.

## Qué uso y para qué

- Java: para escribir el código de la prueba.
- Selenium: para abrir el navegador e interactuar con la página.
- Cucumber y Gherkin: para describir los casos con pasos que se puedan leer fácilmente.
- Page Object Model: cada página tiene su clase con sus elementos y acciones.
- Maven: descarga las dependencias y ejecuta los casos. Cucumber se ejecuta con JUnit.

El archivo `pom.xml` es la configuración de Maven.

## Antes de ejecutar

Necesito un JDK 17 o superior, Maven 3.9, Google Chrome e internet para acceder a DemoQA y descargar las dependencias. Maven debe reconocer el JDK; lo compruebo con:

```sh
mvn -version
```

En este equipo se instaló Maven con Homebrew, que también instaló Java. Si empiezo en otro Mac con Homebrew:

```sh
brew install maven
```

Abro una terminal en la carpeta `demoqa-books-automation`, donde está `pom.xml`.

## Dónde pongo mi usuario

En `config/qa.local.properties` completo:

```properties
username=
password=
```

Escribo el valor justo después de `=`, sin comillas, y guardo el archivo. No pongo espacios extra. Si el archivo no existe, copio `config/qa.example.properties` y nombro la copia `qa.local.properties`.

Este archivo está excluido de Git. No pongo las credenciales en el escenario ni en el código. También puedo usar las variables `DEMOQA_USERNAME` y `DEMOQA_PASSWORD`; si están definidas, tienen prioridad sobre el archivo local.

Solo los escenarios marcados con `@usuario_real` necesitan estas credenciales. Los casos de login fallido traen sus propios datos escritos en el escenario, porque son credenciales inválidas a propósito y no hay nada que proteger. Por eso corren igual aunque el archivo esté vacío.

Un detalle que me costó al crear el usuario: el formulario de registro de DemoQA no acepta cualquier carácter especial, aunque su mensaje diga "one non alphanumeric character". Con un punto me lo rechazó; con `@` lo aceptó.

## Cómo ejecuto los casos

Con el navegador visible:

```sh
mvn test
```

Sin mostrar la ventana del navegador:

```sh
mvn test -Dheadless=true
```

Para correr un grupo de casos uso las etiquetas del escenario:

```sh
mvn test -Dcucumber.filter.tags="@login_exitoso"
mvn test -Dcucumber.filter.tags="@login_fallido"
mvn test -Denv=qa -Dcucumber.filter.tags="@login"
```

El runner no tiene ninguna etiqueta fija, así que `mvn test` sin filtro ejecuta todo lo que haya escrito.

Selenium Manager administra el driver del navegador. Si hay un ChromeDriver antiguo instalado en el equipo, puedo indicarle que no use el del PATH:

```sh
SE_SKIP_DRIVER_IN_PATH=true mvn test
```

## Cómo están escritos los casos

Los dos casos de login inválido comparten un `Esquema del escenario` con una tabla de `Ejemplos`. Los junté porque comprobé que DemoQA responde exactamente igual en los dos: mismo mensaje, misma URL. Si el resultado esperado es el mismo, el escenario puede ser el mismo y solo cambian los datos.

Los campos vacíos van en un escenario propio porque el sitio los trata distinto: no muestra el mensaje de credenciales inválidas, marca los campos en rojo. Como el resultado esperado es otro, la validación tiene que ser otra.

Cada caso de login fallido comprueba tres cosas, no una:

1. Que aparece el mensaje de error.
2. Que no llegué al perfil.
3. Que sigo en el login.

La segunda es la que de verdad importa. Validar solo el mensaje no alcanza: una aplicación rota podría mostrar el error y dejarme entrar igual, y la prueba pasaría sin darse cuenta.

## Cómo está organizado

```text
config/
  qa.example.properties       Ejemplo sin credenciales reales
  qa.local.properties         Mi usuario local; no se sube a Git
src/test/java/com/demoqa/
  config/TestConfig.java      Carga el ambiente y los datos del usuario
  pages/LoginPage.java        Campos, acciones y mensaje de error del login
  pages/ProfilePage.java      Elementos del perfil
  steps/LoginSteps.java       Conecta Gherkin con las páginas y validaciones
  hooks/TestHooks.java        Abre Chrome, guarda la evidencia y lo cierra
  support/TestContext.java    Comparte el navegador dentro de cada escenario
  runners/RunCucumberTest.java Ejecuta los casos con Cucumber
src/test/resources/
  config/qa.properties        URL y tiempos máximos de espera
  features/login.feature      Casos escritos en español
target/runs/                  Reportes y evidencias de cada ejecución
```

Cucumber lee los escenarios de `src/test/resources/features`.

## Configuración por ambiente

Por ahora solo está definido `qa`, que apunta a `https://demoqa.com`. No se ha inventado una URL de otro ambiente.

Si después tengo otro ambiente real, agrego su archivo en `src/test/resources/config/`, con `base.url`, `wait.seconds` y `page.load.seconds`, y sus credenciales locales en `config/<ambiente>.local.properties`. Lo selecciono con `-Denv=<ambiente>`.

## Cómo espero a que cargue la página

No uso `Thread.sleep`. Uso esperas explícitas de Selenium: el campo debe estar visible, el botón disponible o la URL debe cambiar al perfil. La prueba sigue apenas se cumple la condición; si no ocurre dentro del máximo configurado, falla.

En los casos que fallan hay además un detalle de orden. Después del clic en Login la aplicación tarda unos segundos en responder. Si preguntara enseguida si estoy en el perfil, la respuesta sería "no" simplemente porque todavía no pasó nada, no porque el login se haya rechazado. La prueba pasaría por el motivo equivocado. Por eso primero espero a que aparezca el mensaje de error, y recién después reviso la URL: cuando el mensaje está en pantalla ya sé que la aplicación contestó.

## Dónde veo el resultado

Cada `mvn test` crea una carpeta con fecha y hora UTC dentro de `target/runs/`. Allí encuentro:

- `cucumber.html`: reporte que puedo abrir en el navegador, con los pasos y la captura adjunta.
- `cucumber.json` y `cucumber.xml`: resultados para otras herramientas.
- `screenshots/`: captura de cómo terminó cada escenario, pase o falle.
- `login-...-resultado.txt`: nombre del caso, estado, ambiente y fecha.
- `surefire/`: resultado de Maven y JUnit.

Cuando una validación falla, el reporte muestra el valor esperado y el que se obtuvo. Eso fue a propósito: las páginas devuelven lo que leyeron y la comparación se hace en el paso, así no me queda un error de espera agotada que no explica nada.

Si faltan las credenciales, los escenarios que las necesitan fallan en la preparación con un mensaje claro. No se abre Chrome y no hay captura de navegador. Eso no significa que el login tenga un bug.

Los reportes y capturas quedan fuera de Git. La captura del perfil puede mostrar el nombre del usuario de prueba. Antes de compartir evidencia, reviso su contenido. `mvn clean` elimina `target`, incluidos los reportes anteriores; si necesito conservarlos, los copio antes.

## Qué se ha comprobado

La suite completa se ejecutó el 20 de septiembre de 2026 con Chrome en modo headless: cuatro escenarios, ninguno fallido.

```text
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

El reporte de esa corrida quedó en `target/runs/20260920-162005-524/`.

Los localizadores se verificaron contra el sitio real: los campos del login tienen los identificadores `userName` y `password`, el botón tiene `login`, el perfil presenta `userName-value` y un botón `Logout`. El mensaje de error del login se renderiza en `<p id="name">Invalid username or password!</p>`.

Una observación que anoté mientras probaba a mano: DemoQA devuelve el mismo mensaje tanto si el usuario no existe como si la contraseña es incorrecta. No es un descuido, es lo correcto: un mensaje distinto para cada caso le permitiría a un atacante averiguar qué usuarios existen.

## Alcance actual

Está cubierto el login, exitoso y fallido. Quedan pendientes la búsqueda de libros con palabra clave existente, la búsqueda con palabra clave inexistente y el estudio de factibilidad de New User.

Referencias: [instalación de Cucumber para Java](https://cucumber.io/docs/installation/java/) y [esperas explícitas de Selenium](https://www.selenium.dev/documentation/webdriver/support_features/expected_conditions/).
