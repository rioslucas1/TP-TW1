package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Tema;

import java.util.List;

public interface RepositorioTema {

    List<Tema> obtenerTodos();
    Tema buscarPorId(Long id);
}

