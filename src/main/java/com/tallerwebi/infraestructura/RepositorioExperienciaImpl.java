package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioExperiencia;
import com.tallerwebi.dominio.entidades.ExperienciaEstudiantil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.List;

@Repository("repositorioExperiencia")
public class RepositorioExperienciaImpl implements RepositorioExperiencia {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioExperienciaImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<ExperienciaEstudiantil> obtenerPorProfesorId(Long profesorId) {
        final Session session = sessionFactory.getCurrentSession();
        String hql = "FROM ExperienciaEstudiantil e WHERE e.profesor.id = :profesorId";
        Query query = session.createQuery(hql);
        query.setParameter("profesorId", profesorId);
        return query.getResultList();
    }

    @Override
    public ExperienciaEstudiantil guardar(ExperienciaEstudiantil experiencia) {
        sessionFactory.getCurrentSession().saveOrUpdate(experiencia);
        return experiencia;
    }

    @Override
    public void eliminar(Long experienciaId) {
        ExperienciaEstudiantil experiencia = buscarPorId(experienciaId);
        if (experiencia != null) {
            sessionFactory.getCurrentSession().delete(experiencia);
        }
    }

    @Override
    public ExperienciaEstudiantil buscarPorId(Long id) {
        final Session session = sessionFactory.getCurrentSession();
        return session.get(ExperienciaEstudiantil.class, id);
    }

    @Override
    public List<ExperienciaEstudiantil> obtenerTodas() {
        final Session session = sessionFactory.getCurrentSession();
        String hql = "FROM ExperienciaEstudiantil";
        Query query = session.createQuery(hql);
        return query.getResultList();
    }

}
