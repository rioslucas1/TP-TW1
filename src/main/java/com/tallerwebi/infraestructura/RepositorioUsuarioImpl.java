package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

@Repository("repositorioUsuario")
public class RepositorioUsuarioImpl implements RepositorioUsuario {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioUsuarioImpl(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Usuario buscarUsuario(String email, String password) {
        final Session session = sessionFactory.getCurrentSession();
        return (Usuario) session.createCriteria(Usuario.class)
                .add(Restrictions.eq("email", email))
                .add(Restrictions.eq("password", password))
                .uniqueResult();
    }

    @Override
    public void guardar(Usuario usuario) {
        sessionFactory.getCurrentSession().save(usuario);
    }

    @Override
    public Usuario buscar(String email) {
        return (Usuario) sessionFactory.getCurrentSession().createCriteria(Usuario.class)
                .add(Restrictions.eq("email", email))
                .uniqueResult();
    }

    @Override
    public void modificar(Usuario usuario) {
        sessionFactory.getCurrentSession().update(usuario);
    }



    public <T extends Usuario> List<Usuario> buscarPorTipo(Class<T> tipo) {
        final Session session = sessionFactory.getCurrentSession();
        return session.createCriteria(tipo).list();
    }


    @Override
    public List<disponibilidadProfesor> obtenerClasesProfesor(Long profesorId) {
        final Session session = sessionFactory.getCurrentSession();
        LocalDate hoy = LocalDate.now();

        String hql = "FROM disponibilidadProfesor d " +
                "LEFT JOIN FETCH d.profesor p " +
                "LEFT JOIN FETCH p.tema " +
                "LEFT JOIN FETCH d.alumno a " +
                "WHERE d.profesor.id = :profesorId " +
                "AND (d.fechaEspecifica >= :fechaHoy OR d.fechaEspecifica IS NULL) " +
                "ORDER BY d.fechaEspecifica ASC, d.hora ASC";

        Query query = session.createQuery(hql);
        query.setParameter("profesorId", profesorId);
        query.setParameter("fechaHoy", hoy);

        return query.getResultList();
    }

    @Override
    public List<disponibilidadProfesor> obtenerClasesAlumno(Long alumnoId) {
        final Session session = sessionFactory.getCurrentSession();
        LocalDate hoy = LocalDate.now();

        String hql = "FROM disponibilidadProfesor d " +
                "LEFT JOIN FETCH d.profesor p " +
                "LEFT JOIN FETCH p.tema " +
                "LEFT JOIN FETCH d.alumno a " +
                "WHERE d.alumno.id = :alumnoId " +
                "AND (d.fechaEspecifica >= :fechaHoy OR d.fechaEspecifica IS NULL) " +
                "AND d.estado = :estado " +
                "ORDER BY d.fechaEspecifica ASC, d.hora ASC";

        Query query = session.createQuery(hql);
        query.setParameter("alumnoId", alumnoId);
        query.setParameter("fechaHoy", hoy);
        query.setParameter("estado", EstadoDisponibilidad.RESERVADO);
        return query.getResultList();
    }

}
