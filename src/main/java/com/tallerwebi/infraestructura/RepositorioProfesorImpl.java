package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioProfesor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class RepositorioProfesorImpl implements RepositorioProfesor {

    @Override
    public List<String> obtenerTodos() {
        List<String> profesores = new ArrayList<String>();
        String string1 = "profesro1";
        String string2 = "profesor2";
        profesores.add(string1);
        profesores.add(string2);
        return profesores;
    }
}
