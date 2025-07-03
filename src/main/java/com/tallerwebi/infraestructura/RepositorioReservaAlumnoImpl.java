package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioReservaAlumno;
import com.tallerwebi.dominio.entidades.Clase;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository("repositorioReservaAlumno")
public class RepositorioReservaAlumnoImpl implements RepositorioReservaAlumno {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioReservaAlumnoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Clase> buscarPorProfesor(String emailProfesor) {
        final Session session = sessionFactory.getCurrentSession();
        try {
            if (emailProfesor == null) {
                return new ArrayList<>();
            }

            String hql = "FROM Clase d " +
                    "LEFT JOIN FETCH d.profesor p " +
                    "LEFT JOIN FETCH d.alumno a " +
                    "WHERE p.email = :emailProfesor";

            Query query = session.createQuery(hql);
            query.setParameter("emailProfesor", emailProfesor);
            return query.getResultList();

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public Clase buscarPorProfesorDiaHora(String emailProfesor, String diaSemana, String hora) {
        final Session session = sessionFactory.getCurrentSession();
        try {
            if (emailProfesor == null || diaSemana == null || hora == null) {
                return null;
            }
            String hql = "FROM Clase d " +
                    "LEFT JOIN FETCH d.profesor p " +
                    "LEFT JOIN FETCH d.alumno a " +
                    "WHERE p.email = :emailProfesor " +
                    "AND d.diaSemana = :diaSemana " +
                    "AND d.hora = :hora";

            Query query = session.createQuery(hql);
            query.setParameter("emailProfesor", emailProfesor);
            query.setParameter("diaSemana", diaSemana);
            query.setParameter("hora", hora);
            List<Clase> resultados = query.getResultList();
            return resultados.isEmpty() ? null : resultados.get(0);

        } catch (Exception e) {
            return null;
        }
    }

    public Clase buscarPorId(Long id) {
        final Session session = sessionFactory.getCurrentSession();
        try {
            if (id == null) {
                return null;
            }
            String hql = "FROM Clase d " +
                    "LEFT JOIN FETCH d.profesor p " +
                    "LEFT JOIN FETCH d.alumno a " +
                    "WHERE d.id = :id";
            Query query = session.createQuery(hql);
            query.setParameter("id", id);

            List<Clase> resultados = query.getResultList();
            return resultados.isEmpty() ? null : resultados.get(0);

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Clase> obtenerTodasLasClasesPorProfesor(Long profesorId) {
        final Session session = sessionFactory.getCurrentSession();
        // Modificación: Usar HQL en lugar de Criteria y Restrictions
        return session.createQuery("FROM Clase c JOIN FETCH c.profesor p WHERE p.id = :profesorId AND c.alumno IS NOT NULL" , Clase.class)
                .setParameter("profesorId", profesorId)
                .list();
    }


    @Override
    public void guardar(Clase disponibilidad) {
        sessionFactory.getCurrentSession().saveOrUpdate(disponibilidad);
    }

    @Override
    public List<Clase> buscarClasesPorProfesorYAlumno(String emailProfesor, String emailAlumno) {
        final Session session = sessionFactory.getCurrentSession();
        try {
            if (emailProfesor == null || emailAlumno == null) {
                return new ArrayList<>();
            }

            String hql = "FROM Clase c " +
                    "LEFT JOIN FETCH c.profesor p " +
                    "LEFT JOIN FETCH c.alumno a " +
                    "WHERE p.email = :emailProfesor AND a.email = :emailAlumno";

            Query query = session.createQuery(hql);
            query.setParameter("emailProfesor", emailProfesor);
            query.setParameter("emailAlumno", emailAlumno);

            return query.getResultList();

        } catch (Exception e) {
            System.err.println("Error al buscar clases por profesor y alumno: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Clase> buscarPorProfesorDiaFecha(
            String emailProfesor, String diaSemana, LocalDate fechaEspecifica) {
        final Session session = sessionFactory.getCurrentSession();
        try {
            if (emailProfesor == null || diaSemana == null || fechaEspecifica == null) {
                return new ArrayList<>();
            }

            String hql = "FROM Clase d " +
                    "LEFT JOIN FETCH d.profesor p " +
                    "LEFT JOIN FETCH d.alumno a " +
                    "WHERE p.email = :emailProfesor " +
                    "AND d.diaSemana = :diaSemana " +
                    "AND d.fechaEspecifica = :fechaEspecifica";

            Query query = session.createQuery(hql);
            query.setParameter("emailProfesor", emailProfesor);
            query.setParameter("diaSemana", diaSemana);
            query.setParameter("fechaEspecifica", fechaEspecifica);

            return query.getResultList();

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}