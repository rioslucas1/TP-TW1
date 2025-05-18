package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioMapa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class ControladorMapa {
@Autowired
private ServicioMapa servicioMapa;

    @RequestMapping("/mapa")
    public ModelAndView irVerMapa() {

        ModelMap modelo = new ModelMap();
        List<String> profesores = servicioMapa.obtenerProfesores();
        modelo.put("datosProfesores", profesores);
        return new ModelAndView("mapa", modelo);
    }
}
