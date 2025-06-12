package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ControladorLogin {

    private ServicioLogin servicioLogin;

    @Autowired
    public ControladorLogin(ServicioLogin servicioLogin){
        this.servicioLogin = servicioLogin;
    }

    @RequestMapping("/login")
    public ModelAndView irALogin() {

        ModelMap modelo = new ModelMap();
        modelo.put("datosLogin", new DatosLogin());
        return new ModelAndView("login", modelo);
    }

    @RequestMapping(path = "/validar-login", method = RequestMethod.POST)
    public ModelAndView validarLogin(@ModelAttribute("datosLogin") DatosLogin datosLogin, HttpServletRequest request) {
        ModelMap model = new ModelMap();

        String email = datosLogin.getEmail();
        String password = datosLogin.getPassword();

        boolean emailVacio = (email == null || email.trim().isEmpty());
        boolean passwordVacio = (password == null || password.trim().isEmpty());

        if (emailVacio && passwordVacio) {
            model.put("error", "El email y la contraseña son obligatorios");
            return new ModelAndView("login", model);
        }

        if (emailVacio) {
            model.put("error", "El email es obligatorio");
            return new ModelAndView("login", model);
        }

        if (passwordVacio) {
            model.put("error", "La contraseña es obligatoria");
            return new ModelAndView("login", model);
        }

        String errorEmail = validarEmail(email);
        if (errorEmail != null) {
            model.put("error", errorEmail);
            return new ModelAndView("login", model);
        }

        Usuario usuarioBuscado = servicioLogin.consultarUsuario(email, password);

        if (usuarioBuscado != null) {
            request.getSession().setAttribute("ROL", usuarioBuscado.getRol());
            request.getSession().setAttribute("USUARIO", usuarioBuscado);
            return new ModelAndView("redirect:/home");
        } else {
            model.put("error", "Usuario o clave incorrecta");
            return new ModelAndView("login", model);
        }
    }

    @RequestMapping(path = "/registrarme", method = RequestMethod.POST)
    public ModelAndView registrarme(@ModelAttribute("usuario") Usuario usuario) {
        ModelMap model = new ModelMap();

        if (usuario == null) {
            model.put("error", "Error al registrar el nuevo usuario");
            return new ModelAndView("nuevo-usuario", model);
        }

        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty() ||
                usuario.getEmail() == null || usuario.getEmail().trim().isEmpty() ||
                usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
            model.put("error", "Todos los campos son obligatorios");
            return new ModelAndView("nuevo-usuario", model);
        }

        String errorEmail = validarEmail(usuario.getEmail());
        if (errorEmail != null) {
            model.put("error", errorEmail);
            return new ModelAndView("nuevo-usuario", model);
        }

        try {
            servicioLogin.registrar(usuario);
        } catch (UsuarioExistente e) {
            model.put("error", "El usuario ya existe");
            return new ModelAndView("nuevo-usuario", model);
        } catch (Exception e) {
            model.put("error", "Error al registrar el nuevo usuario");
            return new ModelAndView("nuevo-usuario", model);
        }
        return new ModelAndView("redirect:/login");
    }

    @RequestMapping(path = "/nuevo-usuario", method = RequestMethod.GET)
    public ModelAndView nuevoUsuario() {
        ModelMap model = new ModelMap();
        model.put("usuario", new Usuario());
        return new ModelAndView("nuevo-usuario", model);
    }

    @RequestMapping(path = "/home", method = RequestMethod.GET)
    public ModelAndView irAHome(HttpServletRequest request) {
        ModelMap modelo = new ModelMap();
        Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");

        if (usuario != null) {
            modelo.put("nombreUsuario", usuario.getNombre());

            modelo.put("listaProfesores", servicioLogin.obtenerProfesores());
        }

        return new ModelAndView("home", modelo);
    }

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public ModelAndView inicio() {
        return new ModelAndView("redirect:/login");
    }


    @RequestMapping("/registrarprofesor")
    public ModelAndView mostrarFormularioProfesor() {
        ModelMap model = new ModelMap();
        model.put("usuario", new Usuario());
        model.put("materias", Materia.values());
        return new ModelAndView("registrar-profesor", model);
    }


    @RequestMapping(path = "/registrarprofesor", method = RequestMethod.POST)
    public ModelAndView procesarRegistroProfesor(@ModelAttribute("usuario") Usuario usuario,
                                                 @RequestParam("materia") Materia materia) {
        ModelMap model = new ModelMap();

        usuario.setRol("profesor");

        Profesor profesor = new Profesor();
        profesor.setMateria(materia);
        profesor.setLatitud(0.0);
        profesor.setLongitud(0.0);
        usuario.setProfesor(profesor);

        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty() ||
                usuario.getEmail() == null || usuario.getEmail().trim().isEmpty() ||
                usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
            model.put("error", "Todos los campos son obligatorios");
            model.put("materias", Materia.values());
            return new ModelAndView("registrar-profesor", model);
        }

        String errorEmail = validarEmail(usuario.getEmail());
        if (errorEmail != null) {
            model.put("error", errorEmail);
            model.put("materias", Materia.values());
            return new ModelAndView("registrar-profesor", model);
        }

        try {
            servicioLogin.registrar(usuario);
        } catch (UsuarioExistente e) {
            model.put("error", "El correo ya está registrado");
            model.put("materias", Materia.values());
            return new ModelAndView("registrar-profesor", model);
        } catch (Exception e) {
            model.put("error", "Error inesperado");
            model.put("materias", Materia.values());
            return new ModelAndView("registrar-profesor", model);
        }

        return new ModelAndView("redirect:/login");
    }


    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public ModelAndView cerrarSesion(HttpServletRequest request) {
        request.getSession().invalidate();
        return new ModelAndView("redirect:/home");
    }
    @RequestMapping("/verPerfil")
    public String verPerfil() {
        return "verPerfil";
    }

    private String validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "El email es obligatorio";
        }
        if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return "El formato del email es inválido";
        }
        return null;
    }

}




