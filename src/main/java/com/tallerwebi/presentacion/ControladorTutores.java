package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.servicios.ServicioTutores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class ControladorTutores {

    @Autowired
    private ServicioTutores servicioTutores;

    @RequestMapping("/verTutores")
    public ModelAndView verTutores(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String modalidad,
            @RequestParam(required = false) String duracion,
            @RequestParam(required = false) String busqueda
    ) {
        ModelMap modelo = new ModelMap();

        List<Profesor> tutoresFiltrados = servicioTutores.obtenerTutoresFiltrados(categoria, modalidad, duracion, busqueda);

        modelo.put("tutores", tutoresFiltrados);
        return new ModelAndView("verTutores", modelo);
    }

    @RequestMapping("/detallesTutores")
    public ModelAndView detallesTutores() {
        ModelMap modelo = new ModelMap();
        return new ModelAndView("detallesTutores", modelo);
    }
}
