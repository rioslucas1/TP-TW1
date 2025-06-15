package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Usuario;

import java.util.List;

public interface RepositorioUsuario {


    Usuario buscarUsuario(String email, String password);
    void guardar(Usuario usuario);
    Usuario buscar(String email);
    void modificar(Usuario usuario);
    <T extends Usuario> List<Usuario> buscarPorTipo(Class<T> tipo);

}

