package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.entidades.Mensaje;

import java.util.List;

public interface ServicioChat {
    void enviarMensaje(String emisor, String receptor, String contenido);
    List<Mensaje> obtenerConversacion(String emisor, String receptor);
}