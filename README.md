# DemoQA Books - automatización con Java y Selenium

## Qué estoy validando

Tengo cubiertos los cinco puntos del desafío: el login en sus dos caras, la búsqueda de libros con palabra clave existente e inexistente, y el estudio de factibilidad de New User.

El **login exitoso**: ingreso con un usuario que ya está creado y valido que se abre su perfil, que aparece su nombre de usuario y que está la opción para cerrar sesión. Este caso no crea usuarios; para ejecutarlo necesito un usuario válido de DemoQA.

El **login fallido**, en las tres formas en que puede fallar:

- Con un usuario que no existe.
- Con un usuario que sí existe pero la contraseña equivocada.
- Enviando el formulario con los campos vacíos.

Las dos primeras las agrupé en un mismo esquema porque el sitio las trata igual. La tercera va aparte, y más abajo explico por qué.

La **búsqueda de libros**, con palabras que están en el catálogo y con una que no está. Cuando la palabra existe no me alcanza con que aparezcan resultados: compruebo que *todos* los que aparecen corresponden a lo que busqué.

El **registro de usuario**, solo en la parte que se puede automatizar sin resolver el captcha. La justificación completa está en [docs/factibilidad-new-user.md](docs/factibilidad-new-user.md).

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
mvn test -Dcucumber.filter.tags="@login"
mvn test -Dcucumber.filter.tags="@books"
mvn test -Dcucumber.filter.tags="@new_user"
mvn test -Dcucumber.filter.tags="@login_fallido"
```

Hoy la suite tiene 11 escenarios: 4 de login, 5 de búsqueda y 2 de registro. Conviene no contar los bloques del archivo: un `Esquema del escenario` se ejecuta una vez por cada fila de `Ejemplos`, así que 2 bloques en `books.feature` dan 5 ejecuciones.

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

En la búsqueda sigo el mismo criterio. No compruebo que aparezcan resultados, compruebo que **todos** los resultados coinciden con lo que busqué. Contar filas no detectaría un buscador que devuelve cualquier cosa. No fijo la cantidad exacta esperada a propósito: sería una aserción más estricta pero se rompería el día que cambie el catálogo, y lo que quiero verificar es la relevancia, no el inventario.

Separé los `Ejemplos` de la búsqueda en dos bloques: palabras que están en el título y palabras que están en el autor o la editorial. Así queda documentado que el buscador filtra por el registro completo y no solo por el título.

## Cómo está organizado

```text
config/
  qa.example.properties       Ejemplo sin credenciales reales
  qa.local.properties         Mi usuario local; no se sube a Git
src/test/java/com/demoqa/
  config/TestConfig.java      Carga el ambiente y los datos del usuario
  pages/LoginPage.java        Campos, acciones y mensaje de error del login
  pages/ProfilePage.java      Elementos del perfil
  pages/BooksPage.java        Buscador y tabla de resultados
  pages/RegisterPage.java     Formulario de alta y detección del captcha
  steps/LoginSteps.java       Conecta Gherkin con las páginas y validaciones
  steps/BooksSteps.java       Pasos de la búsqueda de libros
  steps/RegisterSteps.java    Pasos del formulario de registro
  hooks/TestHooks.java        Abre Chrome, guarda la evidencia y lo cierra
  support/TestContext.java    Comparte el navegador dentro de cada escenario
  runners/RunCucumberTest.java Ejecuta los casos con Cucumber
src/test/resources/
  config/qa.properties        URL y tiempos máximos de espera
  features/login.feature      Casos de login
  features/books.feature      Casos de búsqueda
  features/register.feature   Casos de registro que no dependen del captcha
docs/
  factibilidad-new-user.md    Estudio de factibilidad de New User
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
- `reporte/cucumber-html-reports/`: el reporte presentable, con gráficos y las capturas embebidas.

