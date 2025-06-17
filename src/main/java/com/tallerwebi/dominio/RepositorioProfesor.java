package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Profesor;

import java.util.List;

public interface RepositorioProfesor {
    List<Profesor> obtenerTodos();
    List<Profesor> buscarTutoresFiltrados(String categoria, String modalidad, String duracion, String busqueda);
}
