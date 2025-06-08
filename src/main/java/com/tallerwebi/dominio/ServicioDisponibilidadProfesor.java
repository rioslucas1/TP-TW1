package com.tallerwebi.dominio;
import com.tallerwebi.dominio.entidades.EstadoDisponibilidad;
import com.tallerwebi.dominio.entidades.disponibilidadProfesor;
import java.util.List;

public interface ServicioDisponibilidadProfesor {
    void toggleDisponibilidad(String emailProfesor, String diaSemana, String hora);
    List<disponibilidadProfesor> obtenerDisponibilidadProfesor(String emailProfesor);
    void cambiarEstadoDisponibilidad(String emailProfesor, String diaSemana, String hora, EstadoDisponibilidad nuevoEstado);
    void reservarHorario(String emailProfesor, String diaSemana, String hora);
    void desagendarHorario(String emailProfesor, String diaSemana, String hora);
    void marcarComoReservado(String emailProfesor, String diaSemana, String hora);
    disponibilidadProfesor obtenerDisponibilidadEspecifica(String emailProfesor, String diaSemana, String hora);
}