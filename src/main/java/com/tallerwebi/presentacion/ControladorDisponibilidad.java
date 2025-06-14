package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.servicios.ServicioDisponibilidadProfesor;
import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.disponibilidadProfesor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.time.format.DateTimeFormatter;



@Controller
public class ControladorDisponibilidad {


    private static final String[] DIAS_SEMANA = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
    private ServicioDisponibilidadProfesor servicioDisponibilidadProfesor;

    @Autowired
    public ControladorDisponibilidad(ServicioDisponibilidadProfesor servicioDisponibilidadProfesor) {
        this.servicioDisponibilidadProfesor = servicioDisponibilidadProfesor;
    }
    @GetMapping("/calendario-profesor")
    public ModelAndView irACalendarioProfesor(
            @RequestParam(value = "semana", required = false) String semanaParam,
            HttpServletRequest request) {

        ModelMap modelo = new ModelMap();
        Usuario usuario = obtenerUsuarioDeSesion(request);

        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }
        if (!esProfesor(usuario)) {
            return new ModelAndView("redirect:/home");
        }

        LocalDate fechaInicioSemana = calcularFechaInicioSemana(semanaParam);
        configurarFechasEnModelo(modelo, fechaInicioSemana);

        try {
            List<disponibilidadProfesor> disponibilidades =
                    servicioDisponibilidadProfesor.obtenerDisponibilidadProfesorPorSemana(
                            usuario.getEmail(), fechaInicioSemana);

            List<String> disponibilidadesKeys = new ArrayList<>();
            Map<String, String> estadosMap = new HashMap<>();

            for (disponibilidadProfesor disp : disponibilidades) {
                String key = disp.getDiaSemana() + "-" + disp.getHora();
                disponibilidadesKeys.add(key);
                estadosMap.put(key, disp.getEstado().toString());
            }

            modelo.put("disponibilidades", disponibilidades);
            modelo.put("disponibilidadesKeys", disponibilidadesKeys);
            modelo.put("emailProfesor", usuario.getEmail());
            modelo.put("nombreUsuario", usuario.getNombre());
            modelo.put("estadosMap", estadosMap);

        } catch (Exception e) {
            System.err.println("Error al cargar calendario: " + e.getMessage());
        }

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
        return usuario instanceof Profesor;
    }

    private boolean sonParametrosValidos(String diaSemana, String hora) {
        if (diaSemana == null || hora == null || diaSemana.isEmpty() || hora.isEmpty()) {
            return false;
        }
        boolean esDiaValido = false;
        for (String dia : DIAS_SEMANA) {
            if (dia.equals(diaSemana)) {
                esDiaValido = true;
                break;
            }
        }
        boolean horaValida = hora.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");
        return esDiaValido && horaValida;
    }

    @PostMapping("/toggle-disponibilidad")
    public ModelAndView toggleDisponibilidad(
            @RequestParam("diaSemana") String diaSemana,
            @RequestParam("hora") String hora,
            @RequestParam(value = "fechaEspecifica", required = false) String fechaEspecificaStr,
            @RequestParam(value = "semanaActual", required = false) String semanaActualStr,
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
            return crearRedirectConSemana("/calendario-profesor", semanaActualStr);
        }

        LocalDate fechaEspecifica;
        if (fechaEspecificaStr != null && !fechaEspecificaStr.trim().isEmpty()) {
            try {
                fechaEspecifica = LocalDate.parse(fechaEspecificaStr);
            } catch (Exception e) {
                System.err.println("Error parseando fecha específica: " + fechaEspecificaStr + ", error: " + e.getMessage());
                fechaEspecifica = calcularFechaCorrecta(diaLimpio, semanaActualStr);
            }
        } else {
            fechaEspecifica = calcularFechaCorrecta(diaLimpio, semanaActualStr);
        }

        try {
            servicioDisponibilidadProfesor.toggleDisponibilidadConFecha(
                    usuario.getEmail(), diaLimpio, horaLimpia, fechaEspecifica);
        } catch (Exception e) {
            System.err.println("Error al procesar toggle disponibilidad: " + e.getMessage());
        }

        return crearRedirectConSemana("/calendario-profesor", semanaActualStr);
    }

    @PostMapping("/reservar-horario")
    public ModelAndView reservarHorario(
            @RequestParam("diaSemana") String diaSemana,
            @RequestParam("hora") String hora,
            @RequestParam(value = "semanaActual", required = false) String semanaActualStr,
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
            return crearRedirectConSemana("/calendario-profesor", semanaActualStr);
        }

        try {
            servicioDisponibilidadProfesor.reservarHorario(usuario.getEmail(), diaLimpio, horaLimpia);
        } catch (Exception e) {
            System.err.println("Error al reservar horario: " + e.getMessage());
        }

        return crearRedirectConSemana("/calendario-profesor", semanaActualStr);
    }

    @PostMapping("/desagendar-horario")
    public ModelAndView desagendarHorario(
            @RequestParam("diaSemana") String diaSemana,
            @RequestParam("hora") String hora,
            @RequestParam(value = "semanaActual", required = false) String semanaActualStr,
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
            return crearRedirectConSemana("/calendario-profesor", semanaActualStr);
        }

        try {
            servicioDisponibilidadProfesor.desagendarHorario(usuario.getEmail(), diaLimpio, horaLimpia);
        } catch (Exception e) {
            System.err.println("Error al desagendar horario: " + e.getMessage());
        }

        return crearRedirectConSemana("/calendario-profesor", semanaActualStr);
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

    private List<String> generarFechasSemanales(LocalDate fechaInicio) {
        List<String> fechas = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate fecha = fechaInicio.plusDays(i);
            fechas.add(fecha.toString());
        }
        return fechas;
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
        for (int i = 0; i < 7; i++) {
            LocalDate fechaDia = fechaInicioSemana.plusDays(i);
            diasConFechas.put(DIAS_SEMANA[i], fechaDia.toString());
        }

        modelo.put("fechaInicioSemana", fechaInicioSemana);
        modelo.put("fechaInicioFormateada", fechaInicioFormateada);
        modelo.put("fechaFinFormateada", fechaFinFormateada);
        modelo.put("diasConFecha", diasConFecha);
        modelo.put("fechasSemanales", generarFechasSemanales(fechaInicioSemana));
        modelo.put("diasConFechas", diasConFechas);
    }

    private ModelAndView crearRedirectConSemana(String url, String semanaActualStr) {
        String redirectUrl = "redirect:" + url;
        if (semanaActualStr != null && !semanaActualStr.trim().isEmpty()) {
            redirectUrl += "?semana=" + semanaActualStr;
        }
        return new ModelAndView(redirectUrl);
    }

}