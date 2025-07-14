package com.tallerwebi.punta_a_punta.vistas;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class VistaLogin extends VistaWeb {

    public VistaLogin(Page page) {
        super(page);
        page.navigate("http://localhost:8080/spring/login");
    }

     public String obtenerTextoDeLaBarraDeNavegacion(){
        return this.obtenerTextoDelElemento("navbar-brand");
    }
    public String obtenerMensajeDeError() {
        page.waitForSelector("p.alert.alert-danger"); // asegurar que exista
        return this.obtenerTextoDelElemento("p.alert.alert-danger");
    }

    public void escribirEMAIL(String email) {
        page.waitForSelector("#email");
        this.escribirEnElElemento("#email", email);
    }

    public void escribirClave(String clave) {
        page.waitForSelector("#password");
        this.escribirEnElElemento("#password", clave);
    }

    public void darClickEnIniciarSesion() {
        page.waitForSelector(".btn-form-submit");
        this.darClickEnElElemento(".btn-form-submit");
    }

    public void iniciarSesion(String email, String clave) {
        escribirEMAIL(email);
        escribirClave(clave);
        darClickEnIniciarSesion();
    }
}
