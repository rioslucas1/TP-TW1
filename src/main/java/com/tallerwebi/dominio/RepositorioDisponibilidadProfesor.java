package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.disponibilidadProfesor;

import java.time.LocalDate;
import java.util.List;

public interface RepositorioDisponibilidadProfesor {
    void guardar(disponibilidadProfesor disponibilidad);
    void eliminar(disponibilidadProfesor disponibilidad);
    List<disponibilidadProfesor> buscarPorProfesor(Profesor profesor);
    disponibilidadProfesor buscarPorProfesorDiaHora(Profesor profesor, String diaSemana, String hora);
    List<disponibilidadProfesor> buscarPorProfesorDiaFecha(Profesor profesor, String diaSemana, LocalDate fechaEspecifica);
    disponibilidadProfesor buscarPorProfesorDiaHoraFecha(Profesor profesor, String diaSemana, String hora, LocalDate fechaEspecifica);
}