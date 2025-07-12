package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Mensaje;

import java.util.List;

public interface RepositorioMensaje {
    void guardar(Mensaje mensaje);
    List<Mensaje> obtenerConversacion(String emisor, String receptor);
}