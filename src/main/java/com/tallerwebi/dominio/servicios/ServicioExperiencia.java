package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.entidades.ExperienciaEstudiantil;

import java.util.List;

public interface ServicioExperiencia  {

    List<ExperienciaEstudiantil> obtenerExperienciasPorProfesor(Long profesorId);
    ExperienciaEstudiantil guardar(ExperienciaEstudiantil experiencia);
    void eliminar(Long experienciaId);
    ExperienciaEstudiantil obtenerPorId(Long id);
    List<ExperienciaEstudiantil> obtenerTodas();
}
