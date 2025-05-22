package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioTema {

    List<Tema> obtenerTodos();
    Tema buscarPorId(Long id);
}

