package com.tallerwebi.infraestructura;
import com.tallerwebi.dominio.RepositorioArchivo;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Archivo;
import com.tallerwebi.dominio.entidades.Profesor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;

@Repository("repositorioArchivo")
@Transactional
public class RepositorioArchivoImpl implements RepositorioArchivo {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioArchivoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Archivo buscarArchivoPorId(Long id) {
        return sessionFactory.getCurrentSession().get(Archivo.class, id);
    }

    @Override
    public void guardarArchivo(Archivo archivo) {
        sessionFactory.getCurrentSession().saveOrUpdate(archivo);
    }

    @Override
    public void eliminarArchivo(Archivo archivo) {
        sessionFactory.getCurrentSession().delete(archivo);
    }

    @Override
    public List<Archivo> obtenerArchivosPorAlumno(Long alumnoId) {
        final Session session = sessionFactory.getCurrentSession();
        String hql = "FROM Archivo a WHERE a.alumno.id = :alumnoId ORDER BY a.fechaSubida DESC";
        Query query = session.createQuery(hql, Archivo.class);
        query.setParameter("alumnoId", alumnoId);
        return query.getResultList();
    }

    @Override
    public List<Archivo> obtenerArchivosPorProfesor(Long profesorId) {
        final Session session = sessionFactory.getCurrentSession();
        String hql = "FROM Archivo a WHERE a.profesor.id = :profesorId ORDER BY a.fechaSubida DESC";
        Query query = session.createQuery(hql, Archivo.class);
        query.setParameter("profesorId", profesorId);
        return query.getResultList();
    }

    @Override
    public List<Archivo> obtenerArchivosCompartidosEntreProfesorYAlumno(Long profesorId, Long alumnoId) {
        final Session session = sessionFactory.getCurrentSession();
        String hql = "FROM Archivo a WHERE a.profesor.id = :profesorId AND a.alumno.id = :alumnoId ORDER BY a.fechaSubida DESC";
        Query query = session.createQuery(hql, Archivo.class);
        query.setParameter("profesorId", profesorId);
        query.setParameter("alumnoId", alumnoId);
        return query.getResultList();
    }

    @Override
    public List<Archivo> obtenerArchivosCompartidosConAlumnoPorSusProfesores(Long alumnoId) {
        final Session session = sessionFactory.getCurrentSession();
        String hql = "SELECT a FROM Archivo a " +
                "JOIN a.profesor p " +
                "JOIN a.alumno al " +
                "JOIN al.profesores ap " +
                "WHERE al.id = :alumnoId AND ap.id = p.id " +
                "ORDER BY a.fechaSubida DESC";
        Query query = session.createQuery(hql, Archivo.class);
        query.setParameter("alumnoId", alumnoId);
        return query.getResultList();
    }
}

