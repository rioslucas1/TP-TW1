package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.entidades.Profesor;

import java.util.List;

public interface ServicioTutores {

    List<Profesor> obtenerTutoresFiltrados(String categoria, String modalidad, String duracion, String busqueda);
}


