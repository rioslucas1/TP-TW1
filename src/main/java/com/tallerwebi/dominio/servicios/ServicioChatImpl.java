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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
            Profesor receptorProfesor = repositorioUsuario.buscarProfesorPorNombre(receptorNombre);
            mensaje.setAlumno(alumno);
            mensaje.setProfesor(receptorProfesor);
            mensaje.setEmisor("ALUMNO");
        } else if (profesor != null) {
            Alumno receptorAlumno = repositorioUsuario.buscarAlumnoPorNombre(receptorNombre);
            mensaje.setProfesor(profesor);
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

    @Override
    public List<Mensaje> obtenerUltimasConversaciones(Usuario usuario, int limite) {
        List<Mensaje> todosLosMensajes = repositorioMensaje.buscarTodosLosMensajesDeUsuario(usuario.getId());
        Map<Long, Mensaje> ultimasConversaciones = new LinkedHashMap<>(); // Preserva el orden de inserción

        for (Mensaje mensaje : todosLosMensajes) {
            Long idOtroUsuario;
            // Determina el ID del otro usuario en la conversación
            if (mensaje.getAlumno().getId().equals(usuario.getId())) {
                idOtroUsuario = mensaje.getProfesor().getId();
            } else {
                idOtroUsuario = mensaje.getAlumno().getId();
            }

            // Como los mensajes están ordenados por fecha desc, el primero que encontramos
            // para un chat es el más reciente. Lo agregamos al mapa si aún no existe.
            if (!ultimasConversaciones.containsKey(idOtroUsuario)) {
                ultimasConversaciones.put(idOtroUsuario, mensaje);
            }
        }

        List<Mensaje> resultado = new ArrayList<>(ultimasConversaciones.values());

        // Devuelve la lista limitada por el parámetro 'limite'
        return resultado.subList(0, Math.min(resultado.size(), limite));
    }


}