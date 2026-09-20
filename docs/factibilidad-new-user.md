# Estudio de factibilidad: automatizar New User

## Conclusión

**No conviene automatizar el alta completa de usuarios por la interfaz.** El formulario está protegido con reCAPTCHA v2, que es un control diseñado justamente para distinguir a una persona de un programa. Automatizarlo significa derrotarlo, y toda forma de hacerlo sale cara, es frágil o no depende de mí.

Lo que sí hago: automatizo la parte del formulario que responde antes del captcha, y creo los usuarios que las pruebas necesitan por API.

## Qué revisé

Abrí `https://demoqa.com/register` con Selenium y miré el formulario por dentro.

Los campos existen y son localizables sin problema: `firstname`, `lastname`, `userName`, `password` y el botón `register`. Hasta ahí, automatizable.

El problema aparece después. La página carga un `iframe` con `title="reCAPTCHA"` que apunta a `https://www.google.com/recaptcha/api2/anchor`. Es un reCAPTCHA v2, el de "No soy un robot".

Después probé qué validaciones contestan sin resolver ese captcha:

| Qué probé | Qué pasó | ¿Automatizable? |
|---|---|---|
| Enviar el formulario vacío | Los cuatro campos se marcan con la clase `is-invalid` | Sí |
| Enviar con una contraseña que no cumple la política | No aparece ningún mensaje | No |
| Crear el usuario | No se crea | No |

La primera validación es del lado del cliente y se dispara antes del captcha. Las otras dos viven detrás. Esa es la frontera exacta.

## Por qué no lo automatizo por la interfaz

**El captcha está ahí para impedir exactamente esto.** No es un obstáculo técnico que haya que sortear, es un requisito de negocio funcionando como corresponde. Una prueba que lo burla deja de probar el sistema real.

Las tres salidas posibles, y por qué descarto cada una:

1. **Usar la clave de prueba pública de Google**, que hace que el captcha apruebe siempre. Requiere cambiar la configuración del sitio y yo no la controlo: DemoQA no es mío.
2. **Contratar un servicio que resuelva captchas.** Cuesta dinero, agrega entre 10 y 30 segundos por ejecución, mete una dependencia externa más y es discutible usarlo contra un sitio ajeno.
3. **Pedir que lo desactiven en un ambiente de pruebas.** Es la solución correcta en un proyecto real, pero depende del equipo de desarrollo. En DemoQA no existe ese ambiente.

## Riesgos si lo automatizara igual

- **Pruebas intermitentes.** El reCAPTCHA v2 puede escalar del checkbox a un desafío de imágenes cuando sospecha del tráfico. La prueba fallaría unos días sí y otros no, sin que nada del sistema haya cambiado.
- **Rojos que no son defectos.** Un fallo del captcha se vería igual que un fallo del registro. Se pierde tiempo investigando lo que no es.
- **Dependencia de un tercero.** Si Google cambia el widget, se rompe mi prueba aunque DemoQA esté perfecta.
- **Posible bloqueo.** El tráfico automatizado repetido puede terminar limitado por IP.
- **Costo de mantenimiento alto para poco valor.** Sería el caso más caro de la suite y el que menos confianza aporta.

## Dependencias

- Que el equipo dueño del sitio pueda desactivar el captcha en un ambiente de pruebas, o exponer un mecanismo para saltarlo controladamente.
- Un ambiente de pruebas separado, para no ensuciar producción con usuarios de prueba.
- Una forma de limpiar los usuarios creados. Hoy no la hay, así que cada ejecución dejaría basura acumulada.

## Lo que sí propongo

**Crear los usuarios por API.** Comprobé que el endpoint de alta funciona sin captcha:

```sh
curl -X POST "https://demoqa.com/Account/v1/User" \
  -H "Content-Type: application/json" \
  -d '{"userName":"<usuario>","password":"<contraseña>"}'
```

Devolvió `HTTP 201` con el `userID` del usuario creado, y el endpoint `POST /Account/v1/GenerateToken` devolvió `HTTP 200` con un token válido. Es decir: el alta por API funciona sin resolver ningún captcha.

Esto habilita el patrón que me parece correcto para cualquier suite: **preparar los datos por API y validar el flujo de negocio por la interfaz.** El usuario se crea en menos de un segundo y sin intervención, y la prueba de interfaz se concentra en lo que realmente quiere validar, que es el login.

**Automatizo la validación de campos obligatorios.** Es la parte del formulario que responde antes del captcha, así que entra en la suite sin trucos. Está en `register.feature`.

**Dejo una prueba que vigila la decisión.** El escenario `@registro_captcha` comprueba que el reCAPTCHA sigue presente. Si algún día DemoQA lo quita, esa prueba falla y me avisa de que conviene releer este documento en lugar de dar la decisión por eterna.

## Qué haría cambiar esta conclusión

- Que exista un ambiente de pruebas con el captcha desactivado.
- Que el sitio pase a reCAPTCHA v3 o a un control que permita una clave de prueba.
- Que el registro se vuelva crítico para el negocio y justifique el costo. Incluso entonces empezaría cubriéndolo por API, y dejaría una única prueba manual para el formulario.

## Observaciones que anoté de paso

**El captcha protege la interfaz pero no la API.** El formulario exige resolver un reCAPTCHA, pero `POST /Account/v1/User` crea usuarios sin pedir nada. Cualquiera puede dar de alta cuentas en masa salteándose el control. Para mí es un defecto de seguridad: la protección debería estar en el servidor, no solo en la pantalla. En un proyecto real lo reportaría.

**El mensaje de la política de contraseñas no coincide con lo que valida.** El formulario dice pedir "one non alphanumeric character", pero rechaza una contraseña terminada en punto y acepta la misma con `@`. O la validación acepta solo una lista cerrada de caracteres, o el mensaje está mal redactado. Defecto de severidad baja, pero confunde a quien se registra.
