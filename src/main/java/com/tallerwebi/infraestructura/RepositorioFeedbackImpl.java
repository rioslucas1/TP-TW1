package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioFeedback;
import com.tallerwebi.dominio.entidades.FeedbackProfesor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.util.List;

@Repository("repositorioFeedback")
public class RepositorioFeedbackImpl implements RepositorioFeedback {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioFeedbackImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<FeedbackProfesor> obtenerPorProfesorId(Long profesorId) {
        final Session session = sessionFactory.getCurrentSession();
        String hql = "FROM FeedbackProfesor f WHERE f.profesor.id = :profesorId";
        Query query = session.createQuery(hql);
        query.setParameter("profesorId", profesorId);
        return query.getResultList();
    }

    @Override
    public FeedbackProfesor guardar(FeedbackProfesor feedback) {
        sessionFactory.getCurrentSession().saveOrUpdate(feedback);
        return feedback;
    }

    @Override
    public void eliminar(Long feedbackId) {
        FeedbackProfesor feedback = buscarPorId(feedbackId);
        if (feedback != null) {
            sessionFactory.getCurrentSession().delete(feedback);
        }
    }

    @Override
    public FeedbackProfesor buscarPorId(Long id) {
        final Session session = sessionFactory.getCurrentSession();
        return session.get(FeedbackProfesor.class, id);
    }

    @Override
    public List<FeedbackProfesor> obtenerTodos() {
        final Session session = sessionFactory.getCurrentSession();
        String hql = "FROM FeedbackProfesor";
        Query query = session.createQuery(hql);
        return query.getResultList();
    }
    @Override
    public boolean existeFeedbackDeAlumnoParaProfesor(Long alumnoId, Long profesorId) {
        final Session session = sessionFactory.getCurrentSession();
        String hql = "SELECT COUNT(f) FROM FeedbackProfesor f WHERE f.alumno.id = :alumnoId AND f.profesor.id = :profesorId";
        Query query = session.createQuery(hql);
        query.setParameter("alumnoId", alumnoId);
        query.setParameter("profesorId", profesorId);
        Long count = (Long) query.getSingleResult();
        return count > 0;
    }
    @Override
    public FeedbackProfesor buscarPorAlumnoYProfesor(Long alumnoId, Long profesorId) {
        final Session session = sessionFactory.getCurrentSession();
        String hql = "FROM FeedbackProfesor f WHERE f.alumno.id = :alumnoId AND f.profesor.id = :profesorId";
        Query query = session.createQuery(hql);
        query.setParameter("alumnoId", alumnoId);
        query.setParameter("profesorId", profesorId);
        query.setMaxResults(1);
        List<FeedbackProfesor> resultados = query.getResultList();
        return resultados.isEmpty() ? null : resultados.get(0);
    }

}
