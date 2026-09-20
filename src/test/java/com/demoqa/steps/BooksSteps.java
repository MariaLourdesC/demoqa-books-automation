package com.demoqa.steps;

import com.demoqa.pages.BooksPage;
import com.demoqa.support.TestContext;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BooksSteps {
    private final TestContext context;
    private BooksPage books;

    public BooksSteps(TestContext context) { this.context = context; }

    @Dado("que estoy en la lista de libros")
    public void openBooks() {
        books = new BooksPage(context.driver, context.config);
        books.open();
    }

    @Cuando("busco la palabra clave {string}")
    public void search(String keyword) {
        books.search(keyword);
    }

    // Este paso es el que espera a que el filtro termine. Comprueba la
    // relevancia, no solo que haya llegado algo: una búsqueda que devuelve
    // resultados que no corresponden está igual de rota que una que no devuelve
    // nada, y contar filas no lo detectaría.
    @Entonces("todos los libros que veo coinciden con {string}")
    public void verifyEveryResultMatches(String keyword) {
        boolean allMatch = books.waitForResultsMatching(keyword);
        assertTrue("Hay libros en el resultado que no coinciden con \"" + keyword
                + "\". Títulos obtenidos: " + books.resultTitles(), allMatch);
    }

    @Entonces("el resultado tiene al menos un libro")
    public void verifyThereIsAtLeastOneResult() {
        List<String> titles = books.resultTitles();
        assertTrue("La búsqueda no devolvió ningún libro.", books.resultCount() > 0);
        assertTrue("El resultado no muestra títulos: " + titles, !titles.isEmpty());
    }

    @Entonces("no veo ningún libro en el resultado")
    public void verifyNoResults() {
        boolean empty = books.waitForNoResults();
        assertTrue("Se esperaba un resultado vacío y aparecieron estos libros: "
                + books.resultTitles(), empty);
    }

    @Entonces("el paginador indica {string}")
    public void verifyPagination(String expected) {
        assertEquals("El paginador no refleja un resultado vacío.", expected, books.paginationText());
    }
}
