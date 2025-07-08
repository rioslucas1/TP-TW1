package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.dominio.servicios.ServicioMapa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ControladorMapa {

    private ServicioMapa servicioMapa;

    @Autowired
    public ControladorMapa(ServicioMapa servicioMapa) {
        this.servicioMapa = servicioMapa;
    }

    @RequestMapping("/mapa")
    public ModelAndView irVerMapa(HttpSession session) { // <-- agregamos HttpSession
        ModelMap modelo = new ModelMap();

        // Agregar nombreUsuario si hay usuario logueado
        Usuario usuario = (Usuario) session.getAttribute("USUARIO");
        if (usuario != null) {
            modelo.put("nombreUsuario", usuario.getNombre());
        }

        List<Profesor> profesores = servicioMapa.obtenerProfesores();
        if (profesores == null) {
            modelo.put("datosProfesores", List.of());
            modelo.put("mensaje", "No se encontraron profesores para mostrar en el mapa.");
            return new ModelAndView("mapa", modelo);
        }

        List<DatosMapa> datosProfesores = profesores.stream()
                .map(profesor -> new DatosMapa(
                        profesor.getNombre(),
                        profesor.getApellido(),
                        profesor.getTema().getNombre(),
                        profesor.getLatitud(),
                        profesor.getLongitud()
                ))
                .collect(Collectors.toList());

        modelo.put("datosProfesores", datosProfesores);
        return new ModelAndView("mapa", modelo);
    }
}