package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Clase;

import java.time.LocalDate;
import java.util.List;

public interface RepositorioDisponibilidadProfesor {
    void guardar(Clase disponibilidad);
    void eliminar(Clase disponibilidad);
    List<Clase> buscarPorProfesor(Profesor profesor);
    Clase buscarPorProfesorDiaHora(Profesor profesor, String diaSemana, String hora);
    List<Clase> buscarPorProfesorDiaFecha(Profesor profesor, String diaSemana, LocalDate fechaEspecifica);
    Clase buscarPorProfesorDiaHoraFecha(Profesor profesor, String diaSemana, String hora, LocalDate fechaEspecifica);
}