package com.tallerwebi.infraestructura;
import java.util.List;

import com.tallerwebi.dominio.RepositorioTema;
import com.tallerwebi.dominio.entidades.Tema;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioTema")
public class RepositorioTemaImpl implements RepositorioTema {

    private SessionFactory sessionFactory;

    @Autowired
    public RepositorioTemaImpl(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Tema> obtenerTodos() {
        return sessionFactory.getCurrentSession().createCriteria(Tema.class).list();
    }

    @Override
    public Tema buscarPorId(Long id) {
        return (Tema) sessionFactory.getCurrentSession().get(Tema.class, id);
    }

}
