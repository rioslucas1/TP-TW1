package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Profesor;

public interface ServicioSuscripcion {


    boolean suscribirAlumnoAProfesor(Long alumnoId, Long profesorId);
    boolean desuscribirAlumnoDeProfesor(Long alumnoId, Long profesorId);
    boolean estaAlumnoSuscritoAProfesor(Long alumnoId, Long profesorId);
}