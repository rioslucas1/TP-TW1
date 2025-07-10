package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Archivo;
import com.tallerwebi.dominio.entidades.Profesor;

import java.util.List;
public interface RepositorioArchivo {
    Archivo buscarArchivoPorId(Long id);
    void guardarArchivo(Archivo archivo);
    void eliminarArchivo(Archivo archivo);
    List<Archivo> obtenerArchivosPorAlumno(Long alumnoId);
    List<Archivo> obtenerArchivosPorProfesor(Long profesorId);
    List<Archivo> obtenerArchivosCompartidosEntreProfesorYAlumno(Long profesorId, Long alumnoId);
    List<Archivo> obtenerArchivosCompartidosConAlumnoPorSusProfesores(Long alumnoId);
    List<Archivo> buscarArchivosAlumno(Long alumnoId, String busqueda);
    List<Archivo> buscarArchivosProfesor(Long profesorId, String busqueda);

}