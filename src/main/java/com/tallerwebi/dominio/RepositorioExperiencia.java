package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.ExperienciaEstudiantil;

import java.util.List;

public interface RepositorioExperiencia {
    List<ExperienciaEstudiantil> obtenerPorProfesorId(Long profesorId);
    ExperienciaEstudiantil guardar(ExperienciaEstudiantil experiencia);
    void eliminar(Long experienciaId);
    ExperienciaEstudiantil buscarPorId(Long id);
    List<ExperienciaEstudiantil> obtenerTodas();
}
