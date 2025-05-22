package com.tallerwebi.dominio;

import java.util.List;

public interface ServicioTema {

    List<Tema> obtenerTodos();
    Tema obtenerPorId(Long id);
}
