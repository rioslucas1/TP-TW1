package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.ServicioDisponibilidadProfesor;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.entidades.disponibilidadProfesor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ControladorDisponibilidad {

    private ServicioDisponibilidadProfesor servicioDisponibilidadProfesor;

    @Autowired
    public ControladorDisponibilidad(ServicioDisponibilidadProfesor servicioDisponibilidadProfesor) {
        this.servicioDisponibilidadProfesor = servicioDisponibilidadProfesor;
    }

    @GetMapping("/calendario-profesor")
    public ModelAndView irACalendarioProfesor(HttpServletRequest request) {
            ModelMap modelo = new ModelMap();
            List<String> disponibilidadesKeys = new ArrayList<>();
            Usuario usuario = obtenerUsuarioDeSesion(request);
            Map<String, String> estadosMap = new HashMap<>();

            if (usuario == null) {
                return new ModelAndView("redirect:/login");
            }
            if (!esProfesor(usuario)) {
                return new ModelAndView("redirect:/home");
            }
              try {
                    List<disponibilidadProfesor> disponibilidades =
                            servicioDisponibilidadProfesor.obtenerDisponibilidadProfesor(usuario.getEmail());

                    for (disponibilidadProfesor disp : disponibilidades) {
                        String key = disp.getDiaSemana() + "-" + disp.getHora();
                        disponibilidadesKeys.add(key);
                        estadosMap.put(key, disp.getEstado().toString());
                    }

                    modelo.put("disponibilidades", disponibilidades);
                    modelo.put("emailProfesor", usuario.getEmail());
                    modelo.put("nombreUsuario", usuario.getNombre());
                   modelo.put("estadosMap", estadosMap);
                } catch (Exception e) {
                System.err.println("Error al cargar calendario: " + e.getMessage());
            }

            modelo.put("disponibilidadesKeys", disponibilidadesKeys);
            return new ModelAndView("calendario-profesor", modelo);
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

    private boolean esProfesor(Usuario usuario) {
        return usuario != null &&
                usuario.getRol() != null &&
                "profesor".equals(usuario.getRol());


    }

    private boolean sonParametrosValidos(String diaSemana, String hora) {
        if (diaSemana == null || hora == null || diaSemana.isEmpty() || hora.isEmpty()) {
            return false;
        }

        String[] diasValidos = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        boolean esDiaValido = false;
        for (String dia : diasValidos) {
            if (dia.equals(diaSemana)) {
                esDiaValido = true;
                break;
            }
        }

        boolean horaValida = hora.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");

        return esDiaValido && horaValida;
    }

    @PostMapping("/toggle-disponibilidad")
    public ModelAndView toggleDisponibilidad(@RequestParam("diaSemana") String diaSemana,
                                             @RequestParam("hora") String hora,
                                             HttpServletRequest request) {

        Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");
        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }

        if (!esProfesor(usuario)) {
            return new ModelAndView("redirect:/home");
        }


        String diaLimpio = diaSemana != null ? diaSemana.trim() : "";
        String horaLimpia = hora != null ? hora.trim() : "";


        if (!sonParametrosValidos(diaLimpio, horaLimpia)) {
            System.err.println("Parámetros inválidos - Día: " + diaLimpio + ", Hora: " + horaLimpia);
            return new ModelAndView("redirect:/calendario-profesor");
        }

        try {
            servicioDisponibilidadProfesor.toggleDisponibilidad(usuario.getEmail(), diaLimpio, horaLimpia);
        } catch (Exception e) {
            System.err.println("Error al procesar toggle disponibilidad: " + e.getMessage());
        }

        return new ModelAndView("redirect:/calendario-profesor");
}

        @PostMapping("/reservar-horario")
        public ModelAndView reservarHorario(@RequestParam("diaSemana") String diaSemana,
                                            @RequestParam("hora") String hora,
                                            HttpServletRequest request) {
            Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");
            if (usuario == null) {
                return new ModelAndView("redirect:/login");
            }

            if (!esProfesor(usuario)) {
                return new ModelAndView("redirect:/home");
            }

            String diaLimpio = diaSemana != null ? diaSemana.trim() : "";
            String horaLimpia = hora != null ? hora.trim() : "";

            if (!sonParametrosValidos(diaLimpio, horaLimpia)) {
                return new ModelAndView("redirect:/calendario-profesor");
            }

            try {
                servicioDisponibilidadProfesor.reservarHorario(usuario.getEmail(), diaLimpio, horaLimpia);
            } catch (Exception e) {
                System.err.println("Error al reservar horario: " + e.getMessage());
            }

            return new ModelAndView("redirect:/calendario-profesor");
        }

        @PostMapping("/desagendar-horario")
        public ModelAndView desagendarHorario(@RequestParam("diaSemana") String diaSemana,
                                              @RequestParam("hora") String hora,
                                              HttpServletRequest request) {
            Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");
            if (usuario == null) {
                return new ModelAndView("redirect:/login");
            }

            if (!esProfesor(usuario)) {
                return new ModelAndView("redirect:/home");
            }

            String diaLimpio = diaSemana != null ? diaSemana.trim() : "";
            String horaLimpia = hora != null ? hora.trim() : "";

            if (!sonParametrosValidos(diaLimpio, horaLimpia)) {
                return new ModelAndView("redirect:/calendario-profesor");
            }

            try {
                servicioDisponibilidadProfesor.desagendarHorario(usuario.getEmail(), diaLimpio, horaLimpia);
            } catch (Exception e) {
                System.err.println("Error al desagendar horario: " + e.getMessage());
            }

            return new ModelAndView("redirect:/calendario-profesor");
        }
}
