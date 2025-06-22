package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.dominio.servicios.ServicioTema;
import com.tallerwebi.dominio.servicios.ServicioFeedback;
import com.tallerwebi.dominio.servicios.ServicioExperiencia;
import com.tallerwebi.dominio.RepositorioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.List;

@Controller
@RequestMapping("/profesor")
public class ControladorPerfilProfesor {

    private ServicioTema servicioTema;
    private RepositorioUsuario repositorioUsuario;
    private ServicioFeedback servicioFeedback;
    private ServicioExperiencia servicioExperiencia;

    @Autowired
    public ControladorPerfilProfesor(ServicioTema servicioTema,
                                     RepositorioUsuario repositorioUsuario,
                                     ServicioFeedback servicioFeedback,
                                     ServicioExperiencia servicioExperiencia) {
        this.servicioTema = servicioTema;
        this.repositorioUsuario = repositorioUsuario;
        this.servicioFeedback = servicioFeedback;
        this.servicioExperiencia = servicioExperiencia;
    }

    @RequestMapping("/perfil")
    @Transactional
    public ModelAndView verPerfilProfesor(HttpServletRequest request) {
        ModelMap modelo = new ModelMap();

        Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("USUARIO");

        if (usuarioLogueado == null) {
            return new ModelAndView("redirect:/login");
        }

        if (!(usuarioLogueado instanceof Profesor)) {
            return new ModelAndView("redirect:/home");
        }

        Profesor profesor = repositorioUsuario.buscarProfesorConExperiencias(usuarioLogueado.getId());
        List<ExperienciaEstudiantil> experiencias = servicioExperiencia.obtenerExperienciasPorProfesor(profesor.getId());
        List<FeedbackProfesor> feedbacks = servicioFeedback.obtenerFeedbacksPorProfesor(profesor.getId());
        List<Tema> todosLosTemas = servicioTema.obtenerTodos();

        Double promedioCalificacion = servicioFeedback.calcularPromedioCalificacion(profesor.getId());
        Integer totalFeedback = servicioFeedback.contarFeedbackPorProfesor(profesor.getId());

        modelo.put("profesor", profesor);
        modelo.put("feedbacks", feedbacks);
        modelo.put("experiencias", experiencias);
        modelo.put("todosLosTemas", todosLosTemas);
        modelo.put("promedioCalificacion", promedioCalificacion != null ? promedioCalificacion : 0.0);
        modelo.put("totalResenas", totalFeedback != null ? totalFeedback : 0);
        modelo.put("esEdicion", false);

        return new ModelAndView("verMiPerfilProfesor", modelo);
    }

    @RequestMapping("/perfil/editar")
    @Transactional
    public ModelAndView editarPerfilProfesor(HttpServletRequest request) {
        ModelMap modelo = new ModelMap();

        Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("USUARIO");

        if (usuarioLogueado == null) {
            return new ModelAndView("redirect:/login");
        }

        if (!(usuarioLogueado instanceof Profesor)) {
            return new ModelAndView("redirect:/home");
        }

        Profesor profesor = repositorioUsuario.buscarProfesorConExperiencias(usuarioLogueado.getId());
        List<ExperienciaEstudiantil> experiencias = servicioExperiencia.obtenerExperienciasPorProfesor(profesor.getId());
        List<Tema> todosLosTemas = servicioTema.obtenerTodos();

        modelo.put("profesor", profesor);
        modelo.put("todosLosTemas", todosLosTemas);
        modelo.put("experiencias", experiencias);
        modelo.put("esEdicion", true);

        return new ModelAndView("verMiPerfilProfesor", modelo);
    }

