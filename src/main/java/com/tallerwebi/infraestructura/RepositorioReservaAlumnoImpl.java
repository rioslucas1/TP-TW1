package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioReservaAlumno;
import com.tallerwebi.dominio.entidades.disponibilidadProfesor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("repositorioReservaAlumno")
public class RepositorioReservaAlumnoImpl implements RepositorioReservaAlumno {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioReservaAlumnoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
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

    @Override
    public void guardar(disponibilidadProfesor disponibilidad) {
        sessionFactory.getCurrentSession().saveOrUpdate(disponibilidad);
    }

}