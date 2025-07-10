package com.tallerwebi.dominio.servicios;
import com.tallerwebi.dominio.RepositorioArchivo;
import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Archivo;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service("servicioArchivo")
@Transactional
public class ServicioArchivoImpl implements ServicioArchivo {

    private RepositorioArchivo repositorioArchivo;
    private RepositorioUsuario repositorioUsuario;

    private final String DIRECTORIO_ARCHIVOS_UPLOAD;

    @Autowired
    public ServicioArchivoImpl(RepositorioArchivo repositorioArchivo, RepositorioUsuario repositorioUsuario) {
        this.repositorioArchivo = repositorioArchivo;
        this.repositorioUsuario = repositorioUsuario;
        this.DIRECTORIO_ARCHIVOS_UPLOAD = configurarDirectorioArchivos();
        inicializarDirectorioArchivos();
    }

    private String configurarDirectorioArchivos() {
        String dockerEnv = System.getenv("DOCKER_ENV");
        boolean isDockerEnvironment = dockerEnv != null && dockerEnv.equals("true");

        if (isDockerEnvironment) {
            String dockerUploadPath = "/app/uploads";
            return dockerUploadPath;
        } else {
            String currentDir = System.getProperty("user.dir");
            File projectRoot = new File(currentDir);
            if (currentDir.endsWith("src")) {
                projectRoot = new File(currentDir).getParentFile().getParentFile();
            } else {
                projectRoot = new File(currentDir).getParentFile();
            }
            String localUploadPath = new File(projectRoot, "uploads").getAbsolutePath();
            return localUploadPath;
        }
    }

