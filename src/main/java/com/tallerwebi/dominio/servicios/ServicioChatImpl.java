package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.RepositorioMensaje;
import com.tallerwebi.dominio.entidades.Mensaje;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServicioChatImpl implements ServicioChat {

    private final RepositorioMensaje repositorioMensaje;

    @Autowired
    public ServicioChatImpl(RepositorioMensaje repositorioMensaje) {
        this.repositorioMensaje = repositorioMensaje;
    }

    @Override
    public void enviarMensaje(String emisor, String receptor, String contenido) {
        Mensaje mensaje = new Mensaje();
        mensaje.setEmisor(emisor);
        mensaje.setReceptor(receptor);
        mensaje.setContenido(contenido);
        mensaje.setFecha(LocalDateTime.now());

        repositorioMensaje.guardar(mensaje);
    }

    @Override
    public List<Mensaje> obtenerConversacion(String emisor, String receptor) {
        return repositorioMensaje.obtenerConversacion(emisor, receptor);
    }
}