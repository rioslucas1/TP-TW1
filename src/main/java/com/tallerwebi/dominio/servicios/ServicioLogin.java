package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;

import java.util.List;

public interface ServicioLogin {

    Usuario consultarUsuario(String email, String password);
    void registrar(Usuario usuario) throws UsuarioExistente;
    List<Usuario> obtenerProfesores();

}
