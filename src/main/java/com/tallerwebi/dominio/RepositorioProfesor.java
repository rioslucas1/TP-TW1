package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.ModalidadPreferida;
import com.tallerwebi.dominio.entidades.Profesor;

import java.util.List;

public interface RepositorioProfesor {
    List<Profesor> obtenerTodos();
    List<Profesor> buscarPorFiltros(Long temaId, ModalidadPreferida modalidad);
}