    private void inicializarDirectorioArchivos() {
        try {
            Path uploadPath = Paths.get(DIRECTORIO_ARCHIVOS_UPLOAD);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            System.err.println("Error al crear el directorio de uploads: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean verificarArchivoExiste(String rutaArchivo) {
        try {
            Path filePath = Paths.get(rutaArchivo);
            boolean exists = Files.exists(filePath) && Files.isReadable(filePath);
            return exists;
        } catch (Exception e) {
            System.err.println("Error al verificar archivo: " + e.getMessage());
            return false;
        }
    }

    private Archivo recuperarArchivo(Archivo archivo) {
        if (archivo == null) return null;
        if (verificarArchivoExiste(archivo.getRutaAlmacenamiento())) {
            return archivo;
        }

        String nombreOriginal = archivo.getNombre();
        Path archivoAlternativo = Paths.get(DIRECTORIO_ARCHIVOS_UPLOAD, nombreOriginal);

        if (Files.exists(archivoAlternativo)) {
            archivo.setRutaAlmacenamiento(archivoAlternativo.toString());
            repositorioArchivo.guardarArchivo(archivo);
            return archivo;
        }
        String rutaAlmacenamiento = archivo.getRutaAlmacenamiento();
        if (rutaAlmacenamiento != null) {
            String nombreArchivo = Paths.get(rutaAlmacenamiento).getFileName().toString();
            Path archivoEnDirectorioConfigurado = Paths.get(DIRECTORIO_ARCHIVOS_UPLOAD, nombreArchivo);

            if (Files.exists(archivoEnDirectorioConfigurado)) {
                archivo.setRutaAlmacenamiento(archivoEnDirectorioConfigurado.toString());
                repositorioArchivo.guardarArchivo(archivo);
                return archivo;
            }
        }

        return null;
    }

    @Override
    public List<Archivo> obtenerArchivosPorAlumno(Long alumnoId) {
        return repositorioArchivo.obtenerArchivosPorAlumno(alumnoId);
    }

    @Override
    public List<Archivo> obtenerArchivosPorProfesor(Long profesorId) {
        return repositorioArchivo.obtenerArchivosPorProfesor(profesorId);
    }

    @Override
    public Archivo buscarArchivoPorId(Long archivoId) {
        Archivo archivo = repositorioArchivo.buscarArchivoPorId(archivoId);
        return recuperarArchivo(archivo);
    }

    @Override
    public void subirArchivo(MultipartFile file, Long profesorId, Long alumnoId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("No se seleccionó ningún archivo para subir.");
        }
        if (file.getSize() > 52428800) {
            throw new IOException("El archivo es demasiado grande. Tamaño máximo: 50MB");
        }

        Profesor profesor = (Profesor) repositorioUsuario.buscarPorId(profesorId);
        Alumno alumno = (Alumno) repositorioUsuario.buscarPorId(alumnoId);

        if (profesor == null || alumno == null) {
            throw new IllegalArgumentException("Profesor o Alumno no encontrados.");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IOException("Nombre de archivo inválido.");
        }
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < originalFilename.length() - 1) {
            extension = originalFilename.substring(lastDotIndex);
        }
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFilename;
        Path filePath = Paths.get(DIRECTORIO_ARCHIVOS_UPLOAD, uniqueFileName);
        try {
            inicializarDirectorioArchivos();
            Path parentDir = filePath.getParent();
            if (!Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            if (!verificarArchivoExiste(filePath.toString())) {
                throw new IOException("El archivo no se pudo guardar en: " + filePath.toString());
            }
            long savedFileSize = Files.size(filePath);
            if (savedFileSize != file.getSize()) {
                System.err.println("El tamaño del archivo guardado no coincide con el original");
            }
            Archivo archivo = new Archivo();
            archivo.setNombre(originalFilename);
            archivo.setTipoContenido(file.getContentType());
            archivo.setRutaAlmacenamiento(filePath.toString());
            archivo.setProfesor(profesor);
            archivo.setAlumno(alumno);
            archivo.setFechaSubida(LocalDateTime.now());
            repositorioArchivo.guardarArchivo(archivo);

        } catch (IOException e) {
            System.err.println("Error al guardar archivo: " + e.getMessage());
            e.printStackTrace();
            try {
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            } catch (IOException cleanupEx) {
                System.err.println("Error al limpiar archivo fallido: " + cleanupEx.getMessage());
            }

            throw new IOException("Error al guardar el archivo: " + e.getMessage());
        }
    }

    @Override
    public void eliminarArchivo(Long archivoId) {
        Archivo archivo = repositorioArchivo.buscarArchivoPorId(archivoId);
        if (archivo != null) {
            try {
                Path filePath = Paths.get(archivo.getRutaAlmacenamiento());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
                repositorioArchivo.eliminarArchivo(archivo);

            } catch (IOException e) {
                System.err.println("Error al eliminar archivo del sistema de archivos: " + e.getMessage());
                try {
                    repositorioArchivo.eliminarArchivo(archivo);
                } catch (Exception dbEx) {
                    System.err.println("Error al eliminar de BD: " + dbEx.getMessage());
                }
                throw new RuntimeException("Error al eliminar el archivo del sistema de archivos", e);
            }
        }
    }

    @Override
    public List<Archivo> obtenerArchivosCompartidosEntreProfesorYAlumno(Long profesorId, Long alumnoId) {
        return repositorioArchivo.obtenerArchivosCompartidosEntreProfesorYAlumno(profesorId, alumnoId);
    }

    @Override
    public List<Archivo> obtenerArchivosCompartidosConAlumnoPorSusProfesores(Long alumnoId) {
        return repositorioArchivo.obtenerArchivosCompartidosConAlumnoPorSusProfesores(alumnoId);
    }

    @Override
    public List<Archivo> buscarArchivosAlumno(Long alumnoId, String busqueda) {
        return repositorioArchivo.buscarArchivosAlumno(alumnoId, busqueda);
    }

    @Override
    public List<Archivo> buscarArchivosProfesor(Long profesorId, String busqueda) {
        return repositorioArchivo.buscarArchivosProfesor(profesorId, busqueda);
    }

}