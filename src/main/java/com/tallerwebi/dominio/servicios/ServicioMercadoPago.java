package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.entidades.Usuario;

public interface ServicioMercadoPago {
    String crearPreferencia(Usuario usuario) throws Exception;
}