package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.dominio.entidades.Clase;

import java.util.List;

import org.springframework.stereotype.Repository;
@Repository
public interface RepositorioUsuario {


    Usuario buscarUsuario(String email, String password);
    void guardar(Usuario usuario);
    Usuario buscar(String email);
    void modificar(Usuario usuario);
    <T extends Usuario> List<Usuario> buscarPorTipo(Class<T> tipo);
    List<Clase> obtenerClasesProfesor(Long profesorid);
    List<Clase> obtenerClasesAlumno(Long alumnoid);
    Usuario buscarPorId(Long id);
    Profesor buscarProfesorConExperiencias(Long id);
    List<Profesor> obtenerProfesoresDeAlumno(Long alumnoId);
    Usuario buscarPorNombre(String nombre);
    Alumno buscarAlumnoPorNombre(String emisorNombre);
    Profesor buscarProfesorPorNombre(String emisorNombre);
        List<Usuario> buscarConNotificacionesPendientes();
}

