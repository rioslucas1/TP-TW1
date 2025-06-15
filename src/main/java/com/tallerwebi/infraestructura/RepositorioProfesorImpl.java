package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.RepositorioProfesor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class RepositorioProfesorImpl implements RepositorioProfesor {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Profesor> obtenerTodos() {
        return entityManager.createQuery("FROM Profesor", Profesor.class).getResultList();
    }
}
