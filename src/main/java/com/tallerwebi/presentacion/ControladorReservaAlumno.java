package com.tallerwebi.presentacion;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.dominio.entidades.Clase;
import com.tallerwebi.dominio.servicios.ServicioMeet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;

import com.tallerwebi.dominio.servicios.ServicioReservaAlumno;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ControladorReservaAlumno {

    private static final String[] DIAS_SEMANA = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};

    @Autowired
    private ServicioReservaAlumno servicioReservaAlumno;

    @Autowired
    private ServicioMeet servicioMeet;


    @Autowired
    public ControladorReservaAlumno(ServicioReservaAlumno servicioReservaAlumno) {
        this.servicioReservaAlumno = servicioReservaAlumno;
    }
    @GetMapping("/calendario-reserva")
    public ModelAndView verCalendarioProfesor(
            @RequestParam String emailProfesor,
            @RequestParam(value = "semana", required = false) String semanaParam,
            HttpServletRequest request) {

        ModelMap modelo = new ModelMap();
        Usuario usuario = obtenerUsuarioDeSesion(request);

        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }
        if (!esAlumno(usuario)) {
            return new ModelAndView("redirect:/home");
        }


        Alumno alumno = (Alumno) usuario;
        if (!servicioReservaAlumno.estaSuscritoAProfesor(alumno.getId(), emailProfesor)) {
            modelo.put("error", "No tenes acceso al calendario de este profesor");
            return new ModelAndView("redirect:/home");
        }

        LocalDate fechaInicioSemana = calcularFechaInicioSemana(semanaParam);
        configurarFechasEnModelo(modelo, fechaInicioSemana);


        try {

            List<Clase> disponibilidades =
                    servicioReservaAlumno.obtenerDisponibilidadProfesorPorSemana(emailProfesor, fechaInicioSemana);
            List<String> disponibilidadesKeys = new ArrayList<>();
            Map<String, String> estadosMap = new HashMap<>();
            Map<String, Long> idsMap = new HashMap<>();


            for (Clase disp : disponibilidades) {
                String key = disp.getDiaSemana() + "-" + disp.getHora();
                disponibilidadesKeys.add(key);
                estadosMap.put(key, disp.getEstado().toString());
                idsMap.put(key, disp.getId());
            }

            modelo.put("disponibilidades", disponibilidades);
            modelo.put("disponibilidadesKeys", disponibilidadesKeys);
            modelo.put("emailProfesor", emailProfesor);
            modelo.put("nombreUsuario", usuario.getNombre());
            modelo.put("estadosMap", estadosMap);
            modelo.put("idsMap", idsMap);

        } catch (Exception e) {
            System.err.println("Error al cargar calendario: " + e.getMessage());
            modelo.put("disponibilidadesKeys", new ArrayList<>());
            modelo.put("estadosMap", new HashMap<>());
        }

        return new ModelAndView("calendario-reserva", modelo);
    }

    private boolean esAlumno(Usuario usuario) {
        return usuario instanceof Alumno;
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
    public ModelAndView reservarClase(@RequestParam("disponibilidadId") Long disponibilidadId,
                                      @RequestParam("emailProfesor") String emailProfesor,
                                      @RequestParam(value = "semanaActual", required = false) String semanaActualStr,
                                      HttpServletRequest request) {

        Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");
        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }

        if (!esAlumno(usuario)) {
            return new ModelAndView("redirect:/home");
        }


        try {
            Alumno alumno = (Alumno) usuario;
            servicioReservaAlumno.reservarClasePorId(disponibilidadId, alumno);
        } catch (Exception e) {
            System.err.println("Error al reservar clase: " + e.getMessage());
        }

        return crearRedirectConSemana("/calendario-reserva", emailProfesor, semanaActualStr);
    }

    private LocalDate calcularFechaInicioSemana(String semanaParam) {
        if (semanaParam != null && !semanaParam.isEmpty()) {
            try {
                return LocalDate.parse(semanaParam);
            } catch (Exception e) {
                System.err.println("Error parseando fecha: " + e.getMessage());
            }
        }
        return LocalDate.now().with(DayOfWeek.MONDAY);
    }

    private LocalDate calcularFechaCorrecta(String diaSemana, String semanaActualStr) {
        LocalDate inicioSemana;
        if (semanaActualStr != null && !semanaActualStr.trim().isEmpty()) {
            try {
                inicioSemana = LocalDate.parse(semanaActualStr);
            } catch (Exception e) {
                System.err.println("Error parseando semana actual: " + semanaActualStr);
                inicioSemana = LocalDate.now().with(DayOfWeek.MONDAY);
            }
        } else {
            inicioSemana = LocalDate.now().with(DayOfWeek.MONDAY);
        }
        return calcularFechaPorDiaSemanaEnSemana(diaSemana, inicioSemana);
    }

    private LocalDate calcularFechaPorDiaSemanaEnSemana(String diaSemana, LocalDate inicioSemana) {
        try {
            int diasAgregar;
            switch (diaSemana.toLowerCase()) {
                case "lunes": diasAgregar = 0; break;
                case "martes": diasAgregar = 1; break;
                case "miércoles": diasAgregar = 2; break;
                case "jueves": diasAgregar = 3; break;
                case "viernes": diasAgregar = 4; break;
                case "sábado": diasAgregar = 5; break;
                case "domingo": diasAgregar = 6; break;
                default:
                    System.err.println("Día de semana no válido: " + diaSemana);
                    return inicioSemana;
            }
            return inicioSemana.plusDays(diasAgregar);
        } catch (Exception e) {
            System.err.println("Error calculando fecha por día de semana: " + e.getMessage());
            return inicioSemana;
        }
    }

    private void configurarFechasEnModelo(ModelMap modelo, LocalDate fechaInicioSemana) {
        DateTimeFormatter formatoCorto = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter formatoCompleto = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String fechaInicioFormateada = fechaInicioSemana.format(formatoCorto);
        String fechaFinFormateada = fechaInicioSemana.plusDays(6).format(formatoCompleto);

        List<String> diasConFecha = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate fechaDia = fechaInicioSemana.plusDays(i);
            String fechaFormateada = fechaDia.format(formatoCorto);
            diasConFecha.add(DIAS_SEMANA[i] + " " + fechaFormateada);
        }

        Map<String, String> diasConFechas = new HashMap<>();
        Map<String, Boolean> diasPasados = new HashMap<>();
        LocalDate hoy = LocalDate.now();

        for (int i = 0; i < 7; i++) {
            LocalDate fechaDia = fechaInicioSemana.plusDays(i);
            diasConFechas.put(DIAS_SEMANA[i], fechaDia.toString());
            diasPasados.put(DIAS_SEMANA[i], fechaDia.isBefore(hoy));
        }

        modelo.put("fechaInicioSemana", fechaInicioSemana);
        modelo.put("fechaInicioFormateada", fechaInicioFormateada);
        modelo.put("fechaFinFormateada", fechaFinFormateada);
        modelo.put("diasConFecha", diasConFecha);
        modelo.put("diasConFechas", diasConFechas);
        modelo.put("diasPasados", diasPasados);
    }

    private ModelAndView crearRedirectConSemana(String url, String emailProfesor, String semanaActualStr) {
        String redirectUrl = "redirect:" + url + "?emailProfesor=" + emailProfesor;
        if (semanaActualStr != null && !semanaActualStr.trim().isEmpty()) {
            redirectUrl += "&semana=" + semanaActualStr;
        }
        return new ModelAndView(redirectUrl);
    }

    @GetMapping("/obtener-link-meet")
    @ResponseBody
    public String obtenerLinkMeet(@RequestParam Long disponibilidadId) {
        Clase disponibilidad = servicioReservaAlumno.obtenerDisponibilidadPorId(disponibilidadId);
        return disponibilidad != null ? disponibilidad.getEnlace_meet() : null;
    }


    @GetMapping("/clases-intercambio")
    public ModelAndView verClasesEntreProfesorYAlumno(
            @RequestParam("emailProfesor") String emailProfesor,
            @RequestParam("emailAlumno") String emailAlumno,
            HttpServletRequest request) {

        ModelMap modelo = new ModelMap();
        Usuario usuarioLogeado = obtenerUsuarioDeSesion(request);

        if (usuarioLogeado == null) {
            return new ModelAndView("redirect:/login");
        }


        if (!(usuarioLogeado.getEmail().equals(emailProfesor) || usuarioLogeado.getEmail().equals(emailAlumno))) {
            modelo.put("error", "No tienes permiso para ver estas clases.");
            return new ModelAndView("redirect:/home", modelo);
        }

        List<Clase> clases = null;
        try {
            clases = servicioReservaAlumno.obtenerClasesPorProfesorYAlumno(emailProfesor, emailAlumno);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (Clase clase : clases) {
                if (clase.getFechaEspecifica() != null) {
                    clase.setFechaFormateada(clase.getFechaEspecifica().format(formatter));
                } else {
                    clase.setFechaFormateada("N/D");
                }
            }
            modelo.put("clases", clases);
            modelo.put("emailProfesor", emailProfesor);
            modelo.put("emailAlumno", emailAlumno);
            modelo.put("nombreUsuario", usuarioLogeado.getNombre());
            modelo.put("rol", usuarioLogeado.getRol());

        } catch (Exception e) {
            System.err.println("Error al obtener clases de intercambio: " + e.getMessage());
            modelo.put("error", "Error al cargar las clases. Inténtalo de nuevo más tarde.");
            for (Clase clase : clases) {
                System.out.println("DEBUG - Clase ID: " + clase.getId() +
                        " | enlace_meet: " + clase.getEnlace_meet());
            }
            return new ModelAndView("error", modelo);
        }
        System.out.println("DEBUG - rol en modelo: " + usuarioLogeado.getRol());


        return new ModelAndView("clases-intercambio", modelo);
    }


    @PostMapping("/clase/{id}/crear-reunion-meet")
    public ModelAndView crearReunionMeet(@PathVariable Long id,
                                         @RequestParam("emailProfesor") String emailProfesor,
                                         @RequestParam("emailAlumno") String emailAlumno,
                                         HttpServletRequest request) {
        Usuario usuarioLogeado = obtenerUsuarioDeSesion(request);

        if (usuarioLogeado == null || !usuarioLogeado.getEmail().equals(emailProfesor)) {
            return new ModelAndView("redirect:/login");
        }

        try {
            Clase clase = servicioReservaAlumno.obtenerDisponibilidadPorId(id);

            if (clase != null && clase.getEnlace_meet() == null) {
                String enlaceMeet = servicioMeet.crearReunionGoogleMeet(clase);
                clase.setEnlace_meet(enlaceMeet);
                servicioReservaAlumno.actualizarClase(clase);
            }
        } catch (Exception e) {
            System.err.println("Error al crear reunión Meet: " + e.getMessage());
        }

        return new ModelAndView("redirect:/clases-intercambio?emailProfesor=" + emailProfesor + "&emailAlumno=" + emailAlumno);
    }




}
