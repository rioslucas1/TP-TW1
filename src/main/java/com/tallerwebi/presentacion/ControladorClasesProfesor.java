package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Clase;
import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.dominio.servicios.ServicioReservaAlumno;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.List;

@Controller
public class ControladorClasesProfesor {

    private ServicioReservaAlumno servicioReservaAlumno;

    @Autowired
    public ControladorClasesProfesor(ServicioReservaAlumno servicioReservaAlumno) {
        this.servicioReservaAlumno = servicioReservaAlumno;
    }

    @GetMapping("/profesor/todas-mis-clases") // Nueva URL para las clases del profesor
    @Transactional
    public ModelAndView verTodasMisClasesProfesor(HttpServletRequest request) { // (Reference to existing controller patterns for session and ModelMap)
        ModelMap modelo = new ModelMap();
        Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("USUARIO");

        if (usuarioLogueado == null) {
            return new ModelAndView("redirect:/login");
        }

        if (!(usuarioLogueado instanceof Profesor)) {
            return new ModelAndView("redirect:/home");
        }

        Profesor profesor = (Profesor) usuarioLogueado;
        List<Clase> todasLasClases = servicioReservaAlumno.obtenerTodasLasClasesPorProfesor(profesor.getId()); //

        modelo.put("todasLasClases", todasLasClases);
        modelo.put("nombreUsuario", profesor.getNombre());
        modelo.put("rol", "profesor");

        return new ModelAndView("todas-mis-clases", modelo); //
    }
}