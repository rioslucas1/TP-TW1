package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Page;

public class VistaHome extends VistaWeb {

     public VistaHome(Page page) {
        super(page);
        page.navigate("http://localhost:8080/spring/home");
        // Dar tiempo para que la página cargue completamente
        page.waitForLoadState();
    }

    public String obtenerTextoNavbar() {
        return this.obtenerTextoDelElemento(".nav-logo-text");
    }

    public String obtenerTituloPrincipal() {
        return this.obtenerTextoDelElemento(".hero-title");
    }

    public boolean botonIniciarSesionVisible() {
        return this.esVisible(".btn-login");
    }

    public boolean botonRegistrarseVisible() {
        return this.esVisible(".btn-register");
    }

    public void clickEnEmpezar() {
        page.locator(".btn-hero").click();
    }

    public String obtenerSaludoUsuario() {
        return this.obtenerTextoDelElemento(".user-greeting");
    }

    public String obtenerTituloDashboard() {
        return this.obtenerTextoDelElemento(".display-5.fw-bold");
    }
}