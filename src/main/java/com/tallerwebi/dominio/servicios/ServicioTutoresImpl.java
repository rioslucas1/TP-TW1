package com.tallerwebi.dominio.servicios;


import com.tallerwebi.dominio.RepositorioProfesor;
import com.tallerwebi.dominio.entidades.Profesor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("ServicioTutores")

public class ServicioTutoresImpl implements ServicioTutores {
    @Autowired
    private RepositorioProfesor repositorioProfesor;

    @Override
    public List<Profesor> obtenerTutoresFiltrados(String categoria, String modalidad, String duracion, String busqueda) {
        return repositorioProfesor.buscarTutoresFiltrados(categoria, modalidad, duracion, busqueda);
    }
}
