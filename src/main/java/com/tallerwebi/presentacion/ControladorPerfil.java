package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.dominio.servicios.ServicioTema;
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
public class ControladorPerfil {

    private ServicioTema servicioTema;
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    public ControladorPerfil(ServicioTema servicioTema, RepositorioUsuario repositorioUsuario) {
        this.servicioTema = servicioTema;
        this.repositorioUsuario = repositorioUsuario;
    }

    @RequestMapping("/perfil")
    @Transactional
    public ModelAndView verPerfil(HttpServletRequest request) {
        ModelMap modelo = new ModelMap();

        Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("USUARIO");

        if (usuarioLogueado == null) {
            return new ModelAndView("redirect:/login");
        }

        if (usuarioLogueado instanceof Profesor) {
            return new ModelAndView("redirect:/profesor/perfil");
        }


        if (!(usuarioLogueado instanceof Alumno)) {
            return new ModelAndView("redirect:/home");
        }

        Alumno alumno = (Alumno) repositorioUsuario.buscarPorId(usuarioLogueado.getId());
        List<Tema> todosLosTemas = servicioTema.obtenerTodos();

        modelo.put("nombreUsuario", alumno.getNombre());
        modelo.put("rol", "alumno");
        modelo.put("usuario", alumno);

        modelo.put("alumno", alumno);
        modelo.put("todosLosTemas", todosLosTemas);
        modelo.put("esEdicion", false);

        return new ModelAndView("verPerfil", modelo);
    }

    @RequestMapping("/perfil/editar")
    @Transactional
    public ModelAndView editarPerfil(HttpServletRequest request) {
        ModelMap modelo = new ModelMap();

        Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("USUARIO");

        if (usuarioLogueado == null) {
            return new ModelAndView("redirect:/login");
        }

        if (!(usuarioLogueado instanceof Alumno)) {
            return new ModelAndView("redirect:/home");
        }

        Alumno alumno = (Alumno) repositorioUsuario.buscarPorId(usuarioLogueado.getId());
        List<Tema> todosLosTemas = servicioTema.obtenerTodos();

        modelo.put("alumno", alumno);
        modelo.put("todosLosTemas", todosLosTemas);
        modelo.put("esEdicion", true);

        return new ModelAndView("verPerfil", modelo);
    }

    @RequestMapping(path = "/perfil/actualizar", method = RequestMethod.POST)
    @Transactional
    public ModelAndView actualizarPerfil(@RequestParam(required = false) String nombre,
                                         @RequestParam(required = false) String apellido,
                                         @RequestParam(required = false) String descripcion,
                                         @RequestParam(required = false) String modalidadPreferida,
                                         @RequestParam(required = false) String fotoBase64,
                                         HttpServletRequest request,
                                         RedirectAttributes redirectAttributes) {

        Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("USUARIO");

        if (usuarioLogueado == null) {
            return new ModelAndView("redirect:/login");
        }

        if (!(usuarioLogueado instanceof Alumno)) {
            return new ModelAndView("redirect:/home");
        }

        // Debug: imprimir parámetros recibidos
        System.out.println("Parámetros recibidos:");
        System.out.println("nombre: " + nombre);
        System.out.println("apellido: " + apellido);
        System.out.println("descripcion: " + descripcion);
        System.out.println("modalidadPreferida: " + modalidadPreferida);
        System.out.println("fotoBase64 length: " + (fotoBase64 != null ? fotoBase64.length() : "null"));

        // Validar campos obligatorios
        if (nombre == null || nombre.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El nombre es obligatorio");
            return new ModelAndView("redirect:/perfil/editar");
        }

        if (apellido == null || apellido.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El apellido es obligatorio");
            return new ModelAndView("redirect:/perfil/editar");
        }

        try {
            Alumno alumno = (Alumno) repositorioUsuario.buscarPorId(usuarioLogueado.getId());

            alumno.setNombre(nombre.trim());
            alumno.setApellido(apellido.trim());

            // Manejar descripción vacía
            if (descripcion != null && !descripcion.trim().isEmpty()) {
                alumno.setDescripcion(descripcion.trim());
            } else {
                alumno.setDescripcion(null);
            }

            // Manejar modalidad preferida
            if (modalidadPreferida != null && !modalidadPreferida.isEmpty()) {
                try {
                    alumno.setModalidadPreferida(ModalidadPreferida.valueOf(modalidadPreferida));
                } catch (IllegalArgumentException e) {
                    System.out.println("Modalidad inválida: " + modalidadPreferida);
                    alumno.setModalidadPreferida(null);
                }
            } else {
                alumno.setModalidadPreferida(null);
            }

            // Actualizar foto de perfil si se proporcionó una nueva
            if (fotoBase64 != null && !fotoBase64.isEmpty()) {
                if (validarImagenBase64(fotoBase64)) {
                    alumno.setFotoPerfil(fotoBase64);
                } else {
                    redirectAttributes.addFlashAttribute("error", "Formato de imagen no válido");
                    return new ModelAndView("redirect:/perfil/editar");
                }
            }

            repositorioUsuario.modificar(alumno);

            // Actualizar la sesión con los datos actualizados
            request.getSession().setAttribute("USUARIO", alumno);

            redirectAttributes.addFlashAttribute("exito", "Perfil actualizado correctamente");

        } catch (Exception e) {
            e.printStackTrace(); // Para debugging
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil: " + e.getMessage());
        }

        return new ModelAndView("redirect:/perfil");
    }

