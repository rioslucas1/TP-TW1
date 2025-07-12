package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.entidades.ModalidadPreferida;
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

    @Override
    public List<Profesor> buscarPorFiltros(Long temaId, ModalidadPreferida modalidad) {
        StringBuilder jpql = new StringBuilder("SELECT p FROM Profesor p WHERE 1=1");

        if (temaId != null) {
            jpql.append(" AND p.tema.id = :temaId");
        }
        if (modalidad != null) {
            jpql.append(" AND p.modalidad = :modalidad");
        }

        var query = entityManager.createQuery(jpql.toString(), Profesor.class);

        if (temaId != null) {
            query.setParameter("temaId", temaId);
        }
        if (modalidad != null) {
            query.setParameter("modalidad", modalidad);
        }

        return query.getResultList();
    }



}
