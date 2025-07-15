package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.RepositorioArchivo;
import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Archivo;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Tema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServicioArchivoTest {

    private ServicioArchivo servicioArchivo;
    private RepositorioArchivo repositorioArchivoMock;
    private RepositorioUsuario repositorioUsuarioMock;
    private Alumno alumnoMock;
    private Profesor profesorMock;
    private Archivo archivoMock;
    private MultipartFile multipartFileMock;
    private Tema temaMock;

    private List<Path> tempFiles = new ArrayList<>();

    @BeforeEach
    public void init() throws IOException {
        repositorioArchivoMock = mock(RepositorioArchivo.class);
        repositorioUsuarioMock = mock(RepositorioUsuario.class);
        servicioArchivo = new ServicioArchivoImpl(repositorioArchivoMock, repositorioUsuarioMock);
        String projectRoot = System.getProperty("user.dir");
        String uploadsDir = new File(projectRoot).getParent() + File.separator + "uploads";
        Path uploadPath = Paths.get(uploadsDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path tempFile = uploadPath.resolve("test_archivo1.pdf");
        Files.createFile(tempFile);
        tempFiles.add(tempFile);

        temaMock = mock(Tema.class);
        when(temaMock.getNombre()).thenReturn("Programacion");

        alumnoMock = mock(Alumno.class);
        when(alumnoMock.getId()).thenReturn(1L);
        when(alumnoMock.getNombre()).thenReturn("NombreAlumno1");
        when(alumnoMock.getApellido()).thenReturn("Apellido1");
        when(alumnoMock.getEmail()).thenReturn("alumno1@unlam.com");

        profesorMock = mock(Profesor.class);
        when(profesorMock.getId()).thenReturn(1L);
        when(profesorMock.getNombre()).thenReturn("NombreProfesor1");
        when(profesorMock.getApellido()).thenReturn("Apellido1");
        when(profesorMock.getEmail()).thenReturn("profesor1@unlam.com");
        when(profesorMock.getTema()).thenReturn(temaMock);

        archivoMock = mock(Archivo.class);
        when(archivoMock.getId()).thenReturn(1L);
        when(archivoMock.getNombre()).thenReturn("archivo1.pdf");
        when(archivoMock.getTipoContenido()).thenReturn("application/pdf");
        when(archivoMock.getRutaAlmacenamiento()).thenReturn(tempFile.toString());
        when(archivoMock.getProfesor()).thenReturn(profesorMock);
        when(archivoMock.getAlumno()).thenReturn(alumnoMock);
        when(archivoMock.getFechaSubida()).thenReturn(LocalDateTime.now());

        multipartFileMock = mock(MultipartFile.class);
        when(multipartFileMock.getOriginalFilename()).thenReturn("archivo1.pdf");
        when(multipartFileMock.getContentType()).thenReturn("application/pdf");
        when(multipartFileMock.getSize()).thenReturn(1024L);
        when(multipartFileMock.isEmpty()).thenReturn(false);
    }

    @AfterEach
    public void cleanup() {
        for (Path tempFile : tempFiles) {
            try {
                if (Files.exists(tempFile)) {
                    Files.delete(tempFile);
                }
            } catch (IOException e) {
                System.err.println("Error al limpiar archivo temporal: " + e.getMessage());
            }
        }
        tempFiles.clear();
    }

    @Test
    public void obtenerArchivosPorAlumnoConAlumnoValidoDeberiaRetornarListaDeArchivos() {
        Long alumnoId = 1L;
        Archivo archivo1 = mock(Archivo.class);
        Archivo archivo2 = mock(Archivo.class);
        List<Archivo> archivosEsperados = Arrays.asList(archivo1, archivo2);

        when(repositorioArchivoMock.obtenerArchivosPorAlumno(alumnoId)).thenReturn(archivosEsperados);

        List<Archivo> archivosObtenidos = servicioArchivo.obtenerArchivosPorAlumno(alumnoId);

        assertNotNull(archivosObtenidos);
        assertEquals(2, archivosObtenidos.size());
        assertThat(archivosObtenidos, containsInAnyOrder(archivo1, archivo2));
        verify(repositorioArchivoMock, times(1)).obtenerArchivosPorAlumno(alumnoId);
    }

    @Test
    public void obtenerArchivosPorAlumnoConAlumnoSinArchivosDeberiaRetornarListaVacia() {
        Long alumnoId = 1L;
        List<Archivo> listaVacia = Arrays.asList();

        when(repositorioArchivoMock.obtenerArchivosPorAlumno(alumnoId)).thenReturn(listaVacia);

        List<Archivo> archivosObtenidos = servicioArchivo.obtenerArchivosPorAlumno(alumnoId);

        assertNotNull(archivosObtenidos);
        assertEquals(0, archivosObtenidos.size());
        verify(repositorioArchivoMock, times(1)).obtenerArchivosPorAlumno(alumnoId);
    }

    @Test
    public void obtenerArchivosPorAlumnoConIdNuloDeberiaLlamarRepositorio() {
        Long alumnoId = null;

        when(repositorioArchivoMock.obtenerArchivosPorAlumno(alumnoId)).thenReturn(Arrays.asList());

        List<Archivo> archivosObtenidos = servicioArchivo.obtenerArchivosPorAlumno(alumnoId);

        assertNotNull(archivosObtenidos);
        verify(repositorioArchivoMock, times(1)).obtenerArchivosPorAlumno(alumnoId);
    }

    @Test
    public void obtenerArchivosPorProfesorConProfesorValidoDeberiaRetornarListaDeArchivos() {
        Long profesorId = 1L;
        Archivo archivo1 = mock(Archivo.class);
        Archivo archivo2 = mock(Archivo.class);
        List<Archivo> archivosEsperados = Arrays.asList(archivo1, archivo2);

        when(repositorioArchivoMock.obtenerArchivosPorProfesor(profesorId)).thenReturn(archivosEsperados);

        List<Archivo> archivosObtenidos = servicioArchivo.obtenerArchivosPorProfesor(profesorId);

        assertNotNull(archivosObtenidos);
        assertEquals(2, archivosObtenidos.size());
        assertThat(archivosObtenidos, containsInAnyOrder(archivo1, archivo2));
        verify(repositorioArchivoMock, times(1)).obtenerArchivosPorProfesor(profesorId);
    }

    @Test
    public void obtenerArchivosPorProfesorConProfesorSinArchivosDeberiaRetornarListaVacia() {
        Long profesorId = 1L;
        List<Archivo> listaVacia = Arrays.asList();

        when(repositorioArchivoMock.obtenerArchivosPorProfesor(profesorId)).thenReturn(listaVacia);

        List<Archivo> archivosObtenidos = servicioArchivo.obtenerArchivosPorProfesor(profesorId);

        assertNotNull(archivosObtenidos);
        assertEquals(0, archivosObtenidos.size());
        verify(repositorioArchivoMock, times(1)).obtenerArchivosPorProfesor(profesorId);
    }

    @Test
    public void obtenerArchivosPorProfesorConIdNuloDeberiaLlamarRepositorio() {
        Long profesorId = null;

        when(repositorioArchivoMock.obtenerArchivosPorProfesor(profesorId)).thenReturn(Arrays.asList());

        List<Archivo> archivosObtenidos = servicioArchivo.obtenerArchivosPorProfesor(profesorId);

        assertNotNull(archivosObtenidos);
        verify(repositorioArchivoMock, times(1)).obtenerArchivosPorProfesor(profesorId);
    }

    @Test
    public void buscarArchivoPorIdConIdValidoDeberiaRetornarArchivo() {
        Long archivoId = 1L;
        when(repositorioArchivoMock.buscarArchivoPorId(archivoId)).thenReturn(archivoMock);

        Archivo archivoEncontrado = servicioArchivo.buscarArchivoPorId(archivoId);

        assertNotNull(archivoEncontrado);
        assertEquals(archivoMock, archivoEncontrado);
        verify(repositorioArchivoMock, times(1)).buscarArchivoPorId(archivoId);
    }

    @Test
    public void buscarArchivoPorIdConIdInexistenteDeberiaRetornarNull() {
        Long archivoId = 999L;
        when(repositorioArchivoMock.buscarArchivoPorId(archivoId)).thenReturn(null);

        Archivo archivoEncontrado = servicioArchivo.buscarArchivoPorId(archivoId);

        assertNull(archivoEncontrado);
        verify(repositorioArchivoMock, times(1)).buscarArchivoPorId(archivoId);
    }

    @Test
    public void buscarArchivoPorIdConIdNuloDeberiaLlamarRepositorio() {
        Long archivoId = null;
        when(repositorioArchivoMock.buscarArchivoPorId(archivoId)).thenReturn(null);

        Archivo archivoEncontrado = servicioArchivo.buscarArchivoPorId(archivoId);

        assertNull(archivoEncontrado);
        verify(repositorioArchivoMock, times(1)).buscarArchivoPorId(archivoId);
    }

    @Test
    public void subirArchivoConDatosValidosDeberiaGuardarArchivo() throws IOException {
        Long profesorId = 1L;
        Long alumnoId = 2L;
        InputStream inputStreamMock = mock(InputStream.class);

        when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(alumnoMock);
        when(multipartFileMock.getInputStream()).thenReturn(inputStreamMock);

        assertDoesNotThrow(() -> {
            servicioArchivo.subirArchivo(multipartFileMock, profesorId, alumnoId);
        });

        verify(repositorioUsuarioMock, times(1)).buscarPorId(profesorId);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
        verify(repositorioArchivoMock, times(1)).guardarArchivo(any(Archivo.class));
    }

    @Test
    public void subirArchivoConArchivoNuloDeberiaLanzarIOException() {
        Long profesorId = 1L;
        Long alumnoId = 1L;

        IOException exception = assertThrows(IOException.class, () -> {
            servicioArchivo.subirArchivo(null, profesorId, alumnoId);
        });

        assertTrue(exception.getMessage().contains("No se seleccionó ningún archivo"));
        verify(repositorioArchivoMock, never()).guardarArchivo(any(Archivo.class));
    }

    @Test
    public void subirArchivoConArchivoVacioDeberiaLanzarIOException() {
        Long profesorId = 1L;
        Long alumnoId = 1L;
        when(multipartFileMock.isEmpty()).thenReturn(true);

        IOException exception = assertThrows(IOException.class, () -> {
            servicioArchivo.subirArchivo(multipartFileMock, profesorId, alumnoId);
        });

        assertTrue(exception.getMessage().contains("No se seleccionó ningún archivo"));
        verify(repositorioArchivoMock, never()).guardarArchivo(any(Archivo.class));
    }

    @Test
    public void subirArchivoConArchivoMuyGrandeDeberiaLanzarIOException() {
        Long profesorId = 1L;
        Long alumnoId = 1L;
        when(multipartFileMock.getSize()).thenReturn(52428801L);

        IOException exception = assertThrows(IOException.class, () -> {
            servicioArchivo.subirArchivo(multipartFileMock, profesorId, alumnoId);
        });

        assertTrue(exception.getMessage().contains("demasiado grande"));
        verify(repositorioArchivoMock, never()).guardarArchivo(any(Archivo.class));
    }

    @Test
    public void subirArchivoConProfesorInexistenteDeberiaLanzarIllegalArgumentException() {
        Long profesorId = 999L;
        Long alumnoId = 1L;

        when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(null);
        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(alumnoMock);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            servicioArchivo.subirArchivo(multipartFileMock, profesorId, alumnoId);
        });

        assertTrue(exception.getMessage().contains("Profesor o Alumno no encontrados"));
        verify(repositorioArchivoMock, never()).guardarArchivo(any(Archivo.class));
    }

    @Test
    public void subirArchivoConAlumnoInexistenteDeberiaLanzarIllegalArgumentException() {
        Long profesorId = 1L;
        Long alumnoId = 999L;

        when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            servicioArchivo.subirArchivo(multipartFileMock, profesorId, alumnoId);
        });

        assertTrue(exception.getMessage().contains("Profesor o Alumno no encontrados"));
        verify(repositorioArchivoMock, never()).guardarArchivo(any(Archivo.class));
    }

    @Test
    public void subirArchivoConNombreInvalidoDeberiaLanzarIOException() {
        Long profesorId = 1L;
        Long alumnoId = 2L;

        when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(alumnoMock);
        when(multipartFileMock.getOriginalFilename()).thenReturn(null);

        IOException exception = assertThrows(IOException.class, () -> {
            servicioArchivo.subirArchivo(multipartFileMock, profesorId, alumnoId);
        });

        assertTrue(exception.getMessage().contains("Nombre de archivo inválido"));
        verify(repositorioArchivoMock, never()).guardarArchivo(any(Archivo.class));
    }

    @Test
    public void subirArchivoConNombreVacioDeberiaLanzarIOException() {
        Long profesorId = 1L;
        Long alumnoId = 2L;

        when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(alumnoMock);
        when(multipartFileMock.getOriginalFilename()).thenReturn("   ");

        IOException exception = assertThrows(IOException.class, () -> {
            servicioArchivo.subirArchivo(multipartFileMock, profesorId, alumnoId);
        });

        assertTrue(exception.getMessage().contains("Nombre de archivo inválido"));
        verify(repositorioArchivoMock, never()).guardarArchivo(any(Archivo.class));
    }

    @Test
    public void eliminarArchivoConIdValidoDeberiaEliminarArchivo() {
        Long archivoId = 1L;
        when(repositorioArchivoMock.buscarArchivoPorId(archivoId)).thenReturn(archivoMock);

        assertDoesNotThrow(() -> {
            servicioArchivo.eliminarArchivo(archivoId);
        });

        verify(repositorioArchivoMock, times(1)).buscarArchivoPorId(archivoId);
        verify(repositorioArchivoMock, times(1)).eliminarArchivo(archivoMock);
    }

    @Test
    public void eliminarArchivoConIdInexistenteNoDeberiaLanzarExcepcion() {
        Long archivoId = 999L;
        when(repositorioArchivoMock.buscarArchivoPorId(archivoId)).thenReturn(null);

        assertDoesNotThrow(() -> {
            servicioArchivo.eliminarArchivo(archivoId);
        });

        verify(repositorioArchivoMock, times(1)).buscarArchivoPorId(archivoId);
        verify(repositorioArchivoMock, never()).eliminarArchivo(any(Archivo.class));
    }

    @Test
    public void eliminarArchivoConIdNuloNoDeberiaLanzarExcepcion() {
        Long archivoId = null;
        when(repositorioArchivoMock.buscarArchivoPorId(archivoId)).thenReturn(null);

        assertDoesNotThrow(() -> {
            servicioArchivo.eliminarArchivo(archivoId);
        });

        verify(repositorioArchivoMock, times(1)).buscarArchivoPorId(archivoId);
        verify(repositorioArchivoMock, never()).eliminarArchivo(any(Archivo.class));
    }

    @Test
    public void obtenerArchivosCompartidosEntreProfesorYAlumnoConDatosValidosDeberiaRetornarListaDeArchivos() {
        Long profesorId = 1L;
        Long alumnoId = 1L;
        Archivo archivo1 = mock(Archivo.class);
        Archivo archivo2 = mock(Archivo.class);
        List<Archivo> archivosEsperados = Arrays.asList(archivo1, archivo2);

        when(repositorioArchivoMock.obtenerArchivosCompartidosEntreProfesorYAlumno(profesorId, alumnoId))
                .thenReturn(archivosEsperados);

        List<Archivo> archivosObtenidos = servicioArchivo.obtenerArchivosCompartidosEntreProfesorYAlumno(profesorId, alumnoId);

        assertNotNull(archivosObtenidos);
        assertEquals(2, archivosObtenidos.size());
        assertThat(archivosObtenidos, containsInAnyOrder(archivo1, archivo2));
        verify(repositorioArchivoMock, times(1)).obtenerArchivosCompartidosEntreProfesorYAlumno(profesorId, alumnoId);
    }

    @Test
    public void obtenerArchivosCompartidosEntreProfesorYAlumnoSinArchivosDeberiaRetornarListaVacia() {
        Long profesorId = 1L;
        Long alumnoId = 1L;
        List<Archivo> listaVacia = Arrays.asList();

        when(repositorioArchivoMock.obtenerArchivosCompartidosEntreProfesorYAlumno(profesorId, alumnoId))
                .thenReturn(listaVacia);

        List<Archivo> archivosObtenidos = servicioArchivo.obtenerArchivosCompartidosEntreProfesorYAlumno(profesorId, alumnoId);

        assertNotNull(archivosObtenidos);
        assertEquals(0, archivosObtenidos.size());
        verify(repositorioArchivoMock, times(1)).obtenerArchivosCompartidosEntreProfesorYAlumno(profesorId, alumnoId);
    }

    @Test
    public void obtenerArchivosCompartidosEntreProfesorYAlumnoConIdsNulosDeberiaLlamarRepositorio() {
        Long profesorId = null;
        Long alumnoId = null;

        when(repositorioArchivoMock.obtenerArchivosCompartidosEntreProfesorYAlumno(profesorId, alumnoId))
                .thenReturn(Arrays.asList());

        List<Archivo> archivosObtenidos = servicioArchivo.obtenerArchivosCompartidosEntreProfesorYAlumno(profesorId, alumnoId);

        assertNotNull(archivosObtenidos);
        verify(repositorioArchivoMock, times(1)).obtenerArchivosCompartidosEntreProfesorYAlumno(profesorId, alumnoId);
    }

    @Test
    public void obtenerArchivosCompartidosConAlumnoPorSusProfesoresConAlumnoValidoDeberiaRetornarListaDeArchivos() {
        Long alumnoId = 1L;
        Archivo archivo1 = mock(Archivo.class);
        Archivo archivo2 = mock(Archivo.class);
        List<Archivo> archivosEsperados = Arrays.asList(archivo1, archivo2);

        when(repositorioArchivoMock.obtenerArchivosCompartidosConAlumnoPorSusProfesores(alumnoId))
                .thenReturn(archivosEsperados);

        List<Archivo> archivosObtenidos = servicioArchivo.obtenerArchivosCompartidosConAlumnoPorSusProfesores(alumnoId);

        assertNotNull(archivosObtenidos);
        assertEquals(2, archivosObtenidos.size());
        assertThat(archivosObtenidos, containsInAnyOrder(archivo1, archivo2));
        verify(repositorioArchivoMock, times(1)).obtenerArchivosCompartidosConAlumnoPorSusProfesores(alumnoId);
    }

    @Test
    public void obtenerArchivosCompartidosConAlumnoPorSusProfesoresSinArchivosDeberiaRetornarListaVacia() {
        Long alumnoId = 1L;
        List<Archivo> listaVacia = Arrays.asList();

        when(repositorioArchivoMock.obtenerArchivosCompartidosConAlumnoPorSusProfesores(alumnoId))
                .thenReturn(listaVacia);

        List<Archivo> archivosObtenidos = servicioArchivo.obtenerArchivosCompartidosConAlumnoPorSusProfesores(alumnoId);

        assertNotNull(archivosObtenidos);
        assertEquals(0, archivosObtenidos.size());
        verify(repositorioArchivoMock, times(1)).obtenerArchivosCompartidosConAlumnoPorSusProfesores(alumnoId);
    }

    @Test
    public void obtenerArchivosCompartidosConAlumnoPorSusProfesoresConIdNuloDeberiaLlamarRepositorio() {
        Long alumnoId = null;

        when(repositorioArchivoMock.obtenerArchivosCompartidosConAlumnoPorSusProfesores(alumnoId))
                .thenReturn(Arrays.asList());

        List<Archivo> archivosObtenidos = servicioArchivo.obtenerArchivosCompartidosConAlumnoPorSusProfesores(alumnoId);

        assertNotNull(archivosObtenidos);
        verify(repositorioArchivoMock, times(1)).obtenerArchivosCompartidosConAlumnoPorSusProfesores(alumnoId);
    }

    @Test
    public void obtenerArchivosPorAlumnoConVariosArchivosDeberiaRetornarTodosLosArchivos() {
        Long alumnoId = 1L;
        Archivo archivo1 = mock(Archivo.class);
        Archivo archivo2 = mock(Archivo.class);
        Archivo archivo3 = mock(Archivo.class);
        List<Archivo> archivosEsperados = Arrays.asList(archivo1, archivo2, archivo3);

        when(repositorioArchivoMock.obtenerArchivosPorAlumno(alumnoId)).thenReturn(archivosEsperados);

        List<Archivo> archivosObtenidos = servicioArchivo.obtenerArchivosPorAlumno(alumnoId);

        assertNotNull(archivosObtenidos);
        assertEquals(3, archivosObtenidos.size());
        assertThat(archivosObtenidos, containsInAnyOrder(archivo1, archivo2, archivo3));
        verify(repositorioArchivoMock, times(1)).obtenerArchivosPorAlumno(alumnoId);
    }

    @Test
    public void obtenerArchivosPorProfesorConVariosArchivosDeberiaRetornarTodosLosArchivos() {
        Long profesorId = 1L;
        Archivo archivo1 = mock(Archivo.class);
        Archivo archivo2 = mock(Archivo.class);
        Archivo archivo3 = mock(Archivo.class);
        List<Archivo> archivosEsperados = Arrays.asList(archivo1, archivo2, archivo3);

        when(repositorioArchivoMock.obtenerArchivosPorProfesor(profesorId)).thenReturn(archivosEsperados);

        List<Archivo> archivosObtenidos = servicioArchivo.obtenerArchivosPorProfesor(profesorId);

        assertNotNull(archivosObtenidos);
        assertEquals(3, archivosObtenidos.size());
        assertThat(archivosObtenidos, containsInAnyOrder(archivo1, archivo2, archivo3));
        verify(repositorioArchivoMock, times(1)).obtenerArchivosPorProfesor(profesorId);
    }

    @Test
    public void subirArchivoConArchivoDeImagenDeberiaGuardarCorrectamente() throws IOException {
        Long profesorId = 1L;
        Long alumnoId = 2L;
        InputStream inputStreamMock = mock(InputStream.class);

        when(multipartFileMock.getOriginalFilename()).thenReturn("imagen1.jpg");
        when(multipartFileMock.getContentType()).thenReturn("image/jpeg");
        when(multipartFileMock.getSize()).thenReturn(2048L);
        when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(alumnoMock);
        when(multipartFileMock.getInputStream()).thenReturn(inputStreamMock);

        assertDoesNotThrow(() -> {
            servicioArchivo.subirArchivo(multipartFileMock, profesorId, alumnoId);
        });

        verify(repositorioUsuarioMock, times(1)).buscarPorId(profesorId);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
        verify(repositorioArchivoMock, times(1)).guardarArchivo(any(Archivo.class));
    }

    @Test
    public void subirArchivoConArchivoDeTextoDeberiaGuardarCorrectamente() throws IOException {
        Long profesorId = 1L;
        Long alumnoId = 2L;
        InputStream inputStreamMock = mock(InputStream.class);

        when(multipartFileMock.getOriginalFilename()).thenReturn("documento1.txt");
        when(multipartFileMock.getContentType()).thenReturn("text/plain");
        when(multipartFileMock.getSize()).thenReturn(512L);
        when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(alumnoMock);
        when(multipartFileMock.getInputStream()).thenReturn(inputStreamMock);

        assertDoesNotThrow(() -> {
            servicioArchivo.subirArchivo(multipartFileMock, profesorId, alumnoId);
        });

        verify(repositorioUsuarioMock, times(1)).buscarPorId(profesorId);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
        verify(repositorioArchivoMock, times(1)).guardarArchivo(any(Archivo.class));
    }



    @Test
    public void obtenerArchivosRecientesConDatosValidosDeberiaRetornarListaDeArchivos() {
        Long usuarioId = 1L;
        String rol = "ALUMNO";
        int limite = 5;


        when(alumnoMock.getRol()).thenReturn("ALUMNO");


        Archivo archivo1 = mock(Archivo.class);
        Archivo archivo2 = mock(Archivo.class);
        List<Archivo> archivosEsperados = Arrays.asList(archivo1, archivo2);

        when(archivo1.getRutaAlmacenamiento()).thenReturn(tempFiles.get(0).toString());
        when(archivo2.getRutaAlmacenamiento()).thenReturn(tempFiles.get(0).toString());

        when(repositorioUsuarioMock.buscarPorId(usuarioId)).thenReturn(alumnoMock);
        when(repositorioArchivoMock.obtenerArchivosRecientes(usuarioId, rol, limite)).thenReturn(archivosEsperados);

        List<Archivo> archivosObtenidos = servicioArchivo.obtenerArchivosRecientes(usuarioId, rol, limite);

        assertNotNull(archivosObtenidos);
        assertEquals(2, archivosObtenidos.size());
        verify(repositorioUsuarioMock, times(1)).buscarPorId(usuarioId);
        verify(repositorioArchivoMock, times(1)).obtenerArchivosRecientes(usuarioId, rol, limite);
    }

    @Test
    public void obtenerArchivosRecientesConProfesorValidoDeberiaRetornarListaDeArchivos() {
        Long usuarioId = 1L;
        String rol = "PROFESOR";
        int limite = 3;


        when(profesorMock.getRol()).thenReturn("PROFESOR");

        Archivo archivo1 = mock(Archivo.class);
        List<Archivo> archivosEsperados = Arrays.asList(archivo1);

        when(archivo1.getRutaAlmacenamiento()).thenReturn(tempFiles.get(0).toString());

        when(repositorioUsuarioMock.buscarPorId(usuarioId)).thenReturn(profesorMock);
        when(repositorioArchivoMock.obtenerArchivosRecientes(usuarioId, rol, limite)).thenReturn(archivosEsperados);

        List<Archivo> archivosObtenidos = servicioArchivo.obtenerArchivosRecientes(usuarioId, rol, limite);

        assertNotNull(archivosObtenidos);
        assertEquals(1, archivosObtenidos.size());
        verify(repositorioUsuarioMock, times(1)).buscarPorId(usuarioId);
        verify(repositorioArchivoMock, times(1)).obtenerArchivosRecientes(usuarioId, rol, limite);
    }

    @Test
    public void obtenerArchivosRecientesConUsuarioIdNuloDeberiaLanzarIllegalArgumentException() {
        Long usuarioId = null;
        String rol = "ALUMNO";
        int limite = 5;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            servicioArchivo.obtenerArchivosRecientes(usuarioId, rol, limite);
        });

        assertTrue(exception.getMessage().contains("ID de usuario inválido"));
        verify(repositorioUsuarioMock, never()).buscarPorId(any());
        verify(repositorioArchivoMock, never()).obtenerArchivosRecientes(any(), any(), anyInt());
    }

    @Test
    public void obtenerArchivosRecientesConUsuarioIdCeroDeberiaLanzarIllegalArgumentException() {
        Long usuarioId = 0L;
        String rol = "ALUMNO";
        int limite = 5;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            servicioArchivo.obtenerArchivosRecientes(usuarioId, rol, limite);
        });

        assertTrue(exception.getMessage().contains("ID de usuario inválido"));
        verify(repositorioUsuarioMock, never()).buscarPorId(any());
        verify(repositorioArchivoMock, never()).obtenerArchivosRecientes(any(), any(), anyInt());
    }

    @Test
    public void obtenerArchivosRecientesConUsuarioIdNegativoDeberiaLanzarIllegalArgumentException() {
        Long usuarioId = -1L;
        String rol = "ALUMNO";
        int limite = 5;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            servicioArchivo.obtenerArchivosRecientes(usuarioId, rol, limite);
        });

        assertTrue(exception.getMessage().contains("ID de usuario inválido"));
        verify(repositorioUsuarioMock, never()).buscarPorId(any());
        verify(repositorioArchivoMock, never()).obtenerArchivosRecientes(any(), any(), anyInt());
    }

    @Test
    public void obtenerArchivosRecientesConRolNuloDeberiaLanzarIllegalArgumentException() {
        Long usuarioId = 1L;
        String rol = null;
        int limite = 5;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            servicioArchivo.obtenerArchivosRecientes(usuarioId, rol, limite);
        });

        assertTrue(exception.getMessage().contains("Tipo de usuario no puede estar vacío"));
        verify(repositorioUsuarioMock, never()).buscarPorId(any());
        verify(repositorioArchivoMock, never()).obtenerArchivosRecientes(any(), any(), anyInt());
    }

    @Test
    public void obtenerArchivosRecientesConRolVacioDeberiaLanzarIllegalArgumentException() {
        Long usuarioId = 1L;
        String rol = "   ";
        int limite = 5;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            servicioArchivo.obtenerArchivosRecientes(usuarioId, rol, limite);
        });

        assertTrue(exception.getMessage().contains("Tipo de usuario no puede estar vacío"));
        verify(repositorioUsuarioMock, never()).buscarPorId(any());
        verify(repositorioArchivoMock, never()).obtenerArchivosRecientes(any(), any(), anyInt());
    }

    @Test
    public void obtenerArchivosRecientesConLimiteCeroDeberiaUsarLimitePorDefecto() {
        Long usuarioId = 1L;
        String rol = "ALUMNO";
        int limite = 0;

        when(alumnoMock.getRol()).thenReturn("ALUMNO");
        when(repositorioUsuarioMock.buscarPorId(usuarioId)).thenReturn(alumnoMock);
        when(repositorioArchivoMock.obtenerArchivosRecientes(usuarioId, rol, 10)).thenReturn(Arrays.asList());

        List<Archivo> archivosObtenidos = servicioArchivo.obtenerArchivosRecientes(usuarioId, rol, limite);

        assertNotNull(archivosObtenidos);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(usuarioId);
        verify(repositorioArchivoMock, times(1)).obtenerArchivosRecientes(usuarioId, rol, 10);
    }

    @Test
    public void obtenerArchivosRecientesConLimiteNegativoDeberiaUsarLimitePorDefecto() {
        Long usuarioId = 1L;
        String rol = "ALUMNO";
        int limite = -5;

        when(alumnoMock.getRol()).thenReturn("ALUMNO");
        when(repositorioUsuarioMock.buscarPorId(usuarioId)).thenReturn(alumnoMock);
        when(repositorioArchivoMock.obtenerArchivosRecientes(usuarioId, rol, 10)).thenReturn(Arrays.asList());

        List<Archivo> archivosObtenidos = servicioArchivo.obtenerArchivosRecientes(usuarioId, rol, limite);

        assertNotNull(archivosObtenidos);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(usuarioId);
        verify(repositorioArchivoMock, times(1)).obtenerArchivosRecientes(usuarioId, rol, 10);
    }

    @Test
    public void obtenerArchivosRecientesConUsuarioInexistenteDeberiaLanzarIllegalArgumentException() {
        Long usuarioId = 999L;
        String rol = "ALUMNO";
        int limite = 5;

        when(repositorioUsuarioMock.buscarPorId(usuarioId)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            servicioArchivo.obtenerArchivosRecientes(usuarioId, rol, limite);
        });

        assertTrue(exception.getMessage().contains("Usuario no encontrado con ID: " + usuarioId));
        verify(repositorioUsuarioMock, times(1)).buscarPorId(usuarioId);
        verify(repositorioArchivoMock, never()).obtenerArchivosRecientes(any(), any(), anyInt());
    }

    @Test
    public void obtenerArchivosRecientesConRolNoCoincidenteDeberiaLanzarIllegalArgumentException() {
        Long usuarioId = 1L;
        String rol = "PROFESOR";
        int limite = 5;

        when(alumnoMock.getRol()).thenReturn("ALUMNO");
        when(repositorioUsuarioMock.buscarPorId(usuarioId)).thenReturn(alumnoMock);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            servicioArchivo.obtenerArchivosRecientes(usuarioId, rol, limite);
        });

        assertTrue(exception.getMessage().contains("Tipo de usuario no coincide con el usuario real"));
        verify(repositorioUsuarioMock, times(1)).buscarPorId(usuarioId);
        verify(repositorioArchivoMock, never()).obtenerArchivosRecientes(any(), any(), anyInt());
    }

    @Test
    public void obtenerArchivosRecientesConRolEnMayusculasDeberiaFuncionar() {
        Long usuarioId = 1L;
        String rol = "ALUMNO";
        int limite = 5;

        when(alumnoMock.getRol()).thenReturn("alumno");
        when(repositorioUsuarioMock.buscarPorId(usuarioId)).thenReturn(alumnoMock);
        when(repositorioArchivoMock.obtenerArchivosRecientes(usuarioId, rol, limite)).thenReturn(Arrays.asList());

        List<Archivo> archivosObtenidos = servicioArchivo.obtenerArchivosRecientes(usuarioId, rol, limite);

        assertNotNull(archivosObtenidos);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(usuarioId);
        verify(repositorioArchivoMock, times(1)).obtenerArchivosRecientes(usuarioId, rol, limite);
    }

    @Test
    public void obtenerArchivosRecientesSinArchivosDeberiaRetornarListaVacia() {
        Long usuarioId = 1L;
        String rol = "ALUMNO";
        int limite = 5;

        when(alumnoMock.getRol()).thenReturn("ALUMNO");
        when(repositorioUsuarioMock.buscarPorId(usuarioId)).thenReturn(alumnoMock);
        when(repositorioArchivoMock.obtenerArchivosRecientes(usuarioId, rol, limite)).thenReturn(Arrays.asList());

        List<Archivo> archivosObtenidos = servicioArchivo.obtenerArchivosRecientes(usuarioId, rol, limite);

        assertNotNull(archivosObtenidos);
        assertEquals(0, archivosObtenidos.size());
        verify(repositorioUsuarioMock, times(1)).buscarPorId(usuarioId);
        verify(repositorioArchivoMock, times(1)).obtenerArchivosRecientes(usuarioId, rol, limite);
    }




}