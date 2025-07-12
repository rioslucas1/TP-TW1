package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.dominio.servicios.ServicioEnvioDeCorreos;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("repositorioUsuario")
@Transactional
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
                     ServicioEnvioDeCorreos servicio = new ServicioEnvioDeCorreos();
                 Map<String, String> datos = new HashMap<>();

        datos.put("nombre", usuario.getNombre());

         servicio.enviarCorreo(
            usuario.getEmail(),                            
            "Bienvenido a Clases Ya!",                             
            ServicioEnvioDeCorreos.TipoCorreo.BIENVENIDA,
            datos                                                 
        );
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
    public List<Alumno> obtenerAlumnosDeProfesor(Long profesorId) {
        final Session session = sessionFactory.getCurrentSession();

        String hql = "SELECT a FROM Alumno a " +
                "INNER JOIN a.profesores p " +
                "WHERE p.id = :profesorId";

        Query query = session.createQuery(hql);
        query.setParameter("profesorId", profesorId);

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

    @Override
    public Usuario buscarPorNombre(String nombre) {
        final Session session = sessionFactory.getCurrentSession();
        String hql = "FROM Usuario WHERE nombre = :nombre";
        return session.createQuery(hql, Usuario.class)
                .setParameter("nombre", nombre)
                .uniqueResult();
    }

    @Override
    public Alumno buscarAlumnoPorNombre(String nombre) {
        String hql = "FROM Alumno a WHERE a.nombre = :nombre";
        Query query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("nombre", nombre);
        List<Alumno> resultados = query.getResultList();
        return resultados.isEmpty() ? null : resultados.get(0);
    }

    @Override
    public Profesor buscarProfesorPorNombre(String nombre) {
        String hql = "FROM Profesor p WHERE p.nombre = :nombre";
        Query query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("nombre", nombre);
        List<Profesor> resultados = query.getResultList();
        return resultados.isEmpty() ? null : resultados.get(0);
    }
    @Override
        public List<Usuario> buscarConNotificacionesPendientes() {
        final Session session = sessionFactory.getCurrentSession();
        String hql = "FROM usuarios WHERE ultima_conexion < NOW() - INTERVAL 7 DAY;";
        Query query = session.createQuery(hql, Usuario.class);
        return query.getResultList();
    }
    @Override
    public void guardarUltimaConexion(Usuario usuario) {
        final Session session = sessionFactory.getCurrentSession();
        if (usuario != null) {
            usuario.setUltimaConexion(ZonedDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires")).toLocalDateTime());
            session.update(usuario);
        }
    }


    @Override
    public Profesor buscarProfesorConAlumnos(Long id) {
        final Session session = sessionFactory.getCurrentSession();
        String hql = "SELECT p FROM Profesor p LEFT JOIN FETCH p.alumnos WHERE p.id = :profesorId";
        Query query = session.createQuery(hql, Profesor.class);
        query.setParameter("profesorId", id);
        return (Profesor) query.getSingleResult();
    }

    @Override
    public boolean alumnoPertenece(Long alumnoId, Long profesorId) {
        final Session session = sessionFactory.getCurrentSession();

        String hql = "SELECT COUNT(ap) FROM Alumno a " +
                "JOIN a.profesores ap " +
                "WHERE a.id = :alumnoId AND ap.id = :profesorId";

        Query query = session.createQuery(hql);
        query.setParameter("alumnoId", alumnoId);
        query.setParameter("profesorId", profesorId);

        Long count = (Long) query.getSingleResult();
        return count > 0;
    }


}
