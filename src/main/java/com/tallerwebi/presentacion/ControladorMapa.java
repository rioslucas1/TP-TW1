package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Materia;
import com.tallerwebi.dominio.Profesor;
import com.tallerwebi.dominio.ServicioMapa;
import com.tallerwebi.dominio.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ControladorMapa {

    private final ServicioMapa servicioMapa;

    @Autowired
    public ControladorMapa(ServicioMapa servicioMapa) {
        this.servicioMapa = servicioMapa;
    }

    @RequestMapping("/mapa")
    public ModelAndView irVerMapa() {
        ModelMap modelo = new ModelMap();

        List<Usuario> usuarios = servicioMapa.obtenerProfesores();

        List<DatosMapa> datosProfesores = usuarios.stream()
                .filter(usuario -> usuario.getProfesor() != null)
                .map(usuario -> {
                    Profesor profesor = usuario.getProfesor();
                    return new DatosMapa(
                            usuario.getNombre(),
                            usuario.getApellido(),
                            profesor.getMateria().toString(),
                            profesor.getLatitud(),
                            profesor.getLongitud()
                    );
                })
                .collect(Collectors.toList());

        modelo.put("datosProfesores", datosProfesores);
        return new ModelAndView("mapa", modelo);
    }
}
