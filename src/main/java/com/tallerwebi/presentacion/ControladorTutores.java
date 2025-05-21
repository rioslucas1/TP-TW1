package com.tallerwebi.presentacion;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ControladorTutores {


    @RequestMapping("/verTutores")
    public String verTutores() {
        return "verTutores";
    }

    @RequestMapping("/detallesTutores")
    public String detallesTutores() {
        return "detallesTutores";
    }
}
