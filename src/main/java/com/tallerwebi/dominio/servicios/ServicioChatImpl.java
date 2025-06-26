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
        Mensaje mensaje = new Mensaje();

        // Buscar Alumno y Profesor por nombre:
        Alumno alumno = repositorioUsuario.buscarAlumnoPorNombre(emisorNombre);
        Profesor profesor = repositorioUsuario.buscarProfesorPorNombre(emisorNombre);

        if (alumno != null) {
            // El emisor es alumno, entonces el receptor es profesor:
            mensaje.setAlumno(alumno);
            Profesor receptorProfesor = repositorioUsuario.buscarProfesorPorNombre(receptorNombre);
            mensaje.setProfesor(receptorProfesor);
            mensaje.setEmisor("ALUMNO");
        } else if (profesor != null) {
            // El emisor es profesor, receptor es alumno:
            mensaje.setProfesor(profesor);
            Alumno receptorAlumno = repositorioUsuario.buscarAlumnoPorNombre(receptorNombre);
            mensaje.setAlumno(receptorAlumno);
            mensaje.setEmisor("PROFESOR");
        }

        mensaje.setContenido(contenido);
        mensaje.setFecha(LocalDateTime.now());

        repositorioMensaje.guardar(mensaje);
    }

    @Override
    public List<Mensaje> obtenerConversacion(String emisor, String receptor) {
        return repositorioMensaje.obtenerConversacion(emisor, receptor);
    }
}