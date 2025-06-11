package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.disponibilidadProfesor;

import java.time.LocalDate;
import java.util.List;

public interface RepositorioDisponibilidadProfesor {
    void guardar(disponibilidadProfesor disponibilidad);
    void eliminar(disponibilidadProfesor disponibilidad);
    List<disponibilidadProfesor> buscarPorProfesor(String emailProfesor);
    disponibilidadProfesor buscarPorProfesorDiaHora(String emailProfesor, String diaSemana, String hora);

    List<disponibilidadProfesor> buscarPorProfesorDiaFecha(String emailProfesor, String diaSemana, LocalDate fechaEspecifica);

    disponibilidadProfesor buscarPorProfesorDiaHoraFecha(String emailProfesor, String diaSemana, String hora, LocalDate fechaEspecifica);
}