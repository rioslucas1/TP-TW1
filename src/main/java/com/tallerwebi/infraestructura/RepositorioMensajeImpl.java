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
    public List<Mensaje> obtenerConversacion(String alumnoNombre, String profesorNombre) {
        return entityManager.createQuery(
                        "FROM Mensaje m WHERE " +
                                "(m.alumno.nombre = :alumnoNombre AND m.profesor.nombre = :profesorNombre) OR " +
                                "(m.alumno.nombre = :profesorNombre AND m.profesor.nombre = :alumnoNombre) " +
                                "ORDER BY m.fecha ASC", Mensaje.class)
                .setParameter("alumnoNombre", alumnoNombre)
                .setParameter("profesorNombre", profesorNombre)
                .getResultList();
    }

    @Override
    public List<Mensaje> buscarTodosLosMensajesDeUsuario(Long usuarioId) {
        return entityManager.createQuery(
                        "FROM Mensaje m " +
                                "WHERE m.alumno.id = :usuarioId OR m.profesor.id = :usuarioId " +
                                "ORDER BY m.fecha DESC", Mensaje.class)
                .setParameter("usuarioId", usuarioId)
                .getResultList();
    }
}