package com.tallerwebi.dominio.servicios;
import com.tallerwebi.dominio.entidades.Archivo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ServicioArchivo {
    List<Archivo> obtenerArchivosPorAlumno(Long alumnoId);
    List<Archivo> obtenerArchivosPorProfesor(Long profesorId);
    Archivo buscarArchivoPorId(Long archivoId);
    void subirArchivo(MultipartFile file, Long profesorId, Long alumnoId) throws IOException;
    void eliminarArchivo(Long archivoId);
    List<Archivo> obtenerArchivosCompartidosConAlumnoPorSusProfesores(Long alumnoId);
    List<Archivo> obtenerArchivosCompartidosEntreProfesorYAlumno(Long profesorId, Long alumnoId);
    List<Archivo> buscarArchivosAlumno(Long alumnoId, String busqueda);
    List<Archivo> buscarArchivosProfesor(Long profesorId, String busqueda);
    List<Archivo> obtenerArchivosRecientes(Long usuarioId, String rol, int limite);
}