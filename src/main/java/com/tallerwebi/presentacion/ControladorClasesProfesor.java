package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Clase;
import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.dominio.entidades.EstadoAsistencia;
import com.tallerwebi.dominio.servicios.ServicioReservaAlumno;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public ModelAndView verTodasMisClasesProfesor(HttpServletRequest request) {
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
        modelo.put("estadosAsistencia", EstadoAsistencia.values());

        return new ModelAndView("todas-mis-clases", modelo); //
    }

    @PostMapping("/profesor/actualizar-asistencia")
    @Transactional
    public ModelAndView actualizarAsistenciaClase(@RequestParam("claseId") Long claseId,
                                                  @RequestParam("estadoAsistencia") String estadoAsistencia,
                                                  HttpServletRequest request) {
        Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("USUARIO");

        if (usuarioLogueado == null || !(usuarioLogueado instanceof Profesor)) {
            return new ModelAndView("redirect:/login");
        }

        try {
            EstadoAsistencia nuevoEstado = EstadoAsistencia.valueOf(estadoAsistencia.toUpperCase());
            servicioReservaAlumno.actualizarEstadoAsistenciaClase(claseId, nuevoEstado);
        } catch (Exception e) {
            System.err.println("Error al actualizar la asistencia: " + e.getMessage());
        }

        return new ModelAndView("redirect:/profesor/todas-mis-clases");
    }

}