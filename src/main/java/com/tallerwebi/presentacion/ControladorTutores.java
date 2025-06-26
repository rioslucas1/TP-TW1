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
                                            @RequestParam(defaultValue = "false") boolean editandoFeedback,
                                            HttpServletRequest request) {
        try {
            Profesor profesor = (Profesor) repositorioUsuario.buscar(email);
            if (profesor == null) {
                return new ModelAndView("redirect:/verTutores");
            }

            Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("USUARIO");
            return construirVistaPerfilProfesor(profesor, usuarioLogueado, editandoFeedback, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ModelAndView("redirect:/verTutores");
        }
    }

    @RequestMapping(value = "/suscribirseAProfesor", method = RequestMethod.POST)
    public ModelAndView suscribirseAProfesor(@RequestParam Long profesorId, HttpServletRequest request) {
        try {
            Usuario usuarioLogueado = validarUsuarioLogueado(request);
            if (usuarioLogueado == null) {
                return redirigirALogin("Debes iniciar sesión para suscribirte a un profesor");
            }

            boolean exito = servicioSuscripcion.suscribirAlumnoAProfesor(usuarioLogueado.getId(), profesorId);
            Profesor profesor = (Profesor) repositorioUsuario.buscarPorId(profesorId);

            String mensaje = exito ? "Te has suscrito exitosamente al profesor"
                    : "Ya estás suscrito a este profesor o ocurrió un error";

            return construirVistaPerfilProfesor(profesor, usuarioLogueado, false, mensaje, null);
        } catch (Exception e) {
            e.printStackTrace();
            return manejarErrorYVolverATutores("Ocurrió un error al procesar la suscripción");
        }
    }

    @RequestMapping(value = "/desuscribirseDeProfesor", method = RequestMethod.POST)
    public ModelAndView desuscribirseDeProfesor(@RequestParam Long profesorId, HttpServletRequest request) {
        try {
            Usuario usuarioLogueado = validarUsuarioLogueado(request);
            if (usuarioLogueado == null) {
                return redirigirALogin("Debes iniciar sesión para desuscribirte de un profesor");
            }

            boolean exito = servicioSuscripcion.desuscribirAlumnoDeProfesor(usuarioLogueado.getId(), profesorId);
            Profesor profesor = (Profesor) repositorioUsuario.buscarPorId(profesorId);

            String mensaje = exito ? "Te has desuscrito exitosamente del profesor"
                    : "No estabas suscrito a este profesor o ocurrió un error";

            return construirVistaPerfilProfesor(profesor, usuarioLogueado, false, mensaje, null);
        } catch (Exception e) {
            e.printStackTrace();
            return manejarErrorYVolverATutores("Error al procesar la desuscripción");
        }
    }

    @RequestMapping(value = "/dejarFeedback", method = RequestMethod.POST)
    @Transactional
    public ModelAndView dejarFeedback(@RequestParam Long profesorId,
                                      @RequestParam Integer calificacion,
                                      @RequestParam String comentario,
                                      HttpServletRequest request) {
        try {
            Usuario usuarioLogueado = validarUsuarioLogueado(request);
            if (usuarioLogueado == null) {
                return redirigirALogin("Debes iniciar sesión para dejar feedback");
            }

            Alumno alumno = (Alumno) usuarioLogueado;
            Profesor profesor = (Profesor) repositorioUsuario.buscarPorId(profesorId);

            if (profesor == null) {
                return new ModelAndView("redirect:/verTutores");
            }

            String error = validarFeedback(alumno, profesorId, calificacion);
            if (error != null) {
                return construirVistaPerfilProfesor(profesor, usuarioLogueado, false, null, error);
            }

            FeedbackProfesor feedback = new FeedbackProfesor(profesor, alumno, calificacion, comentario);
            servicioFeedback.guardar(feedback);

            return construirVistaPerfilProfesor(profesor, usuarioLogueado, false,
                    "Feedback enviado exitosamente", null);

        } catch (Exception e) {
            e.printStackTrace();
            return manejarErrorYVolverATutores("Error al procesar el feedback");
        }
    }

    @RequestMapping(value = "/editarFeedback", method = RequestMethod.POST)
    public ModelAndView editarFeedback(@RequestParam Long profesorId, HttpServletRequest request) {
        try {
            Usuario usuarioLogueado = validarUsuarioLogueado(request);
            if (usuarioLogueado == null) {
                return redirigirALogin("Debes iniciar sesión para editar feedback");
            }

            Profesor profesor = (Profesor) repositorioUsuario.buscarPorId(profesorId);
            if (profesor == null) {
                return new ModelAndView("redirect:/verTutores");
            }

            FeedbackProfesor feedbackUsuario = servicioFeedback.buscarFeedbackDeAlumnoParaProfesor(
                    usuarioLogueado.getId(), profesorId);

            if (feedbackUsuario == null) {
                return construirVistaPerfilProfesor(profesor, usuarioLogueado, false,
                        null, "No tenes feedback para editar");
            }

            return construirVistaPerfilProfesor(profesor, usuarioLogueado, true, null, null);

        } catch (Exception e) {
            e.printStackTrace();
            return manejarErrorYVolverATutores("Error al cargar el modo de edición");
        }
    }

    @RequestMapping(value = "/actualizarFeedback", method = RequestMethod.POST)
    @Transactional
    public ModelAndView actualizarFeedback(@RequestParam Long profesorId,
                                           @RequestParam Long feedbackId,
                                           @RequestParam Integer calificacion,
                                           @RequestParam String comentario,
                                           HttpServletRequest request) {
        try {
            Usuario usuarioLogueado = validarUsuarioLogueado(request);
            if (usuarioLogueado == null) {
                return redirigirALogin("Debes iniciar sesión para actualizar feedback");
            }

            Profesor profesor = (Profesor) repositorioUsuario.buscarPorId(profesorId);
            FeedbackProfesor feedback = servicioFeedback.obtenerPorId(feedbackId);

            if (profesor == null) {
                return new ModelAndView("redirect:/verTutores");
            }

            String error = validarActualizacionFeedback(feedback, usuarioLogueado, calificacion);
            if (error != null) {
                return construirVistaPerfilProfesor(profesor, usuarioLogueado, true, null, error);
            }

            feedback.setCalificacion(calificacion);
            feedback.setComentario(comentario);
            servicioFeedback.guardar(feedback);

            return construirVistaPerfilProfesor(profesor, usuarioLogueado, false,
                    "Feedback actualizado exitosamente", null);

        } catch (Exception e) {
            e.printStackTrace();
            return manejarErrorYVolverATutores("Error al actualizar el feedback");
        }
    }

    @RequestMapping(value = "/cancelarEdicionFeedback", method = RequestMethod.POST)
    public ModelAndView cancelarEdicionFeedback(@RequestParam Long profesorId) {
        Profesor profesor = (Profesor) repositorioUsuario.buscarPorId(profesorId);
        return new ModelAndView("redirect:/verPerfilDeProfesor?email=" + profesor.getEmail());
    }

    @RequestMapping(value = "/borrarFeedback", method = RequestMethod.POST)
    @Transactional
    public ModelAndView borrarFeedback(@RequestParam Long profesorId,
                                       @RequestParam Long feedbackId,
                                       HttpServletRequest request) {
        try {
            Usuario usuarioLogueado = validarUsuarioLogueado(request);
            if (usuarioLogueado == null) {
                return redirigirALogin("Debes iniciar sesión para borrar feedback");
            }

            Profesor profesor = (Profesor) repositorioUsuario.buscarPorId(profesorId);
            FeedbackProfesor feedback = servicioFeedback.obtenerPorId(feedbackId);

            if (profesor == null) {
                return new ModelAndView("redirect:/verTutores");
            }

            String error = validarEliminacionFeedback(feedback, usuarioLogueado);
            if (error != null) {
                return construirVistaPerfilProfesor(profesor, usuarioLogueado, false, null, error);
            }

            servicioFeedback.eliminar(feedback.getId());
            return construirVistaPerfilProfesor(profesor, usuarioLogueado, false,
                    "Feedback eliminado exitosamente", null);

        } catch (Exception e) {
            e.printStackTrace();
            return manejarErrorYVolverATutores("Error al eliminar el feedback");
        }
    }

    private Usuario validarUsuarioLogueado(HttpServletRequest request) {
        Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");
        return (usuario instanceof Alumno) ? usuario : null;
    }

    private ModelAndView redirigirALogin(String mensaje) {
        ModelMap modelo = new ModelMap();
        modelo.put("error", mensaje);
        modelo.put("datosLogin", new DatosLogin());
        return new ModelAndView("login", modelo);
    }

    private ModelAndView manejarErrorYVolverATutores(String mensajeError) {
        ModelMap modelo = new ModelMap();
        modelo.put("error", mensajeError);
        List<Usuario> listaProfesores = servicioLogin.obtenerProfesores();
        modelo.put("listaProfesores", listaProfesores);
        return new ModelAndView("verTutores", modelo);
    }

    private String validarFeedback(Alumno alumno, Long profesorId, Integer calificacion) {
        boolean yaDejoFeedback = servicioFeedback.alumnoYaDejoFeedback(alumno.getId(), profesorId);
        boolean suscripto = servicioSuscripcion.estaAlumnoSuscritoAProfesor(alumno.getId(), profesorId);

        if (yaDejoFeedback) {
            return "Ya le dejaste feedback para este profesor.";
        }
        if (!suscripto) {
            return "Tenes que estar suscrito al profesor para poder dejar una reseña";
        }
        if (calificacion < 1 || calificacion > 5) {
            return "La calificación debe estar entre 1 y 5 estrellas";
        }
        return null;
    }

    private String validarActualizacionFeedback(FeedbackProfesor feedback, Usuario usuario, Integer calificacion) {
        if (feedback == null) {
            return "Feedback no encontrado";
        }
        if (!feedback.getAlumno().getId().equals(usuario.getId())) {
            return "No tenes permisos para editar este feedback";
        }
        if (calificacion < 1 || calificacion > 5) {
            return "La calificación debe estar entre 1 y 5 estrellas";
        }
        return null;
    }

    private String validarEliminacionFeedback(FeedbackProfesor feedback, Usuario usuario) {
        if (feedback == null) {
            return "Feedback no encontrado";
        }
        if (!feedback.getAlumno().getId().equals(usuario.getId())) {
            return "No tenes permisos para borrar este feedback";
        }
        return null;
    }

    private ModelAndView construirVistaPerfilProfesor(Profesor profesor, Usuario usuarioLogueado,
                                                      boolean editandoFeedback, String mensaje, String error) {
        ModelMap modelo = new ModelMap();
        List<ExperienciaEstudiantil> experiencias = servicioExperiencia.obtenerExperienciasPorProfesor(profesor.getId());
        List<FeedbackProfesor> feedbacks = servicioFeedback.obtenerFeedbacksPorProfesor(profesor.getId());
        Double promedioCalificacion = servicioFeedback.calcularPromedioCalificacion(profesor.getId());
        Integer totalFeedback = servicioFeedback.contarFeedbackPorProfesor(profesor.getId());

        String nombreUsuario = usuarioLogueado != null ? usuarioLogueado.getNombre() : null;
        boolean suscripto = false;
        boolean yaDejoFeedback = false;
        FeedbackProfesor feedbackUsuario = null;
        Long usuarioId = null;

        if (usuarioLogueado != null) {
            usuarioId = usuarioLogueado.getId();
            suscripto = servicioSuscripcion.estaAlumnoSuscritoAProfesor(usuarioId, profesor.getId());
            yaDejoFeedback = servicioFeedback.alumnoYaDejoFeedback(usuarioId, profesor.getId());
            if (yaDejoFeedback) {
                feedbackUsuario = servicioFeedback.buscarFeedbackDeAlumnoParaProfesor(usuarioId, profesor.getId());
            }
        }

        modelo.put("profesor", profesor);
        modelo.put("experiencias", experiencias);
        modelo.put("feedbacks", feedbacks);
        modelo.put("feedbackUsuario", feedbackUsuario);
        modelo.put("promedioCalificacion", promedioCalificacion != null ? promedioCalificacion : 0.0);
        modelo.put("totalResenas", totalFeedback != null ? totalFeedback : 0);
        modelo.put("usuarioId", usuarioId);
        modelo.put("nombreUsuario", nombreUsuario);
        modelo.put("yaSuscripto", suscripto);
        modelo.put("yaDejoFeedback", yaDejoFeedback);
        modelo.put("editandoFeedback", editandoFeedback);
        modelo.put("esVistaPublica", true);

        if (mensaje != null) modelo.put("mensaje", mensaje);
        if (error != null) modelo.put("error", error);

        return new ModelAndView("verPerfilDeProfesor", modelo);
    }
}
