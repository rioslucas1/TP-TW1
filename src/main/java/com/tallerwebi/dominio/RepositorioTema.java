package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Tema;

import java.util.List;

public interface RepositorioTema {

    List<Tema> obtenerTodos();
    Tema buscarPorId(Long id);
    void guardar(Tema tema);
    void modificar(Tema tema);
    void eliminar(Tema tema);
    Tema buscarPorNombre(String nombre);

}

