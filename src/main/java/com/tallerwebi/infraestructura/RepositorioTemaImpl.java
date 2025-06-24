package com.tallerwebi.infraestructura;
import java.util.List;

import com.tallerwebi.dominio.RepositorioTema;
import com.tallerwebi.dominio.entidades.Tema;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;

@Repository("repositorioTema")
public class RepositorioTemaImpl implements RepositorioTema {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioTemaImpl(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Tema> obtenerTodos() {
        final Session session = sessionFactory.getCurrentSession();
        String hql = "FROM Tema ORDER BY nombre ASC";
        Query query = session.createQuery(hql);
        return query.getResultList();
    }

    @Override
    public Tema buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        return (Tema) sessionFactory.getCurrentSession().get(Tema.class, id);
    }

    @Override
    public void guardar(Tema tema) {
        sessionFactory.getCurrentSession().save(tema);
    }

    @Override
    public void modificar(Tema tema) {
        sessionFactory.getCurrentSession().update(tema);
    }

    @Override
    public void eliminar(Tema tema) {
        sessionFactory.getCurrentSession().delete(tema);
    }

    @Override
    public Tema buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return null;
        }

        final Session session = sessionFactory.getCurrentSession();
        String hql = "FROM Tema WHERE nombre = :nombre";
        Query query = session.createQuery(hql);
        query.setParameter("nombre", nombre);

        List<Tema> resultados = query.getResultList();
        return resultados.isEmpty() ? null : resultados.get(0);
    }

}
