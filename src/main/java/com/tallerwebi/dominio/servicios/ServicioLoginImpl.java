package com.tallerwebi.dominio.servicios;
import java.time.LocalDateTime;
import java.util.List;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.dominio.entidades.Clase;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;

import org.hibernate.Session;
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
    public List<Profesor> obtenerProfesoresDeAlumno(Long alumnoId) {
        return repositorioUsuario.obtenerProfesoresDeAlumno(alumnoId);
    }


    @Override
    public List<Clase> obtenerClasesProfesor(Long profesorId) {
        return repositorioUsuario.obtenerClasesProfesor(profesorId);
    }
    @Override
    public List<Clase> obtenerClasesAlumno(Long alumnoId) {
        return repositorioUsuario.obtenerClasesAlumno(alumnoId);
    }

    @Override
    public void guardarUltimaConexion(Usuario usuario) {
      repositorioUsuario.guardarUltimaConexion(usuario);
    }
}