    @RequestMapping(path = "/perfil/actualizar", method = RequestMethod.POST)
    @Transactional
    public ModelAndView actualizarPerfilProfesor(@RequestParam(required = false) String nombre,
                                                 @RequestParam(required = false) String apellido,
                                                 @RequestParam(required = false) String descripcion,
                                                 @RequestParam(required = false) String modalidadPreferida,
                                                 @RequestParam(required = false) Long temaId,
                                                 @RequestParam(required = false) String fotoBase64,
                                                 HttpServletRequest request,
                                                 RedirectAttributes redirectAttributes) {

        Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("USUARIO");

        if (usuarioLogueado == null) {
            return new ModelAndView("redirect:/login");
        }

        if (!(usuarioLogueado instanceof Profesor)) {
            return new ModelAndView("redirect:/home");
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El nombre es obligatorio");
            return new ModelAndView("redirect:/profesor/perfil/editar");
        }

        if (apellido == null || apellido.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El apellido es obligatorio");
            return new ModelAndView("redirect:/profesor/perfil/editar");
        }

        try {
            Profesor profesor = (Profesor) repositorioUsuario.buscarPorId(usuarioLogueado.getId());

            profesor.setNombre(nombre.trim());
            profesor.setApellido(apellido.trim());


            if (descripcion != null && !descripcion.trim().isEmpty()) {
                profesor.setDescripcion(descripcion.trim());
            } else {
                profesor.setDescripcion(null);
            }

            if (modalidadPreferida != null && !modalidadPreferida.isEmpty()) {
                try {
                    profesor.setModalidadPreferida(ModalidadPreferida.valueOf(modalidadPreferida));
                } catch (IllegalArgumentException e) {
                    profesor.setModalidadPreferida(null);
                }
            } else {
                profesor.setModalidadPreferida(null);
            }


            if (temaId != null) {
                Tema tema = servicioTema.obtenerPorId(temaId);
                if (tema != null) {
                    profesor.setTema(tema);
                }
            }
            if (fotoBase64 != null && !fotoBase64.isEmpty()) {
                if (validarImagenBase64(fotoBase64)) {
                    profesor.setFotoPerfil(fotoBase64);
                } else {
                    redirectAttributes.addFlashAttribute("error", "Formato de imagen no válido");
                    return new ModelAndView("redirect:/profesor/perfil/editar");
                }
            }

            repositorioUsuario.modificar(profesor);
            request.getSession().setAttribute("USUARIO", profesor);

            redirectAttributes.addFlashAttribute("exito", "Perfil actualizado correctamente");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil: " + e.getMessage());
        }

        return new ModelAndView("redirect:/profesor/perfil");
    }

    @RequestMapping(path = "/perfil/eliminar-foto", method = RequestMethod.POST)
    @Transactional
    public ModelAndView eliminarFotoProfesor(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("USUARIO");

        if (usuarioLogueado == null) {
            return new ModelAndView("redirect:/login");
        }

        if (!(usuarioLogueado instanceof Profesor)) {
            return new ModelAndView("redirect:/home");
        }

        try {
            Profesor profesor = (Profesor) repositorioUsuario.buscarPorId(usuarioLogueado.getId());
            profesor.setFotoPerfil(null);

            repositorioUsuario.modificar(profesor);
            request.getSession().setAttribute("USUARIO", profesor);

            redirectAttributes.addFlashAttribute("exito", "Foto de perfil eliminada correctamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la foto: " + e.getMessage());
        }

        return new ModelAndView("redirect:/profesor/perfil");
    }

    @RequestMapping(path = "/perfil/agregar-experiencia", method = RequestMethod.POST)
    @Transactional
    public ModelAndView agregarExperiencia(@RequestParam String institucion,
                                           @RequestParam String descripcion,
                                           @RequestParam String fecha,
                                           HttpServletRequest request,
                                           RedirectAttributes redirectAttributes) {

        Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("USUARIO");

        if (usuarioLogueado == null) {
            return new ModelAndView("redirect:/login");
        }

        if (!(usuarioLogueado instanceof Profesor)) {
            return new ModelAndView("redirect:/home");
        }

        try {
            Profesor profesor = (Profesor) repositorioUsuario.buscarPorId(usuarioLogueado.getId());

            ExperienciaEstudiantil experiencia = new ExperienciaEstudiantil();
            experiencia.setInstitucion(institucion);
            experiencia.setDescripcion(descripcion);
            experiencia.setFecha(fecha);
            experiencia.setProfesor(profesor);

            servicioExperiencia.guardar(experiencia);

            redirectAttributes.addFlashAttribute("exito", "Experiencia agregada correctamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al agregar la experiencia: " + e.getMessage());
        }

        return new ModelAndView("redirect:/profesor/perfil");
    }

    @RequestMapping(path = "/perfil/eliminar-experiencia", method = RequestMethod.POST)
    @Transactional
    public ModelAndView eliminarExperiencia(@RequestParam Long experienciaId,
                                            HttpServletRequest request,
                                            RedirectAttributes redirectAttributes) {

        Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("USUARIO");

        if (usuarioLogueado == null) {
            return new ModelAndView("redirect:/login");
        }

        if (!(usuarioLogueado instanceof Profesor)) {
            return new ModelAndView("redirect:/home");
        }

        try {
            servicioExperiencia.eliminar(experienciaId);
            redirectAttributes.addFlashAttribute("exito", "Experiencia eliminada correctamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la experiencia: " + e.getMessage());
        }

        return new ModelAndView("redirect:/profesor/perfil");
    }

    private boolean validarImagenBase64(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return false;
        }
        if (!base64.startsWith("data:image/")) {
            return false;
        }
        if (!base64.contains("base64,")) {
            return false;
        }

        try {
            String base64Data = base64.substring(base64.indexOf("base64,") + 7);

            if (base64Data.length() % 4 != 0) {
                return false;
            }

            if (!base64Data.matches("^[A-Za-z0-9+/]*={0,2}$")) {
                return false;
            }

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}