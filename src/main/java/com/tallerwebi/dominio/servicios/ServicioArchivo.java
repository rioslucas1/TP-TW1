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
}