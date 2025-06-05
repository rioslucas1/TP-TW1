package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.disponibilidadProfesor;
import java.util.List;

public interface ServicioDisponibilidadProfesor {
    void toggleDisponibilidad(String emailProfesor, String diaSemana, String hora);
    List<disponibilidadProfesor> obtenerDisponibilidadProfesor(String emailProfesor);
}