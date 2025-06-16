package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.servicios.ServicioLogin;
import com.tallerwebi.dominio.servicios.ServicioTema;
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
    private ServicioTema servicioTema;

    @Autowired
    public ControladorLogin(ServicioLogin servicioLogin, ServicioTema servicioTema){
        this.servicioLogin = servicioLogin;
        this.servicioTema = servicioTema;
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
        String errorEmail = validarEmail(datosLogin.getEmail());
        if (errorEmail != null) {
            model.put("error", errorEmail);
            return new ModelAndView("login", model);
        }

        if (datosLogin.getEmail() == null || datosLogin.getEmail().trim().isEmpty() ||
                datosLogin.getPassword() == null || datosLogin.getPassword().trim().isEmpty()) {
            model.put("error", "El email y la contraseña son obligatorios");
            return new ModelAndView("login", model);
        }

        Usuario usuarioBuscado = servicioLogin.consultarUsuario(datosLogin.getEmail(), datosLogin.getPassword());

        if (usuarioBuscado != null) {

            String rol;
            if (usuarioBuscado instanceof Profesor) {
                rol = "profesor";
            } else if (usuarioBuscado instanceof Alumno) {
                rol = "alumno";
            } else {
                rol = "usuario";
            }

            request.getSession().setAttribute("ROL", rol);
            request.getSession().setAttribute("USUARIO", usuarioBuscado);
            return new ModelAndView("redirect:/home");
        } else {
            model.put("error", "Usuario o clave incorrecta");
            return new ModelAndView("login", model);
        }
    }

    @RequestMapping(path = "/registrarme", method = RequestMethod.POST)
    public ModelAndView registrarme(@ModelAttribute("datosRegistro") DatosRegistro datosRegistro) {
        ModelMap model = new ModelMap();

        if (datosRegistro.getNombre() == null || datosRegistro.getNombre().trim().isEmpty() ||
                datosRegistro.getApellido() == null || datosRegistro.getApellido().trim().isEmpty() ||
                datosRegistro.getEmail() == null || datosRegistro.getEmail().trim().isEmpty() ||
                datosRegistro.getPassword() == null || datosRegistro.getPassword().trim().isEmpty()) {
            model.put("error", "Todos los campos son obligatorios");
            return new ModelAndView("nuevo-usuario", model);
        }

        String errorEmail = validarEmail(datosRegistro.getEmail());
        if (errorEmail != null) {
            model.put("error", errorEmail);
            return new ModelAndView("nuevo-usuario", model);
        }

        try{
            Alumno nuevoAlumno = new Alumno();
            nuevoAlumno.setNombre(datosRegistro.getNombre());
            nuevoAlumno.setApellido(datosRegistro.getApellido());
            nuevoAlumno.setEmail(datosRegistro.getEmail());
            nuevoAlumno.setPassword(datosRegistro.getPassword());
            nuevoAlumno.setActivo(true);
            servicioLogin.registrar(nuevoAlumno);
        } catch (UsuarioExistente e){
            model.put("error", "El usuario ya existe");
            return new ModelAndView("nuevo-usuario", model);
        } catch (Exception e){
            model.put("error", "Error al registrar el nuevo usuario");
            return new ModelAndView("nuevo-usuario", model);
        }
        return new ModelAndView("redirect:/login");
    }

    @RequestMapping(path = "/nuevo-usuario", method = RequestMethod.GET)
    public ModelAndView nuevoUsuario() {
        ModelMap model = new ModelMap();
        model.put("datosRegistro", new DatosRegistro());
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
        model.put("datosRegistro", new DatosRegistroProfesor());
        model.put("temas", servicioTema.obtenerTodos());
        return new ModelAndView("registrar-profesor", model);
    }

    @RequestMapping(path = "/registrarprofesor", method = RequestMethod.POST)
    public ModelAndView procesarRegistroProfesor(@ModelAttribute("datosRegistro") DatosRegistroProfesor datosRegistroProfesor, @RequestParam("temaId") Long temaId) {
        ModelMap model = new ModelMap();

        if (datosRegistroProfesor.getNombre() == null || datosRegistroProfesor.getNombre().trim().isEmpty() ||
                datosRegistroProfesor.getApellido() == null || datosRegistroProfesor.getApellido().trim().isEmpty() ||
                datosRegistroProfesor.getEmail() == null || datosRegistroProfesor.getEmail().trim().isEmpty() ||
                datosRegistroProfesor.getPassword() == null || datosRegistroProfesor.getPassword().trim().isEmpty() ||
                temaId == null) {
            model.put("error", "Todos los campos son obligatorios");
            model.put("temas", servicioTema.obtenerTodos());
            return new ModelAndView("registrar-profesor", model);
        }

        String errorEmail = validarEmail(datosRegistroProfesor.getEmail());
        if (errorEmail != null) {
            model.put("error", errorEmail);
            model.put("temas", servicioTema.obtenerTodos());
            return new ModelAndView("registrar-profesor", model);
        }

        try {

            Profesor nuevoProfesor = new Profesor();
            nuevoProfesor.setNombre(datosRegistroProfesor.getNombre());
            nuevoProfesor.setApellido(datosRegistroProfesor.getApellido());
            nuevoProfesor.setEmail(datosRegistroProfesor.getEmail());
            nuevoProfesor.setPassword(datosRegistroProfesor.getPassword());
            nuevoProfesor.setActivo(true);
            nuevoProfesor.setTema(servicioTema.obtenerPorId(datosRegistroProfesor.getTemaId()));
            servicioLogin.registrar(nuevoProfesor);
        } catch (UsuarioExistente e) {
            model.put("error", "El correo ya está registrado");
            model.put("temas", servicioTema.obtenerTodos());
            return new ModelAndView("registrar-profesor", model);
        } catch (Exception e) {
            model.put("error", "Error inesperado");
            model.put("temas", servicioTema.obtenerTodos());
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




