package com.tallerwebi.dominio.servicios;
import com.tallerwebi.dominio.entidades.EstadoDisponibilidad;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Clase;

import java.time.LocalDate;
import java.util.List;

public interface ServicioDisponibilidadProfesor {


    void toggleDisponibilidad(Profesor profesor, String diaSemana, String hora);
    void cambiarEstadoDisponibilidad(Profesor profesor, String diaSemana, String hora, EstadoDisponibilidad nuevoEstado);
    void desagendarHorario(Profesor profesor, String diaSemana, String hora);
    void marcarComoReservado(Profesor profesor, String diaSemana, String hora);
    void reservarHorario(Profesor profesor, String diaSemana, String hora);
    List<Clase> obtenerDisponibilidadProfesor(Profesor profesor);
    Clase obtenerDisponibilidadEspecifica(Profesor profesor, String diaSemana, String hora);
    List<Clase> obtenerDisponibilidadProfesorPorSemana(Profesor profesor, LocalDate fechaInicioSemana);
    void toggleDisponibilidadConFecha(Profesor profesor, String diaSemana, String hora, LocalDate fechaEspecifica);
}