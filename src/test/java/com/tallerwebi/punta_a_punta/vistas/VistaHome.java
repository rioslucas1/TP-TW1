package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaHome extends VistaWeb {

    public VistaHome(Page page) {
        super(page);
        page.navigate("http://localhost:8080/spring/home");
    }

    public String obtenerTextoNavbar() {
        return this.obtenerTextoDelElemento("a.navbar-brand");
    }

    public String obtenerTituloPrincipal() {
        return this.obtenerTextoDelElemento("h1.main-title");
    }

    public boolean botonIniciarSesionVisible() {
        return this.esVisible("a.btn-iniciosesion");
    }

    public boolean botonRegistrarseVisible() {
        return this.esVisible("a.btn-registro");
    }

    public void clickEnEmpezar() {
        page.locator("a.btn-inicio").first().click();
    }

    public String obtenerSaludoUsuario() {
        return this.obtenerTextoDelElemento("span.me-3");
    }

    public String obtenerTituloDashboard() {
        return this.obtenerTextoDelElemento("h2");
    }
}