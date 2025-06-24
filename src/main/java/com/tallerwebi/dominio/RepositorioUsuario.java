package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.dominio.entidades.disponibilidadProfesor;

import java.util.List;

public interface RepositorioUsuario {


    Usuario buscarUsuario(String email, String password);
    void guardar(Usuario usuario);
    Usuario buscar(String email);
    void modificar(Usuario usuario);
    <T extends Usuario> List<Usuario> buscarPorTipo(Class<T> tipo);
    List<disponibilidadProfesor> obtenerClasesProfesor(Long profesorid);
    List<disponibilidadProfesor> obtenerClasesAlumno(Long alumnoid);


}

