package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.Usuario;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("repositorioUsuario")
public class RepositorioUsuarioImpl implements RepositorioUsuario {

    private final SessionFactory sessionFactory;

    @Autowired
    public RepositorioUsuarioImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Usuario buscarUsuario(String email, String password) {
        Session session = sessionFactory.getCurrentSession();
        Query<Usuario> query = session.createQuery(
                "FROM Usuario WHERE email = :email AND contrasenia = :contrasenia", Usuario.class);
        query.setParameter("email", email);
        query.setParameter("contrasenia", password);
        return query.uniqueResult();
    }

    @Override
    public void guardar(Usuario usuario) {
        sessionFactory.getCurrentSession().save(usuario);
    }

    @Override
    public Usuario buscar(String email) {
        Session session = sessionFactory.getCurrentSession();
        Query<Usuario> query = session.createQuery(
                "FROM Usuario WHERE email = :email", Usuario.class);
        query.setParameter("email", email);
        return query.uniqueResult();
    }

    @Override
    public void modificar(Usuario usuario) {
        sessionFactory.getCurrentSession().update(usuario);
    }

    @Override
    public List<Usuario> buscarPorRol(String rol) {
        Session session = sessionFactory.getCurrentSession();
        Query<Usuario> query = session.createQuery(
                "FROM Usuario WHERE rol = :rol", Usuario.class);
        query.setParameter("rol", rol);
        return query.getResultList();
    }

    @Override
    public Usuario buscarPorId(Long id) {
        return sessionFactory.getCurrentSession().get(Usuario.class, id);
    }

}
