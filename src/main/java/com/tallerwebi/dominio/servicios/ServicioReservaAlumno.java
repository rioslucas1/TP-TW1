package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Clase;

import java.time.LocalDate;
import java.util.List;

public interface ServicioReservaAlumno {
    List<Clase> obtenerDisponibilidadProfesor(String emailProfesor);

    void reservarClase(String emailProfesor, String diaSemana, String hora, Alumno alumno);
    void reservarClasePorId(Long disponibilidadId, Alumno alumno);
    List<Clase> obtenerDisponibilidadProfesorPorSemana(String emailProfesor, LocalDate fechaInicioSemana);
    Clase obtenerDisponibilidadPorId(Long disponibilidadId);

    boolean estaSuscritoAProfesor(Long id, String emailProfesor);
}
