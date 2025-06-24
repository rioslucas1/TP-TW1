package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioSuscripcionImpl implements ServicioSuscripcion {

    private final RepositorioUsuario repositorioUsuario;

    @Autowired
    public ServicioSuscripcionImpl(RepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
    }

    @Override
    public boolean suscribirAlumnoAProfesor(Long alumnoId, Long profesorId) {
        try {
            Usuario usuarioAlumno = repositorioUsuario.buscarPorId(alumnoId);
            Usuario usuarioProfesor = repositorioUsuario.buscarPorId(profesorId);

            if (usuarioAlumno instanceof Alumno && usuarioProfesor instanceof Profesor) {
                Alumno alumno = (Alumno) usuarioAlumno;
                Profesor profesor = (Profesor) usuarioProfesor;

                if (!alumno.estaSuscritoAProfesor(profesor)) {
                    alumno.agregarProfesor(profesor);
                    repositorioUsuario.modificar(alumno);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean desuscribirAlumnoDeProfesor(Long alumnoId, Long profesorId) {
        try {
            Usuario usuarioAlumno = repositorioUsuario.buscarPorId(alumnoId);
            Usuario usuarioProfesor = repositorioUsuario.buscarPorId(profesorId);

            if (usuarioAlumno instanceof Alumno && usuarioProfesor instanceof Profesor) {
                Alumno alumno = (Alumno) usuarioAlumno;
                Profesor profesor = (Profesor) usuarioProfesor;

                if (alumno.estaSuscritoAProfesor(profesor)) {
                    alumno.eliminarProfesor(profesor);
                    repositorioUsuario.modificar(alumno);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean estaAlumnoSuscritoAProfesor(Long alumnoId, Long profesorId) {
        try {
            Usuario usuarioAlumno = repositorioUsuario.buscarPorId(alumnoId);
            Usuario usuarioProfesor = repositorioUsuario.buscarPorId(profesorId);

            if (usuarioAlumno instanceof Alumno && usuarioProfesor instanceof Profesor) {
                Alumno alumno = (Alumno) usuarioAlumno;
                Profesor profesor = (Profesor) usuarioProfesor;
                return alumno.estaSuscritoAProfesor(profesor);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}