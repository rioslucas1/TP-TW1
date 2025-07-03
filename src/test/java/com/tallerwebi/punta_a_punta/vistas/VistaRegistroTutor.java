package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.Position;

public class VistaRegistroTutor extends VistaWeb {

    public VistaRegistroTutor(Page page) {
        super(page);
    }


    public void completarNombre(String nombre) {
        escribirEnElElemento("#nombre", nombre);
    }

    public void completarApellido(String apellido) {
        escribirEnElElemento("#apellido", apellido);
    }

    public void completarEmail(String email) {
        escribirEnElElemento("#email", email);
    }

    public void completarPassword(String password) {
        escribirEnElElemento("#password", password);
    }

    public void seleccionarTemaPorIndice(int indice) {
        page.locator("#tema").selectOption(new String[]{String.valueOf(indice)});
    }

    public void clickearEnMapa() {
        page.locator("#map").click(new Locator.ClickOptions().setPosition(new Position(100, 100)));
    }

    public void enviarFormulario() {
        darClickEnElElemento("#btn-login");
    }

    public String obtenerTextoDeLaBarraDeNavegacion() {
        return obtenerTextoDelElemento(".navbar-brand");
    }

    public String obtenerTextoDeAlertaError() {
        Locator alerta = page.locator(".alert-danger");
        alerta.waitFor(); // espera que el mensaje aparezca
        return alerta.textContent();
    }

    public Page getPage() {
        return this.page;
    }
}