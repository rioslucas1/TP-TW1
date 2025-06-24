package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Clase;

import java.time.LocalDate;
import java.util.List;

public interface RepositorioReservaAlumno {
    List<Clase> buscarPorProfesor(String emailProfesor);
    Clase buscarPorProfesorDiaHora(String emailProfesor, String diaSemana, String hora);
    void guardar(Clase disponibilidadExistente);

    Clase buscarPorId(Long disponibilidadId);

    List<Clase> buscarPorProfesorDiaFecha(String emailProfesor, String diaSemana, LocalDate fechaEspecifica);
}