El reporte presentable lo genera `maven-cucumber-reporting` a partir de `cucumber.json`, y se arma solo al terminar `mvn test`. Para verlo abro `reporte/cucumber-html-reports/overview-features.html`. Tiene una vista por feature, otra por paso y otra por etiqueta, y arriba muestra el ambiente, el sitio, el navegador y si se corrió en headless.

Una nota sobre la versión del plugin: quedó fijada en `5.8.0` a propósito. Las versiones posteriores están compiladas para Maven 4 y, con Maven 3, su configuración se ignora en silencio y la generación falla.

Cuando una validación falla, el reporte muestra el valor esperado y el que se obtuvo. Eso fue a propósito: las páginas devuelven lo que leyeron y la comparación se hace en el paso, así no me queda un error de espera agotada que no explica nada.

Si faltan las credenciales, los escenarios que las necesitan fallan en la preparación con un mensaje claro. No se abre Chrome y no hay captura de navegador. Eso no significa que el login tenga un bug.

Los reportes y capturas quedan fuera de Git. La captura del perfil puede mostrar el nombre del usuario de prueba. Antes de compartir evidencia, reviso su contenido. `mvn clean` elimina `target`, incluidos los reportes anteriores; si necesito conservarlos, los copio antes.

## Qué se ha comprobado

La suite completa se ejecutó el 20 de septiembre de 2026 con Chrome en modo headless: once escenarios, ninguno fallido.

```text
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
```

El reporte de esa corrida quedó en `target/runs/20260920-162005-524/`.

Los localizadores se verificaron contra el sitio real: los campos del login tienen los identificadores `userName` y `password`, el botón tiene `login`, el perfil presenta `userName-value` y un botón `Logout`. El mensaje de error del login se renderiza en `<p id="name">Invalid username or password!</p>`.

En la lista de libros me encontré con que DemoQA **ya no usa ReactTable**. Los localizadores `rt-tr-group`, `rt-td` y `rt-noData` que aparecen en casi todos los ejemplos publicados ya no existen: hoy es una `<table>` HTML normal. Lo comprobé volcando el DOM real antes de escribir el Page Object.

## Lo que encontré mientras probaba

**El mismo mensaje para los dos errores de login.** DemoQA responde igual si el usuario no existe que si la contraseña es incorrecta. No es un descuido, es lo correcto: un mensaje distinto para cada caso le permitiría a un atacante averiguar qué usuarios existen.

**"ISTQB Fundamentals" no está en el catálogo.** El enunciado lo propone como ejemplo de palabra clave existente, pero consulté el catálogo en `GET /BookStore/v1/Books` y tiene ocho libros, ninguno de ISTQB. Son todos de JavaScript, Git y ECMAScript. Así que usé palabras que sí existen para el caso de búsqueda con resultados, y le di a "ISTQB Fundamentals" el lugar que le corresponde: el caso de búsqueda sin resultados.

**La búsqueda sin resultados no avisa nada.** Cuando no hay coincidencias la tabla queda vacía y no aparece ningún mensaje. Lo único que cambia es el paginador, que pasa a decir `Page 1 of 0`. El usuario no puede distinguir entre "no hay resultados" y "algo se rompió". Lo uso como validación porque es lo único observable, pero me parece un defecto de experiencia de uso.

**El captcha protege la pantalla pero no la API.** El formulario de registro exige resolver un reCAPTCHA, pero `POST /Account/v1/User` crea usuarios sin pedir nada y devuelve `201`. La protección debería estar también del lado del servidor. Lo detallo en el estudio de factibilidad.

**El mensaje de la política de contraseñas no coincide con lo que valida.** Dice pedir "one non alphanumeric character", pero rechaza una contraseña terminada en punto y acepta la misma con `@`.


Referencias: [instalación de Cucumber para Java](https://cucumber.io/docs/installation/java/) y [esperas explícitas de Selenium](https://www.selenium.dev/documentation/webdriver/support_features/expected_conditions/).
