package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service("servicioTema")
@Transactional
public class ServicioTemaImpl implements ServicioTema {

    private RepositorioTema repositorioTema;

    @Autowired
    public ServicioTemaImpl(RepositorioTema repositorioTema) {
        this.repositorioTema = repositorioTema;
    }

    @Override
    public List<Tema> obtenerTodos() {
        return repositorioTema.obtenerTodos();
    }

    @Override
    public Tema obtenerPorId(Long id) {
        return repositorioTema.buscarPorId(id);
    }

}

