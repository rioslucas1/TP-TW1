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

    @Override
    public List<Mensaje> obtenerConversacion(String emisor, String receptor) {
        return entityManager.createQuery(
                        "FROM Mensaje m WHERE " +
                                "(m.emisor = :emisor AND m.receptor = :receptor) OR " +
                                "(m.emisor = :receptor AND m.receptor = :emisor) " +
                                "ORDER BY m.fecha ASC", Mensaje.class)
                .setParameter("emisor", emisor)
                .setParameter("receptor", receptor)
                .getResultList();
    }
}