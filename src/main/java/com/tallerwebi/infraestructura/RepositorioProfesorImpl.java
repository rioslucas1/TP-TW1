package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.RepositorioProfesor;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RepositorioProfesorImpl implements RepositorioProfesor {

    @PersistenceContext
    private EntityManager entityManager;

    public RepositorioProfesorImpl(SessionFactory sessionFactory) {
    }

    @Override
    public List<Profesor> obtenerTodos() {
        return entityManager.createQuery("FROM Profesor", Profesor.class).getResultList();
    }
    @Override
    public List<Profesor> buscarTutoresFiltrados(String categoria, String modalidad, String duracion, String busqueda) {
        // Esto es solo un ejemplo que devuelve una lista vacía
        return new ArrayList<>();
    }

}
