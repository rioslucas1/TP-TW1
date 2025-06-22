package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.dominio.servicios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.List;

@Controller
public class ControladorTutores {

    private RepositorioUsuario repositorioUsuario;
    private ServicioFeedback servicioFeedback;
    private ServicioExperiencia servicioExperiencia;
    private ServicioLogin servicioLogin;
    private ServicioTema servicioTema;
    private ServicioSuscripcion servicioSuscripcion;

    @Autowired
    public ControladorTutores(RepositorioUsuario repositorioUsuario,
                              ServicioFeedback servicioFeedback,
                              ServicioExperiencia servicioExperiencia,
                              ServicioLogin servicioLogin,
                              ServicioTema servicioTema,
                              ServicioSuscripcion servicioSuscripcion) {
        this.repositorioUsuario = repositorioUsuario;
        this.servicioFeedback = servicioFeedback;
        this.servicioExperiencia = servicioExperiencia;
        this.servicioLogin = servicioLogin;
        this.servicioTema = servicioTema;
        this.servicioSuscripcion = servicioSuscripcion;
    }

    @RequestMapping("/verTutores")
    public ModelAndView verTutores(HttpServletRequest request) {
        ModelMap modelo = new ModelMap();

        try {
            List<Usuario> listaProfesores = servicioLogin.obtenerProfesores();
            Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("USUARIO");
            String nombreUsuario = usuarioLogueado != null ? usuarioLogueado.getNombre() : null;

            modelo.put("listaProfesores", listaProfesores);
            modelo.put("nombreUsuario", nombreUsuario);

            return new ModelAndView("verTutores", modelo);
        } catch (Exception e) {
            e.printStackTrace();
            modelo.put("error", "Error al cargar los tutores");
            return new ModelAndView("verTutores", modelo);
        }
    }

    @RequestMapping("/verPerfilDeProfesor")
    @Transactional
    public ModelAndView verPerfilDeProfesor(@RequestParam String email,
                                            HttpServletRequest request) {
        ModelMap modelo = new ModelMap();

        try {
            Profesor profesor = (Profesor) repositorioUsuario.buscar(email);

            if (profesor == null) {
                return new ModelAndView("redirect:/verTutores");
            }

            List<ExperienciaEstudiantil> experiencias = servicioExperiencia.obtenerExperienciasPorProfesor(profesor.getId());
            List<FeedbackProfesor> feedbacks = servicioFeedback.obtenerFeedbacksPorProfesor(profesor.getId());

            Double promedioCalificacion = servicioFeedback.calcularPromedioCalificacion(profesor.getId());
            Integer totalFeedback = servicioFeedback.contarFeedbackPorProfesor(profesor.getId());

            Alumno usuarioLogueado = (Alumno) request.getSession().getAttribute("USUARIO");
            String nombreUsuario = usuarioLogueado != null ? usuarioLogueado.getNombre() : null;
            boolean suscripto = usuarioLogueado != null ? servicioSuscripcion.estaAlumnoSuscritoAProfesor(usuarioLogueado.getId(), profesor.getId()) : false;

            modelo.put("profesor", profesor);
            modelo.put("feedbacks", feedbacks);
            modelo.put("experiencias", experiencias);
            modelo.put("promedioCalificacion", promedioCalificacion != null ? promedioCalificacion : 0.0);
            modelo.put("totalResenas", totalFeedback != null ? totalFeedback : 0);
            modelo.put("nombreUsuario", nombreUsuario);
            modelo.put("esVistaPublica", true);
            modelo.put("yaSuscripto", suscripto);

            return new ModelAndView("verPerfilDeProfesor", modelo);

        } catch (Exception e) {
            e.printStackTrace();
            return new ModelAndView("redirect:/verTutores");
        }
    }

