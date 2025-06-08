package com.tallerwebi.presentacion;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.entidades.disponibilidadProfesor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;

import com.tallerwebi.dominio.ServicioReservaAlumno;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ControladorReservaAlumno {

    private ServicioReservaAlumno servicioReservaAlumno;
    @Autowired
    public ControladorReservaAlumno(ServicioReservaAlumno servicioReservaAlumno) {
        this.servicioReservaAlumno = servicioReservaAlumno;
    }

    @GetMapping("/calendario-reserva")
    public ModelAndView verCalendarioProfesor(@RequestParam String emailProfesor, HttpServletRequest request) {
        ModelMap modelo = new ModelMap();
        List<String> disponibilidadesKeys = new ArrayList<>();
        Usuario usuario = obtenerUsuarioDeSesion(request);
        Map<String, String> estadosMap = new HashMap<>();

        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }
        if (!esAlumno(usuario)) {
            return new ModelAndView("redirect:/home");
        }
        try {
            List<disponibilidadProfesor> disponibilidades =
                    servicioReservaAlumno.obtenerDisponibilidadProfesor(emailProfesor);

            for (disponibilidadProfesor disp : disponibilidades) {
                String key = disp.getDiaSemana() + "-" + disp.getHora();
                disponibilidadesKeys.add(key);
                estadosMap.put(key, disp.getEstado().toString());
            }

            modelo.put("disponibilidades", disponibilidades);
            modelo.put("emailProfesor", emailProfesor);
            modelo.put("nombreUsuario", usuario.getNombre());
            modelo.put("estadosMap", estadosMap);
        } catch (Exception e) {
            System.err.println("Error al cargar calendario: " + e.getMessage());
        }
        modelo.put("disponibilidadesKeys", disponibilidadesKeys);
        return new ModelAndView("calendario-reserva", modelo);
    }

    private Usuario obtenerProfesorElegido(String emailProfesor) {
        return null;
    }

    private boolean esAlumno(Usuario usuario) {
        return usuario != null &&
                usuario.getRol() != null &&
                !"profesor".equals(usuario.getRol());

    }

    private Usuario obtenerUsuarioDeSesion(HttpServletRequest request) {
        try {
            if (request != null && request.getSession() != null) {
                return (Usuario) request.getSession().getAttribute("USUARIO");
            }
        } catch (Exception e) {
            System.err.println("Error al obtener usuario de sesión: " + e.getMessage());
        }
        return null;
    }

    @PostMapping("/reservar-clase")
    public ModelAndView reservarClase(@RequestParam("diaSemana") String diaSemana,
                                             @RequestParam("hora") String hora,
                                             HttpServletRequest request) {

        Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");
        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }

        if (!esAlumno(usuario)) {
            return new ModelAndView("redirect:/home");
        }


        String diaLimpio = diaSemana != null ? diaSemana.trim() : "";
        String horaLimpia = hora != null ? hora.trim() : "";



        try {
            servicioReservaAlumno.reservarClase(usuario.getEmail(), diaLimpio, horaLimpia);
        } catch (Exception e) {
            System.err.println("Error al procesar toggle disponibilidad: " + e.getMessage());
        }

        return new ModelAndView("redirect:/calendario-profesor");
    }
}
