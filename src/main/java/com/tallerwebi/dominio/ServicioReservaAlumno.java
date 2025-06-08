package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.disponibilidadProfesor;

import java.util.List;

public interface ServicioReservaAlumno {
    List<disponibilidadProfesor> obtenerDisponibilidadProfesor(String emailProfesor);

    void reservarClase(String email, String diaLimpio, String horaLimpia);
}
