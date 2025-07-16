package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.servicios.ServicioArchivo;
import com.tallerwebi.dominio.servicios.ServicioLogin;
import com.tallerwebi.dominio.servicios.ServicioTema;
import com.tallerwebi.dominio.servicios.ServicioChat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ControladorLogin {

    private ServicioLogin servicioLogin;
    private ServicioTema servicioTema;
    private ServicioArchivo servicioArchivo;
    private ServicioChat servicioChat;

    @Autowired
    public ControladorLogin(ServicioLogin servicioLogin, ServicioTema servicioTema, ServicioArchivo servicioArchivo, ServicioChat servicioChat){
        this.servicioLogin = servicioLogin;
        this.servicioTema = servicioTema;
        this.servicioArchivo = servicioArchivo;
        this.servicioChat = servicioChat;
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

            String rol;
            if (usuarioBuscado instanceof Profesor) {
                rol = "profesor";
            } else if (usuarioBuscado instanceof Alumno) {
                rol = "alumno";
            } else {
                rol = "usuario";
            }
            servicioLogin.guardarUltimaConexion(usuarioBuscado);
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
        } catch (Exception e) {
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
        String rol = definirRol(usuario);

        if (usuario != null) {
            modelo.put("nombreUsuario", usuario.getNombre());
            modelo.put("rol", rol);


            if(rol.equals("profesor")){
                Profesor profesor = (Profesor) usuario;
                modelo.put("temaProfesor", profesor.getTema());
                List<Clase> todasLasClases = servicioLogin.obtenerClasesProfesor(profesor.getId());
                List<Clase> proximasClases = todasLasClases.stream()
                        .limit(5)
                        .collect(Collectors.toList());
                modelo.put("clasesProfesor", proximasClases);
                modelo.put("clasesReservadas", proximasClases);


                List<Archivo> archivosRecientes = servicioArchivo.obtenerArchivosRecientes(profesor.getId(), "profesor", 5);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                modelo.put("archivosRecientes", archivosRecientes);
                modelo.put("fechaFormatter", formatter);

            } else if(rol.equals("alumno")){
                Alumno alumno = (Alumno) usuario;
                modelo.put("listaProfesores", servicioLogin.obtenerProfesoresDeAlumno(alumno.getId()));
                List<Clase> todasLasClases = servicioLogin.obtenerClasesAlumno(alumno.getId());
                List<Clase> proximasClases = todasLasClases.stream()
                        .limit(5)
                        .collect(Collectors.toList());
                modelo.put("clasesReservadas", proximasClases);



                List<Archivo> archivosRecientes = servicioArchivo.obtenerArchivosRecientes(alumno.getId(), "alumno", 5);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                modelo.put("archivosRecientes", archivosRecientes);
                modelo.put("fechaFormatter", formatter);

                List<Mensaje> mensajesRecientes = servicioChat.obtenerUltimasConversaciones(usuario, 3); // Limite de 3 mensajes
                modelo.put("mensajesRecientes", mensajesRecientes);
            }
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

        if (datosRegistroProfesor.getLatitud() == null || datosRegistroProfesor.getLongitud() == null) {
            model.put("error", "Debe seleccionar una ubicación en el mapa");
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
            nuevoProfesor.setLatitud(datosRegistroProfesor.getLatitud());
            nuevoProfesor.setLongitud(datosRegistroProfesor.getLongitud());
// lo registro con los datos
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

    @RequestMapping(path = "/mis-clases", method = RequestMethod.GET)
    public ModelAndView verTodasMisClases(HttpServletRequest request) {
        ModelMap modelo = new ModelMap();
        Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");
        String rol = definirRol(usuario);

        if (usuario != null) {
            modelo.put("nombreUsuario", usuario.getNombre());
            modelo.put("rol", rol);

            List<Clase> todasLasClases = null;
            if (rol.equals("profesor")) {
                Profesor profesor = (Profesor) usuario;
                todasLasClases = servicioLogin.obtenerClasesProfesor(profesor.getId());
                modelo.put("temaProfesor", profesor.getTema());
            } else if (rol.equals("alumno")) {
                Alumno alumno = (Alumno) usuario;
                todasLasClases = servicioLogin.obtenerClasesAlumno(alumno.getId());
            }
            modelo.put("todasLasClases", todasLasClases);
        } else {
            // Si no hay usuario en sesión, redirigir al login
            return new ModelAndView("redirect:/login");
        }

        return new ModelAndView("todas-mis-clases", modelo);
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public ModelAndView cerrarSesion(HttpServletRequest request) {
        request.getSession().invalidate();
        return new ModelAndView("redirect:/home");
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


    private String definirRol(Usuario usuario) {
        if(usuario!=null) {
            if(usuario instanceof Profesor) {
                return "profesor";
            } else if (usuario instanceof Alumno) {
                return "alumno";
            }

        }
        return "usuario";
    }

}




