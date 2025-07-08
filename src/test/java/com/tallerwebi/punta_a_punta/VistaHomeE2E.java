package com.tallerwebi.punta_a_punta;

import com.microsoft.playwright.*;
import com.tallerwebi.punta_a_punta.vistas.VistaHome;
import org.junit.jupiter.api.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Page.LocatorOptions;

public class VistaHomeE2E {

    static Playwright playwright;
    static Browser browser;
    BrowserContext context;
    Page page;
    VistaHome vistaHome;
    private static final String BASE_URL = "http://localhost:8080/spring";

    @BeforeAll
    static void abrirNavegador() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(700));
    }

    @AfterAll
    static void cerrarNavegador() {
        playwright.close();
    }

    @BeforeEach
    void preparar() {
        context = browser.newContext();
        page = context.newPage();
        vistaHome = new VistaHome(page);
    }

    @AfterEach
    void cerrar() {
        context.close();
    }

    @Test
    void deberiaMostrarClasesYaEnNavbar() {
        String texto = vistaHome.obtenerTextoNavbar();
        assertThat(texto, containsStringIgnoringCase("ClasesYa"));
    }

    @Test
    void deberiaMostrarTituloPrincipal() {
        String titulo = vistaHome.obtenerTituloPrincipal();
        assertThat(titulo, containsStringIgnoringCase("Bienvenido a"));
    }

    @Test
    void deberiaMostrarBotonIniciarSesion() {
        assertThat(vistaHome.botonIniciarSesionVisible(), equalTo(true));
    }

    @Test
    void deberiaMostrarBotonRegistrarse() {
        assertThat(vistaHome.botonRegistrarseVisible(), equalTo(true));
    }

    @Test
    void alHacerClickEnEmpezarRedirigeALogin() {
        vistaHome.clickEnEmpezar();
        String urlActual = page.url();
        assertThat(urlActual, containsStringIgnoringCase("/login"));
    }

   /* @Test
    void alCargarLaHomeMuestraTituloBienvenidaCorrecto() {
        page.navigate(BASE_URL + "/");
        Locator titulo = page.locator("h2.titulo-home:has-text('Conectando estudiantes con los mejores profesores')");
        assertTrue(titulo.isVisible());
    }

    @Test
    void alCargarLaHomeMuestraBotonesEmpezar() {
        page.navigate(BASE_URL + "/");
        Locator botones = page.locator("a.btn-inicio");
        assertEquals(2, botones.count());
        assertTrue(botones.first().isVisible());
        assertTrue(botones.nth(1).isVisible());
    }

    @Test
    void alHacerClickEnPrimerBotonEmpezarRedirigeALogin() {
        page.navigate(BASE_URL + "/");
        page.locator("a.btn-inicio").first().click();
        assertThat(page.url(), containsStringIgnoringCase("/login"));
    }

    @Test
    void alHacerClickEnSegundoBotonEmpezarRedirigeAVerTutores() {
        page.navigate(BASE_URL + "/");
        page.locator("a.btn-inicio").nth(1).click();
        assertThat(page.url(), containsStringIgnoringCase("/ver-tutores"));
    }

    @Test
    void alCargarLaHomeMuestraSeccionSobreNosotros() {
        page.navigate(BASE_URL + "/");
        Locator sobreNosotros = page.locator(".features-section");
        assertTrue(sobreNosotros.isVisible());
    }

   /* @Test
    void alCargarLaHomeMuestraElFooterConEquipo() {
        page.navigate(BASE_URL + "/");
        page.locator("footer").scrollIntoViewIfNeeded();
        Locator equipo = page.locator(".team-member");
        assertTrue(equipo.first().isVisible());
    }*/
}