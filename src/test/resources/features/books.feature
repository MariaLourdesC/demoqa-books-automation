# language: es
@books
Característica: Búsqueda de libros en DemoQA Books
  Quiero buscar libros por palabra clave
  para encontrar rápido los que me interesan.

  @busqueda_existente
  Esquema del escenario: Búsqueda con una palabra clave que existe en el catálogo
    Dado que estoy en la lista de libros
    Cuando busco la palabra clave "<palabra>"
    Entonces todos los libros que veo coinciden con "<palabra>"
    Y el resultado tiene al menos un libro

    Ejemplos: Palabras presentes en el título
      | palabra    |
      | JavaScript |
      | Git        |

    Ejemplos: Palabras presentes en otras columnas
      | palabra  |
      | Osmani   |
      | O'Reilly |

  @busqueda_inexistente
  Escenario: Búsqueda con una palabra clave que no existe en el catálogo
    Dado que estoy en la lista de libros
    Cuando busco la palabra clave "ISTQB Fundamentals"
    Entonces no veo ningún libro en el resultado
    Y el paginador indica "Page 1 of 0"
