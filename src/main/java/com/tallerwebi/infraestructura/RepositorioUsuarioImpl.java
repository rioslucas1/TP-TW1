package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.*;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
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
        String hql = "FROM Usuario WHERE email = :email AND password = :password";
        Query query = session.createQuery(hql);
        query.setParameter("email", email);
        query.setParameter("password", password);
        List<Usuario> resultados = query.getResultList();
        return resultados.isEmpty() ? null : resultados.get(0);
    }

    @Override
    public void guardar(Usuario usuario) {
        sessionFactory.getCurrentSession().save(usuario);
    }

    @Override
    public Usuario buscar(String email) {
        final Session session = sessionFactory.getCurrentSession();
        String hql = "FROM Usuario WHERE email = :email";
        Query query = session.createQuery(hql);
        query.setParameter("email", email);
        List<Usuario> resultados = query.getResultList();
        return resultados.isEmpty() ? null : resultados.get(0);
    }

    @Override
    public void modificar(Usuario usuario) {
        sessionFactory.getCurrentSession().update(usuario);
    }



    public <T extends Usuario> List<Usuario> buscarPorTipo(Class<T> tipo) {
        final Session session = sessionFactory.getCurrentSession();
        String hql = "FROM " + tipo.getSimpleName();
        Query query = session.createQuery(hql);
        return query.getResultList();
    }

    @Override
    public List<Profesor> obtenerProfesoresDeAlumno(Long alumnoId) {
        final Session session = sessionFactory.getCurrentSession();

        String hql = "SELECT p FROM Profesor p " +
                "INNER JOIN p.alumnos a " +
                "WHERE a.id = :alumnoId";

        Query query = session.createQuery(hql);
        query.setParameter("alumnoId", alumnoId);

        return query.getResultList();
    }

    @Override
    public List<Clase> obtenerClasesProfesor(Long profesorId) {
        final Session session = sessionFactory.getCurrentSession();
        LocalDate hoy = LocalDate.now();

        String hql = "FROM Clase d " +
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
    public List<Clase> obtenerClasesAlumno(Long alumnoId) {
        final Session session = sessionFactory.getCurrentSession();
        LocalDate hoy = LocalDate.now();

        String hql = "FROM Clase d " +
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

    @Override
    public Usuario buscarPorId(Long id) {
        final Session session = sessionFactory.getCurrentSession();
        return session.get(Usuario.class, id);
    }

    public Profesor buscarProfesorConExperiencias(Long id) {
        return sessionFactory.getCurrentSession()
                .createQuery("SELECT p FROM Profesor p LEFT JOIN FETCH p.experiencias WHERE p.id = :id", Profesor.class)
                .setParameter("id", id)
                .uniqueResult();
    }

}
