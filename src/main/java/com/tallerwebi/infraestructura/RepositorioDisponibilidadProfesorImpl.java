package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioDisponibilidadProfesor;
import com.tallerwebi.dominio.entidades.disponibilidadProfesor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("repositorioDisponibilidadProfesor")
public class RepositorioDisponibilidadProfesorImpl implements RepositorioDisponibilidadProfesor {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioDisponibilidadProfesorImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void guardar(disponibilidadProfesor disponibilidad) {
        sessionFactory.getCurrentSession().save(disponibilidad);
    }

    @Override
    public void eliminar(disponibilidadProfesor disponibilidad) {
        sessionFactory.getCurrentSession().delete(disponibilidad);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<disponibilidadProfesor> buscarPorProfesor(String emailProfesor) {
        final Session session = sessionFactory.getCurrentSession();
        return session.createCriteria(disponibilidadProfesor.class)
                .add(Restrictions.eq("emailProfesor", emailProfesor))
                .list();
    }

    @Override
    public disponibilidadProfesor buscarPorProfesorDiaHora(String emailProfesor, String diaSemana, String hora) {
        final Session session = sessionFactory.getCurrentSession();
        return (disponibilidadProfesor) session.createCriteria(disponibilidadProfesor.class)
                .add(Restrictions.eq("emailProfesor", emailProfesor))
                .add(Restrictions.eq("diaSemana", diaSemana))
                .add(Restrictions.eq("hora", hora))
                .uniqueResult();
    }
}