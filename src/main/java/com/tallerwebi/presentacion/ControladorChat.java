package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Mensaje;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.dominio.servicios.ServicioChat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class ControladorChat {

    private final ServicioChat servicioChat;
    private final RepositorioUsuario repositorioUsuario;

    @Autowired
    public ControladorChat(ServicioChat servicioChat, RepositorioUsuario repositorioUsuario) {
        this.servicioChat = servicioChat;
        this.repositorioUsuario = repositorioUsuario;
    }

    @GetMapping("/chat")
    public ModelAndView verChat(@RequestParam(required = false) String con,
                                HttpServletRequest request) {

        ModelMap modelo = new ModelMap();
        Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");

        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }

        modelo.put("nombreUsuario", usuario.getNombre());

        String rol = usuario.getClass().getSimpleName().toUpperCase();
        modelo.put("rol", rol);

        if ("ALUMNO".equals(rol)) {
            List<Usuario> profesores = repositorioUsuario.buscarPorTipo(com.tallerwebi.dominio.entidades.Profesor.class);
            modelo.put("contactos", profesores);
        } else if ("PROFESOR".equals(rol)) {
            List<Usuario> alumnos = repositorioUsuario.buscarPorTipo(com.tallerwebi.dominio.entidades.Alumno.class);
            modelo.put("contactos", alumnos);
        }

        if (con != null && !con.trim().isEmpty()) {
            modelo.put("receptor", con);
            List<Mensaje> mensajes;
            if ("ALUMNO".equals(rol)) {
                mensajes = servicioChat.obtenerConversacion(usuario.getNombre(), con);
            } else {
                mensajes = servicioChat.obtenerConversacion(con, usuario.getNombre());
            }
            modelo.put("mensajes", mensajes);
        }

        return new ModelAndView("chat", modelo);
    }


    @PostMapping("/chat/enviar")
    public ModelAndView enviarMensaje(@RequestParam String receptor,
                                      @RequestParam String contenido,
                                      HttpServletRequest request) {
        Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");

        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }

        servicioChat.enviarMensaje(usuario.getNombre(), receptor, contenido);

        return new ModelAndView("redirect:/chat?con=" + receptor);
    }
}