    @RequestMapping(value = "/suscribirseAProfesor", method = RequestMethod.POST)
    public ModelAndView suscribirseAProfesor(@RequestParam Long profesorId, HttpServletRequest request) {
        ModelMap modelo = new ModelMap();

        try {
            Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("USUARIO");

            if (usuarioLogueado == null) {
                modelo.put("error", "Debes iniciar sesión para suscribirte a un profesor");
                return new ModelAndView("login", modelo);
            }

            if (!(usuarioLogueado instanceof Alumno)) {
                modelo.put("error", "Solo los alumnos pueden suscribirse a profesores");
                List<Usuario> listaProfesores = servicioLogin.obtenerProfesores();
                modelo.put("listaProfesores", listaProfesores);
                return new ModelAndView("verTutores", modelo);
            }

            boolean exito = servicioSuscripcion.suscribirAlumnoAProfesor(usuarioLogueado.getId(), profesorId);
            Profesor profesor = (Profesor) repositorioUsuario.buscarPorId(profesorId);

            List<ExperienciaEstudiantil> experiencias = servicioExperiencia.obtenerExperienciasPorProfesor(profesor.getId());
            List<FeedbackProfesor> feedbacks = servicioFeedback.obtenerFeedbacksPorProfesor(profesor.getId());
            Double promedioCalificacion = servicioFeedback.calcularPromedioCalificacion(profesor.getId());
            Integer totalFeedback = servicioFeedback.contarFeedbackPorProfesor(profesor.getId());

            modelo.put("profesor", profesor);
            modelo.put("experiencias", experiencias);
            modelo.put("feedbacks", feedbacks);
            modelo.put("promedioCalificacion", promedioCalificacion != null ? promedioCalificacion : 0.0);
            modelo.put("totalResenas", totalFeedback != null ? totalFeedback : 0);
            modelo.put("esVistaPublica", true);
            modelo.put("nombreUsuario", usuarioLogueado.getNombre());

            if (exito) {
                modelo.put("mensaje", "Te has suscrito exitosamente al profesor");
            } else {
                modelo.put("error", "Ya estás suscrito a este profesor o ocurrió un error");
            }

            return new ModelAndView("verPerfilDeProfesor", modelo);

        } catch (Exception e) {
            e.printStackTrace();
            modelo.put("error", "Ocurrió un error al procesar la suscripción");
            List<Usuario> listaProfesores = servicioLogin.obtenerProfesores();
            modelo.put("listaProfesores", listaProfesores);
            return new ModelAndView("verTutores", modelo);
        }
    }

    @RequestMapping(value = "/desuscribirseDeProfesor", method = RequestMethod.POST)
    public ModelAndView desuscribirseDeProfesor(@RequestParam Long profesorId, HttpServletRequest request) {
        ModelMap modelo = new ModelMap();

        try {
            Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("USUARIO");

            if (usuarioLogueado == null) {
                modelo.put("error", "Debes iniciar sesión para desuscribirte de un profesor");
                return new ModelAndView("login", modelo);
            }

            if (!(usuarioLogueado instanceof Alumno)) {
                modelo.put("error", "Solo los alumnos pueden desuscribirse de profesores");
                List<Usuario> listaProfesores = servicioLogin.obtenerProfesores();
                modelo.put("listaProfesores", listaProfesores);
                return new ModelAndView("verTutores", modelo);
            }

            boolean exito = servicioSuscripcion.desuscribirAlumnoDeProfesor(usuarioLogueado.getId(), profesorId);
            Profesor profesor = (Profesor) repositorioUsuario.buscarPorId(profesorId);

            List<ExperienciaEstudiantil> experiencias = servicioExperiencia.obtenerExperienciasPorProfesor(profesor.getId());
            List<FeedbackProfesor> feedbacks = servicioFeedback.obtenerFeedbacksPorProfesor(profesor.getId());
            Double promedioCalificacion = servicioFeedback.calcularPromedioCalificacion(profesor.getId());
            Integer totalFeedback = servicioFeedback.contarFeedbackPorProfesor(profesor.getId());

            modelo.put("profesor", profesor);
            modelo.put("experiencias", experiencias);
            modelo.put("feedbacks", feedbacks);
            modelo.put("promedioCalificacion", promedioCalificacion != null ? promedioCalificacion : 0.0);
            modelo.put("totalResenas", totalFeedback != null ? totalFeedback : 0);
            modelo.put("esVistaPublica", true);
            modelo.put("nombreUsuario", usuarioLogueado.getNombre());

            if (exito) {
                modelo.put("mensaje", "Te has desuscrito exitosamente del profesor");
            } else {
                modelo.put("error", "No estabas suscrito a este profesor o ocurrió un error");
            }

            return new ModelAndView("verPerfilDeProfesor", modelo);

        } catch (Exception e) {
            e.printStackTrace();
            modelo.put("error", "Error al procesar la desuscripción");
            List<Usuario> listaProfesores = servicioLogin.obtenerProfesores();
            modelo.put("listaProfesores", listaProfesores);
            return new ModelAndView("verTutores", modelo);
        }
    }
}