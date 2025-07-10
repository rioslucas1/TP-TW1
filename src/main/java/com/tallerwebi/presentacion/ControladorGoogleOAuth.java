package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.servicios.ServicioMeet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ControladorGoogleOAuth {

    @Autowired
    private ServicioMeet servicioMeet;


    @GetMapping("/oauth2callback")
    public String callbackOAuth(@RequestParam String code) {
        servicioMeet.intercambiarCodigoPorTokens(code);
        return "redirect:/clases-intercambio";
    }
}