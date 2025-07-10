package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.dominio.entidades.Clase;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;

import java.util.List;

public interface ServicioLogin {

    Usuario consultarUsuario(String email, String password);
    void registrar(Usuario usuario) throws UsuarioExistente;
    List<Usuario> obtenerProfesores();
    List<Profesor> obtenerProfesoresDeAlumno(Long idAlumno);
    List<Clase> obtenerClasesProfesor(Long id);
    List<Clase> obtenerClasesAlumno(Long id);
     void guardarUltimaConexion( Usuario usuario);
}
