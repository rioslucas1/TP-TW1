package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.entidades.Tema;

import java.util.List;

public interface ServicioTema {

    List<Tema> obtenerTodos();
    Tema obtenerPorId(Long id);
}
