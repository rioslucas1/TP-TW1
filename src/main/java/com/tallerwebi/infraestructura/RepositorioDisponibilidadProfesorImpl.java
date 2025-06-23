package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioDisponibilidadProfesor;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Clase;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import javax.persistence.Query;


import java.time.LocalDate;
import java.util.List;

@Repository("repositorioDisponibilidadProfesor")
public class RepositorioDisponibilidadProfesorImpl implements RepositorioDisponibilidadProfesor {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioDisponibilidadProfesorImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void guardar(Clase disponibilidad) {
        if (disponibilidad != null) {
            sessionFactory.getCurrentSession().saveOrUpdate(disponibilidad);
        }
    }

    @Override
    public void eliminar(Clase disponibilidad) {
        if (disponibilidad != null) {
            sessionFactory.getCurrentSession().delete(disponibilidad);
        }
    }

    @Override
    public List<Clase> buscarPorProfesor(Profesor profesor) {
        if (profesor == null) {
            return List.of(); // Retorna lista vacía si el profesor es null
        }

        final Session session = sessionFactory.getCurrentSession();
        String hql = "FROM Clase WHERE profesor = :profesor ORDER BY diaSemana, hora";
        Query query = session.createQuery(hql);
        query.setParameter("profesor", profesor);
        return query.getResultList();
    }

    @Override
    public Clase buscarPorProfesorDiaHora(Profesor profesor, String diaSemana, String hora) {
        if (profesor == null || diaSemana == null || hora == null) {
            return null;
        }

        final Session session = sessionFactory.getCurrentSession();
        String hql = "FROM Clase WHERE profesor = :profesor AND diaSemana = :diaSemana AND hora = :hora";
        Query query = session.createQuery(hql);
        query.setParameter("profesor", profesor);
        query.setParameter("diaSemana", diaSemana);
        query.setParameter("hora", hora);

        List<Clase> resultados = query.getResultList();
        return resultados.isEmpty() ? null : resultados.get(0);
    }

    @Override
    public List<Clase> buscarPorProfesorDiaFecha(
            Profesor profesor, String diaSemana, LocalDate fechaEspecifica) {
        if (profesor == null || diaSemana == null || fechaEspecifica == null) {
            return List.of(); // Retorna lista vacía si algún parámetro es null
        }

        final Session session = sessionFactory.getCurrentSession();
        String hql = "FROM Clase WHERE profesor = :profesor AND diaSemana = :diaSemana AND fechaEspecifica = :fechaEspecifica ORDER BY hora";
        Query query = session.createQuery(hql);
        query.setParameter("profesor", profesor);
        query.setParameter("diaSemana", diaSemana);
        query.setParameter("fechaEspecifica", fechaEspecifica);
        return query.getResultList();
    }

    @Override
    public Clase buscarPorProfesorDiaHoraFecha(
            Profesor profesor, String diaSemana, String hora, LocalDate fechaEspecifica) {
        if (profesor == null || diaSemana == null || hora == null || fechaEspecifica == null) {
            return null;
        }

        final Session session = sessionFactory.getCurrentSession();
        String hql = "FROM Clase WHERE profesor = :profesor AND diaSemana = :diaSemana AND hora = :hora AND fechaEspecifica = :fechaEspecifica";
        Query query = session.createQuery(hql);
        query.setParameter("profesor", profesor);
        query.setParameter("diaSemana", diaSemana);
        query.setParameter("hora", hora);
        query.setParameter("fechaEspecifica", fechaEspecifica);

        List<Clase> resultados = query.getResultList();
        return resultados.isEmpty() ? null : resultados.get(0);
    }


}