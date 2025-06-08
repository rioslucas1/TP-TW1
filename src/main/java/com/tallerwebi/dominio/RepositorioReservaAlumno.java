package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.disponibilidadProfesor;

import java.util.List;

public interface RepositorioReservaAlumno {
    List<disponibilidadProfesor> buscarPorProfesor(String emailProfesor);
    disponibilidadProfesor buscarPorProfesorDiaHora(String emailProfesor, String diaSemana, String hora);

    void guardar(disponibilidadProfesor disponibilidadExistente);
}