    @RequestMapping(path = "/perfil/eliminar-foto", method = RequestMethod.POST)
    @Transactional
    public ModelAndView eliminarFoto(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("USUARIO");

        if (usuarioLogueado == null) {
            return new ModelAndView("redirect:/login");
        }

        if (!(usuarioLogueado instanceof Alumno)) {
            return new ModelAndView("redirect:/home");
        }

        try {
            Alumno alumno = (Alumno) repositorioUsuario.buscarPorId(usuarioLogueado.getId());
            alumno.setFotoPerfil(null);

            repositorioUsuario.modificar(alumno);
            request.getSession().setAttribute("USUARIO", alumno);

            redirectAttributes.addFlashAttribute("exito", "Foto de perfil eliminada correctamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la foto: " + e.getMessage());
        }

        return new ModelAndView("redirect:/perfil");
    }

    @RequestMapping(path = "/perfil/agregar-tema", method = RequestMethod.POST)
    @Transactional
    public ModelAndView agregarTema(@RequestParam Long temaId,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {

        Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("USUARIO");

        if (usuarioLogueado == null) {
            return new ModelAndView("redirect:/login");
        }

        if (!(usuarioLogueado instanceof Alumno)) {
            return new ModelAndView("redirect:/home");
        }

        try {
            Alumno alumno = (Alumno) repositorioUsuario.buscarPorId(usuarioLogueado.getId());
            Tema tema = servicioTema.obtenerPorId(temaId);

            if (tema == null) {
                redirectAttributes.addFlashAttribute("error", "El tema seleccionado no existe");
                return new ModelAndView("redirect:/perfil");
            }

            if (alumno.getTemas().contains(tema)) {
                redirectAttributes.addFlashAttribute("error", "Ya tienes este tema en tus preferencias");
                return new ModelAndView("redirect:/perfil");
            }

            alumno.getTemas().add(tema);
            repositorioUsuario.modificar(alumno);

            request.getSession().setAttribute("USUARIO", alumno);

            redirectAttributes.addFlashAttribute("exito", "Tema agregado correctamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al agregar el tema: " + e.getMessage());
        }

        return new ModelAndView("redirect:/perfil");
    }

    @RequestMapping(path = "/perfil/eliminar-tema", method = RequestMethod.POST)
    @Transactional
    public ModelAndView eliminarTema(@RequestParam Long temaId,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {

        Usuario usuarioLogueado = (Usuario) request.getSession().getAttribute("USUARIO");

        if (usuarioLogueado == null) {
            return new ModelAndView("redirect:/login");
        }

        if (!(usuarioLogueado instanceof Alumno)) {
            return new ModelAndView("redirect:/home");
        }

        try {
            Alumno alumno = (Alumno) repositorioUsuario.buscarPorId(usuarioLogueado.getId());
            Tema tema = servicioTema.obtenerPorId(temaId);

            if (tema == null) {
                redirectAttributes.addFlashAttribute("error", "El tema seleccionado no existe");
                return new ModelAndView("redirect:/perfil");
            }

            alumno.getTemas().remove(tema);
            repositorioUsuario.modificar(alumno);

            request.getSession().setAttribute("USUARIO", alumno);

            redirectAttributes.addFlashAttribute("exito", "Tema eliminado correctamente");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el tema: " + e.getMessage());
        }

        return new ModelAndView("redirect:/perfil");
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