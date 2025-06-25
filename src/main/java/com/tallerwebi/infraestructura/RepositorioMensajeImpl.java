package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioMensaje;
import com.tallerwebi.dominio.entidades.Mensaje;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class RepositorioMensajeImpl implements RepositorioMensaje {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void guardar(Mensaje mensaje) {
        entityManager.persist(mensaje);
    }

    public List<Mensaje> obtenerConversacion(String nombre1, String nombre2) {
        return entityManager.createQuery(
                        "FROM Mensaje m WHERE " +
                                "(m.alumno.nombre = :nombre1 AND m.profesor.nombre = :nombre2) OR " +
                                "(m.alumno.nombre = :nombre2 AND m.profesor.nombre = :nombre1) " +
                                "ORDER BY m.fecha ASC", Mensaje.class)
                .setParameter("nombre1", nombre1)
                .setParameter("nombre2", nombre2)
                .getResultList();
    }
}