package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.entidades.disponibilidadProfesor;

import java.time.LocalDate;
import java.util.List;

public interface ServicioReservaAlumno {
    List<disponibilidadProfesor> obtenerDisponibilidadProfesor(String emailProfesor);

    void reservarClase(String email, String diaLimpio, String horaLimpia, String emailAlumno);

    List<disponibilidadProfesor> obtenerDisponibilidadProfesorPorSemana(String emailProfesor, LocalDate fechaInicioSemana);

    void reservarClasePorId(Long disponibilidadId, String email);
}
