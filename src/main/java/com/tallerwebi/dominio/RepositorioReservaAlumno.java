package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.disponibilidadProfesor;

import java.time.LocalDate;
import java.util.List;

public interface RepositorioReservaAlumno {
    List<disponibilidadProfesor> buscarPorProfesor(String emailProfesor);
    disponibilidadProfesor buscarPorProfesorDiaHora(String emailProfesor, String diaSemana, String hora);
    void guardar(disponibilidadProfesor disponibilidadExistente);

    disponibilidadProfesor buscarPorId(Long disponibilidadId);

    List<disponibilidadProfesor> buscarPorProfesorDiaFecha(String emailProfesor, String diaSemana, LocalDate fechaEspecifica);
}