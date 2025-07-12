package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.VistaRegistroTutor;
import org.junit.jupiter.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

public class VistaRegistroTutorE2E {

    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    VistaRegistroTutor vistaRegistroTutor;

    @BeforeAll
    static void abrirNavegador() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)
                .setSlowMo(500));
    }

    @AfterAll
    static void cerrarNavegador() {
        playwright.close();
    }

    @BeforeEach
    void crearContextoYPagina() {
        context = browser.newContext();
        Page page = context.newPage();
        page.navigate("http://localhost:8080/spring/registrarprofesor"); // navega directamente en el test
        vistaRegistroTutor = new VistaRegistroTutor(page);
    }

    @AfterEach
    void cerrarContexto() {
        context.close();
    }

    @Test
    void deberiaMostrarElTituloClasesYaEnElNavbar() {
        String texto = vistaRegistroTutor.obtenerTextoDeLaBarraDeNavegacion();
        assertThat("ClasesYa", equalToIgnoringCase(texto));
    }

    @Test
    void deberiaRegistrarNuevoTutorCorrectamente() {
        vistaRegistroTutor.completarNombre("Angel");
        vistaRegistroTutor.completarApellido("Leyes");
        vistaRegistroTutor.completarEmail("Angel" + "@unlam.edu.ar");
        vistaRegistroTutor.completarPassword("123");
        vistaRegistroTutor.seleccionarTemaPorIndice(1);
        vistaRegistroTutor.clickearEnMapa();
        vistaRegistroTutor.enviarFormulario();

        String urlActual = vistaRegistroTutor.obtenerURLActual();
        assertThat(urlActual, containsStringIgnoringCase("http://localhost:8080/spring/login"));
    }

     @Test
    void deberiaMostrarErrorSiNoSeCompletaElFormulario() {
        vistaRegistroTutor.enviarFormulario();

        String textoError = vistaRegistroTutor.obtenerTextoDeAlertaError();

        assertThat(textoError, containsStringIgnoringCase("Todos los campos son obligatorios"));
    }
}