package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.disponibilidadProfesor;

import java.time.LocalDate;
import java.util.List;

public interface ServicioReservaAlumno {
    List<disponibilidadProfesor> obtenerDisponibilidadProfesor(String emailProfesor);

    void reservarClase(String emailProfesor, String diaSemana, String hora, Alumno alumno);
    void reservarClasePorId(Long disponibilidadId, Alumno alumno);
    List<disponibilidadProfesor> obtenerDisponibilidadProfesorPorSemana(String emailProfesor, LocalDate fechaInicioSemana);
    }
