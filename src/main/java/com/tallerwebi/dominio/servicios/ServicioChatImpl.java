package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.RepositorioMensaje;
import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Mensaje;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ServicioChatImpl implements ServicioChat {

    private final RepositorioMensaje repositorioMensaje;
    private final RepositorioUsuario repositorioUsuario;

    @Autowired
    public ServicioChatImpl(RepositorioMensaje repositorioMensaje, RepositorioUsuario repositorioUsuario) {
        this.repositorioMensaje = repositorioMensaje;
        this.repositorioUsuario = repositorioUsuario;
    }

    @Override
    public void enviarMensaje(String emisorNombre, String receptorNombre, String contenido) {
        Usuario emisor = repositorioUsuario.buscarPorNombre(emisorNombre);
        Usuario receptor = repositorioUsuario.buscarPorNombre(receptorNombre);

        Mensaje mensaje = new Mensaje();
        mensaje.setEmisor(emisor.getNombre());
        mensaje.setContenido(contenido);
        mensaje.setFecha(LocalDateTime.now());

        // Asignar roles según clase
        if (emisor instanceof Alumno && receptor instanceof Profesor) {
            mensaje.setAlumno((Alumno) emisor);
            mensaje.setProfesor((Profesor) receptor);
        } else if (emisor instanceof Profesor && receptor instanceof Alumno) {
            mensaje.setProfesor((Profesor) emisor);
            mensaje.setAlumno((Alumno) receptor);
        }

        repositorioMensaje.guardar(mensaje);
    }

    @Override
    public List<Mensaje> obtenerConversacion(String emisor, String receptor) {
        return repositorioMensaje.obtenerConversacion(emisor, receptor);
    }
}