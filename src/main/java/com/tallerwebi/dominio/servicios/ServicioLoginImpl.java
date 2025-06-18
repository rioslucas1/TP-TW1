package com.tallerwebi.dominio.servicios;
import java.util.List;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.dominio.entidades.disponibilidadProfesor;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service("servicioLogin")
@Transactional
public class ServicioLoginImpl implements ServicioLogin {

    private RepositorioUsuario repositorioUsuario;

    @Autowired
    public ServicioLoginImpl(RepositorioUsuario repositorioUsuario){
        this.repositorioUsuario = repositorioUsuario;
    }

    @Override
    public Usuario consultarUsuario (String email, String password) {
        return repositorioUsuario.buscarUsuario(email, password);
    }

    @Override
    public void registrar(Usuario usuario) throws UsuarioExistente {
        Usuario usuarioEncontrado = repositorioUsuario.buscar(usuario.getEmail());
        if(usuarioEncontrado != null){
            throw new UsuarioExistente();
        }
        repositorioUsuario.guardar(usuario);
    }

    @Override
    public List<Usuario> obtenerProfesores() {
        return repositorioUsuario.buscarPorTipo(Profesor.class);
    }


    @Override
    public List<disponibilidadProfesor> obtenerClasesProfesor(Long profesorId) {
        return repositorioUsuario.obtenerClasesProfesor(profesorId);
    }
    @Override
    public List<disponibilidadProfesor> obtenerClasesAlumno(Long alumnoId) {
        return repositorioUsuario.obtenerClasesAlumno(alumnoId);
    }


}

