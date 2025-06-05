package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.disponibilidadProfesor;
import java.util.List;

public interface RepositorioDisponibilidadProfesor {
    void guardar(disponibilidadProfesor disponibilidad);
    void eliminar(disponibilidadProfesor disponibilidad);
    List<disponibilidadProfesor> buscarPorProfesor(String emailProfesor);
    disponibilidadProfesor buscarPorProfesorDiaHora(String emailProfesor, String diaSemana, String hora);
}