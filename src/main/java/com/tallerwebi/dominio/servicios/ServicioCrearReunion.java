package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.entidades.Clase;

public interface ServicioCrearReunion {


    String crearReunionMeet(Clase disponibilidad);
    boolean eliminarReunionMeet(String enlaceMeet);

}
