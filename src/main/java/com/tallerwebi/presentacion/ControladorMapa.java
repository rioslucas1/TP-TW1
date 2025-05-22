package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Profesor;
import com.tallerwebi.dominio.ServicioMapa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ControladorMapa {

    @Autowired
    private ServicioMapa servicioMapa;

    @RequestMapping("/mapa")
    public ModelAndView irVerMapa() {
        ModelMap modelo = new ModelMap();

        List<Profesor> profesores = servicioMapa.obtenerProfesores();

        List<DatosMapa> datosProfesores = profesores.stream()
                .map(profesor -> new DatosMapa(
                        profesor.getNombre(),
                        profesor.getApellido(),
                        profesor.getMateria().toString(),
                        profesor.getLatitud(),
                        profesor.getLongitud()
                ))
                .collect(Collectors.toList());

        modelo.put("datosProfesores", datosProfesores);
        return new ModelAndView("mapa", modelo);
    }
}
