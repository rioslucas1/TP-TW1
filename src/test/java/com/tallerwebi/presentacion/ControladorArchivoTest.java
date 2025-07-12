package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.dominio.servicios.ServicioArchivo;
import com.tallerwebi.dominio.servicios.ServicioLogin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ControladorArchivoTest {

    private ControladorArchivo controladorArchivo;
    private ServicioArchivo servicioArchivoMock;
    private ServicioLogin servicioLoginMock;
    private RepositorioUsuario repositorioUsuarioMock;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;
    private RedirectAttributes redirectAttributesMock;
    private MultipartFile fileMock;

    private Alumno alumno1;
    private Alumno alumno2;
    private Profesor profesor1;
    private Profesor profesor2;
    private Tema programacion;
    private Tema diseño;
    private Archivo archivo1;
    private Archivo archivo2;

    @BeforeEach
    public void init() {
        servicioArchivoMock = mock(ServicioArchivo.class);
        servicioLoginMock = mock(ServicioLogin.class);
        repositorioUsuarioMock = mock(RepositorioUsuario.class);
        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);
        redirectAttributesMock = mock(RedirectAttributes.class);
        fileMock = mock(MultipartFile.class);

        controladorArchivo = new ControladorArchivo(servicioArchivoMock, servicioLoginMock, repositorioUsuarioMock);

        programacion = new Tema();
        programacion.setId(1L);
        programacion.setNombre("Programación");

        diseño = new Tema();
        diseño.setId(2L);
        diseño.setNombre("Diseño");

        alumno1 = new Alumno();
        alumno1.setId(1L);
        alumno1.setNombre("nombrealumno1");
        alumno1.setApellido("Apellido");
        alumno1.setEmail("alumno1@unlam.com");
        alumno1.setPassword("123456");

        alumno2 = new Alumno();
        alumno2.setId(2L);
        alumno2.setNombre("nombrealumno2");
        alumno2.setApellido("Apellido");
        alumno2.setEmail("alumno2@unlam.com");
        alumno2.setPassword("123456");

        profesor1 = new Profesor();
        profesor1.setId(3L);
        profesor1.setNombre("nombreprofesor1");
        profesor1.setApellido("Apellido");
        profesor1.setEmail("profesor1@unlam.com");
        profesor1.setPassword("123456");
        profesor1.setTema(programacion);

        profesor2 = new Profesor();
        profesor2.setId(4L);
        profesor2.setNombre("nombreprofesor2");
        profesor2.setApellido("Apellido");
        profesor2.setEmail("profesor2@unlam.com");
        profesor2.setPassword("123456");
        profesor2.setTema(diseño);

        archivo1 = new Archivo();
        archivo1.setId(1L);
        archivo1.setNombre("archivo1.pdf");
        archivo1.setTipoContenido("application/pdf");
        archivo1.setRutaAlmacenamiento("/uploads/archivo1.pdf");
        archivo1.setProfesor(profesor1);
        archivo1.setAlumno(alumno1);
        archivo1.setFechaSubida(LocalDateTime.now());

        archivo2 = new Archivo();
        archivo2.setId(2L);
        archivo2.setNombre("archivo2.docx");
        archivo2.setTipoContenido("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        archivo2.setRutaAlmacenamiento("/uploads/archivo2.docx");
        archivo2.setProfesor(profesor1);
        archivo2.setAlumno(alumno1);
        archivo2.setFechaSubida(LocalDateTime.now());

        alumno1.agregarProfesor(profesor1);
        profesor1.agregarAlumno(alumno1);

        when(requestMock.getSession()).thenReturn(sessionMock);
    }

    @Test
    public void mostrarArchivosConAlumnoLogueadoDeberiaMostrarArchivosCompartidos() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumno1);
        List<Archivo> archivosCompartidos = Arrays.asList(archivo1, archivo2);
        when(servicioArchivoMock.obtenerArchivosCompartidosConAlumnoPorSusProfesores(alumno1.getId()))
                .thenReturn(archivosCompartidos);
        when(repositorioUsuarioMock.obtenerProfesoresDeAlumno(alumno1.getId()))
                .thenReturn(Arrays.asList(profesor1));

        ModelAndView modelAndView = controladorArchivo.mostrarArchivos(requestMock, null, null);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("pantallaarchivos"));
        assertThat(modelAndView.getModel().get("nombreUsuario").toString(), equalToIgnoringCase("nombrealumno1"));
        assertThat(modelAndView.getModel().get("rol").toString(), equalToIgnoringCase("alumno"));
        assertEquals(modelAndView.getModel().get("archivos"), archivosCompartidos);
        assertThat(modelAndView.getModel().get("listaProfesores"), notNullValue());
        verify(servicioArchivoMock, times(1)).obtenerArchivosCompartidosConAlumnoPorSusProfesores(alumno1.getId());
    }

    @Test
    public void mostrarArchivosConProfesorLogueadoDeberiaMostrarArchivosDelProfesor() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesor1);
        List<Archivo> archivosDelProfesor = Arrays.asList(archivo1, archivo2);
        when(servicioArchivoMock.obtenerArchivosPorProfesor(profesor1.getId()))
                .thenReturn(archivosDelProfesor);
        when(repositorioUsuarioMock.obtenerAlumnosDeProfesor(profesor1.getId()))
                .thenReturn(Arrays.asList(alumno1));

        ModelAndView modelAndView = controladorArchivo.mostrarArchivos(requestMock, null, null);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("pantallaarchivos"));
        assertThat(modelAndView.getModel().get("nombreUsuario").toString(), equalToIgnoringCase("nombreprofesor1"));
        assertThat(modelAndView.getModel().get("rol").toString(), equalToIgnoringCase("profesor"));
        assertEquals(modelAndView.getModel().get("archivos"), archivosDelProfesor);
        assertThat(modelAndView.getModel().get("alumnosDelProfesor"), notNullValue());
        assertThat(modelAndView.getModel().get("listaAlumnos"), notNullValue());
        verify(servicioArchivoMock, times(1)).obtenerArchivosPorProfesor(profesor1.getId());
    }

    @Test
    public void mostrarArchivosSinUsuarioLogueadoDeberiaRedirigirALogin() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

        ModelAndView modelAndView = controladorArchivo.mostrarArchivos(requestMock, null, null);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
    }

    @Test
    public void mostrarArchivosConUsuarioNoReconocidoDeberiaMostrarError() {
        Usuario usuarioGenerico = mock(Usuario.class);
        when(usuarioGenerico.getNombre()).thenReturn("Usuario Generico");
        when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioGenerico);

        ModelAndView modelAndView = controladorArchivo.mostrarArchivos(requestMock, null, null);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
        assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("Rol de usuario no reconocido."));
    }

    @Test
    public void mostrarArchivosConBusquedaParaAlumnoDeberiaBuscarArchivos() {
        String busqueda = "programacion";
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumno1);
        List<Archivo> archivosEncontrados = Arrays.asList(archivo1);
        when(servicioArchivoMock.buscarArchivosAlumno(alumno1.getId(), busqueda))
                .thenReturn(archivosEncontrados);
        when(repositorioUsuarioMock.obtenerProfesoresDeAlumno(alumno1.getId()))
                .thenReturn(Arrays.asList(profesor1));

        ModelAndView modelAndView = controladorArchivo.mostrarArchivos(requestMock, busqueda, null);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("pantallaarchivos"));
        assertEquals(modelAndView.getModel().get("archivos"), archivosEncontrados);
        assertEquals(modelAndView.getModel().get("busquedaActual"), busqueda);
        verify(servicioArchivoMock, times(1)).buscarArchivosAlumno(alumno1.getId(), busqueda);
    }

    @Test
    public void mostrarArchivosConBusquedaParaProfesorDeberiaBuscarArchivos() {
        String busqueda = "java";
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesor1);
        List<Archivo> archivosEncontrados = Arrays.asList(archivo2);
        when(servicioArchivoMock.buscarArchivosProfesor(profesor1.getId(), busqueda))
                .thenReturn(archivosEncontrados);
        when(repositorioUsuarioMock.obtenerAlumnosDeProfesor(profesor1.getId()))
                .thenReturn(Arrays.asList(alumno1));

        ModelAndView modelAndView = controladorArchivo.mostrarArchivos(requestMock, busqueda, null);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("pantallaarchivos"));
        assertEquals(modelAndView.getModel().get("archivos"), archivosEncontrados);
        assertEquals(modelAndView.getModel().get("busquedaActual"), busqueda);
        verify(servicioArchivoMock, times(1)).buscarArchivosProfesor(profesor1.getId(), busqueda);
    }

    @Test
    public void mostrarArchivosConBusquedaVaciaParaAlumnoDeberiaIgnorarBusqueda() {
        String busquedaVacia = "   ";
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumno1);
        List<Archivo> archivosCompartidos = Arrays.asList(archivo1, archivo2);
        when(servicioArchivoMock.obtenerArchivosCompartidosConAlumnoPorSusProfesores(alumno1.getId()))
                .thenReturn(archivosCompartidos);
        when(repositorioUsuarioMock.obtenerProfesoresDeAlumno(alumno1.getId()))
                .thenReturn(Arrays.asList(profesor1));

        ModelAndView modelAndView = controladorArchivo.mostrarArchivos(requestMock, busquedaVacia, null);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("pantallaarchivos"));
        assertEquals(modelAndView.getModel().get("archivos"), archivosCompartidos);
        verify(servicioArchivoMock, times(1)).obtenerArchivosCompartidosConAlumnoPorSusProfesores(alumno1.getId());
        verify(servicioArchivoMock, never()).buscarArchivosAlumno(anyLong(), anyString());
    }

    @Test
    public void mostrarArchivosConFiltroPersonaParaAlumnoDeberiaFiltrarPorProfesor() {
        Long profesorId = profesor1.getId();
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumno1);
        List<Archivo> archivosCompartidos = Arrays.asList(archivo1);
        when(servicioArchivoMock.obtenerArchivosCompartidosEntreProfesorYAlumno(profesorId, alumno1.getId()))
                .thenReturn(archivosCompartidos);
        when(repositorioUsuarioMock.obtenerProfesoresDeAlumno(alumno1.getId()))
                .thenReturn(Arrays.asList(profesor1));

        ModelAndView modelAndView = controladorArchivo.mostrarArchivos(requestMock, null, "3");

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("pantallaarchivos"));
        assertEquals(modelAndView.getModel().get("archivos"), archivosCompartidos);
        assertEquals(modelAndView.getModel().get("filtroPersonaActual"), profesorId);
        verify(servicioArchivoMock, times(1)).obtenerArchivosCompartidosEntreProfesorYAlumno(profesorId, alumno1.getId());
    }

    @Test
    public void mostrarArchivosConFiltroPersonaParaProfesorDeberiaFiltrarPorAlumno() {
        Long alumnoId = alumno1.getId();
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesor1);
        List<Archivo> archivosCompartidos = Arrays.asList(archivo1);
        when(servicioArchivoMock.obtenerArchivosCompartidosEntreProfesorYAlumno(profesor1.getId(), alumnoId))
                .thenReturn(archivosCompartidos);
        when(repositorioUsuarioMock.obtenerAlumnosDeProfesor(profesor1.getId()))
                .thenReturn(Arrays.asList(alumno1));

        ModelAndView modelAndView = controladorArchivo.mostrarArchivos(requestMock, null, "1");

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("pantallaarchivos"));
        assertEquals(modelAndView.getModel().get("archivos"), archivosCompartidos);
        assertEquals(modelAndView.getModel().get("filtroPersonaActual"), alumnoId);
        verify(servicioArchivoMock, times(1)).obtenerArchivosCompartidosEntreProfesorYAlumno(profesor1.getId(), alumnoId);
    }

    @Test
    public void mostrarArchivosConBusquedaYFiltroDeberiaIgnorarFiltroYUsarBusqueda() {
        String busqueda = "programacion";
        Long profesorId = profesor1.getId();
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumno1);
        List<Archivo> archivosEncontrados = Arrays.asList(archivo1);
        when(servicioArchivoMock.buscarArchivosAlumno(alumno1.getId(), busqueda))
                .thenReturn(archivosEncontrados);
        when(repositorioUsuarioMock.obtenerProfesoresDeAlumno(alumno1.getId()))
                .thenReturn(Arrays.asList(profesor1));

        ModelAndView modelAndView = controladorArchivo.mostrarArchivos(requestMock, busqueda, "3");

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("pantallaarchivos"));
        assertEquals(modelAndView.getModel().get("archivos"), archivosEncontrados);
        assertEquals(modelAndView.getModel().get("busquedaActual"), busqueda);
        assertEquals(modelAndView.getModel().get("filtroPersonaActual"), profesorId);
        verify(servicioArchivoMock, times(1)).buscarArchivosAlumno(alumno1.getId(), busqueda);
        verify(servicioArchivoMock, never()).obtenerArchivosCompartidosEntreProfesorYAlumno(anyLong(), anyLong());
    }

    @Test
    public void mostrarFormularioSubidaConProfesorDeberiaMostrarFormulario() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesor1);
        when(repositorioUsuarioMock.buscarProfesorConAlumnos(profesor1.getId())).thenReturn(profesor1);

        ModelAndView modelAndView = controladorArchivo.mostrarFormularioSubida(requestMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("subir-archivo"));
        assertThat(modelAndView.getModel().get("nombreUsuario").toString(), equalToIgnoringCase("nombreprofesor1"));
        assertThat(modelAndView.getModel().get("rol").toString(), equalToIgnoringCase("profesor"));
        assertEquals(modelAndView.getModel().get("profesorId"), profesor1.getId());
        assertThat(modelAndView.getModel().get("alumnosDelProfesor"), notNullValue());
        assertThat(modelAndView.getModel().get("alumnosDisponibles"), notNullValue());
    }

    @Test
    public void mostrarFormularioSubidaSinUsuarioDeberiaRedirigirAArchivos() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(null);
        ModelAndView modelAndView = controladorArchivo.mostrarFormularioSubida(requestMock);
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/archivos"));
    }

    @Test
    public void mostrarFormularioSubidaConAlumnoDeberiaRedirigirAArchivos() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumno1);
        ModelAndView modelAndView = controladorArchivo.mostrarFormularioSubida(requestMock);
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/archivos"));
    }

    @Test
    public void mostrarFormularioSubidaConProfesorNoEncontradoDeberiaRedirigirALogin() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesor1);
        when(repositorioUsuarioMock.buscarProfesorConAlumnos(profesor1.getId())).thenReturn(null);
        ModelAndView modelAndView = controladorArchivo.mostrarFormularioSubida(requestMock);
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
    }

    @Test
    public void subirArchivoConDatosValidosDeberiaSubirExitosamente() throws IOException {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesor1);
        when(fileMock.isEmpty()).thenReturn(false);
        when(fileMock.getOriginalFilename()).thenReturn("archivo1.pdf");
        when(repositorioUsuarioMock.buscarProfesorConAlumnos(profesor1.getId())).thenReturn(profesor1);
        when(repositorioUsuarioMock.buscarPorId(alumno1.getId())).thenReturn(alumno1);
        when(repositorioUsuarioMock.alumnoPertenece(alumno1.getId(), profesor1.getId())).thenReturn(true);
        ModelAndView modelAndView = controladorArchivo.subirArchivo(
                fileMock, profesor1.getId(), alumno1.getId(), requestMock, redirectAttributesMock);
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/archivos"));
        verify(servicioArchivoMock, times(1)).subirArchivo(fileMock, profesor1.getId(), alumno1.getId());
        verify(redirectAttributesMock, times(1)).addFlashAttribute("mensaje", "Archivo subido exitosamente.");
    }

    @Test
    public void subirArchivoSinUsuarioDeberiaRedirigirALogin() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(null);
        ModelAndView modelAndView = controladorArchivo.subirArchivo(
                fileMock, profesor1.getId(), alumno1.getId(), requestMock, redirectAttributesMock);
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
        verify(redirectAttributesMock, times(1)).addFlashAttribute("error", "Debes iniciar sesión para subir archivos.");
    }

    @Test
    public void subirArchivoConAlumnoDeberiaRedirigirAArchivos() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumno1);
        ModelAndView modelAndView = controladorArchivo.subirArchivo(
                fileMock, profesor1.getId(), alumno1.getId(), requestMock, redirectAttributesMock);
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/archivos"));
        verify(redirectAttributesMock, times(1)).addFlashAttribute("error", "Solo los profesores pueden subir archivos.");
    }

    @Test
    public void subirArchivoConProfesorIncorrectoDeberiaRedirigirAArchivos() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesor2);

        ModelAndView modelAndView = controladorArchivo.subirArchivo(
                fileMock, profesor1.getId(), alumno1.getId(), requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/archivos"));
        verify(redirectAttributesMock, times(1)).addFlashAttribute("error", "No tenes permiso para subir archivos como este profesor.");
    }

    @Test
    public void subirArchivoVacioDeberiaRedirigirConError() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesor1);
        when(fileMock.isEmpty()).thenReturn(true);

        ModelAndView modelAndView = controladorArchivo.subirArchivo(
                fileMock, profesor1.getId(), alumno1.getId(), requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/archivos/subir"));
        verify(redirectAttributesMock, times(1)).addFlashAttribute("error", "Debes seleccionar un archivo para subir.");
    }

    @Test
    public void subirArchivoConProfesorNoEncontradoDeberiaRedirigirConError() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesor1);
        when(fileMock.isEmpty()).thenReturn(false);
        when(repositorioUsuarioMock.buscarProfesorConAlumnos(profesor1.getId())).thenReturn(null);

        ModelAndView modelAndView = controladorArchivo.subirArchivo(
                fileMock, profesor1.getId(), alumno1.getId(), requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/archivos/subir"));
        verify(redirectAttributesMock, times(1)).addFlashAttribute("error", "Profesor no encontrado.");
    }

    @Test
    public void subirArchivoConAlumnoNoEncontradoDeberiaRedirigirConError() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesor1);
        when(fileMock.isEmpty()).thenReturn(false);
        when(repositorioUsuarioMock.buscarProfesorConAlumnos(profesor1.getId())).thenReturn(profesor1);
        when(repositorioUsuarioMock.buscarPorId(alumno1.getId())).thenReturn(null);

        ModelAndView modelAndView = controladorArchivo.subirArchivo(
                fileMock, profesor1.getId(), alumno1.getId(), requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/archivos/subir"));
        verify(redirectAttributesMock, times(1)).addFlashAttribute("error", "Alumno no encontrado.");
    }

    @Test
    public void subirArchivoConAlumnoNoAsociadoDeberiaRedirigirConError() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesor1);
        when(fileMock.isEmpty()).thenReturn(false);
        when(repositorioUsuarioMock.buscarProfesorConAlumnos(profesor1.getId())).thenReturn(profesor1);
        when(repositorioUsuarioMock.buscarPorId(alumno1.getId())).thenReturn(alumno1);
        when(repositorioUsuarioMock.alumnoPertenece(alumno1.getId(), profesor1.getId())).thenReturn(false);

        ModelAndView modelAndView = controladorArchivo.subirArchivo(
                fileMock, profesor1.getId(), alumno1.getId(), requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/archivos/subir"));
        verify(redirectAttributesMock, times(1)).addFlashAttribute("error", "El alumno seleccionado no está suscripto con este profesor.");
    }

    @Test
    public void subirArchivoConIOExceptionDeberiaRedirigirConError() throws IOException {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesor1);
        when(fileMock.isEmpty()).thenReturn(false);
        when(repositorioUsuarioMock.buscarProfesorConAlumnos(profesor1.getId())).thenReturn(profesor1);
        when(repositorioUsuarioMock.buscarPorId(alumno1.getId())).thenReturn(alumno1);
        when(repositorioUsuarioMock.alumnoPertenece(alumno1.getId(), profesor1.getId())).thenReturn(true);
        doThrow(new IOException("Error de IO")).when(servicioArchivoMock).subirArchivo(any(), any(), any());

        ModelAndView modelAndView = controladorArchivo.subirArchivo(
                fileMock, profesor1.getId(), alumno1.getId(), requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/archivos"));
        verify(redirectAttributesMock, times(1)).addFlashAttribute(eq("error"), contains("Error al subir el archivo"));
    }

    @Test
    public void verArchivoConProfesorPropietarioDeberiaRetornarArchivo() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesor1);
        when(servicioArchivoMock.buscarArchivoPorId(archivo1.getId())).thenReturn(archivo1);

        ResponseEntity<Resource> response = controladorArchivo.verArchivo(archivo1.getId(), requestMock);

        assertNotNull(response);
    }

    @Test
    public void verArchivoConAlumnoPropietarioDeberiaRetornarArchivo() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumno1);
        when(servicioArchivoMock.buscarArchivoPorId(archivo1.getId())).thenReturn(archivo1);

        ResponseEntity<Resource> response = controladorArchivo.verArchivo(archivo1.getId(), requestMock);

        assertNotNull(response);
    }

    @Test
    public void verArchivoConUsuarioSinPermisosDeberiaRetornarForbidden() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumno2);
        when(servicioArchivoMock.buscarArchivoPorId(archivo1.getId())).thenReturn(archivo1);

        ResponseEntity<Resource> response = controladorArchivo.verArchivo(archivo1.getId(), requestMock);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void verArchivoNoExistenteDeberiaRetornarNotFound() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesor1);
        when(servicioArchivoMock.buscarArchivoPorId(999L)).thenReturn(null);

        ResponseEntity<Resource> response = controladorArchivo.verArchivo(999L, requestMock);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void descargarArchivoConProfesorPropietarioDeberiaPermitirDescarga() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesor1);
        when(servicioArchivoMock.buscarArchivoPorId(archivo1.getId())).thenReturn(archivo1);

        ResponseEntity<Resource> response = controladorArchivo.descargarArchivo(archivo1.getId(), requestMock);

        assertNotNull(response);
    }



    @Test
    public void descargarArchivoConUsuarioSinPermisosDeberiaRetornarForbidden() {
        Profesor otroProfesor = new Profesor();
        otroProfesor.setId(5L); // ID diferente para evitar conflictos
        otroProfesor.setNombre("nombreprofesor2");

        when(sessionMock.getAttribute("USUARIO")).thenReturn(otroProfesor);
        when(servicioArchivoMock.buscarArchivoPorId(archivo1.getId())).thenReturn(archivo1);

        ResponseEntity<Resource> response = controladorArchivo.descargarArchivo(archivo1.getId(), requestMock);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void eliminarArchivoConProfesorPropietarioDeberiaEliminarExitosamente() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesor1);
        when(servicioArchivoMock.buscarArchivoPorId(archivo1.getId())).thenReturn(archivo1);

        ModelAndView modelAndView = controladorArchivo.eliminarArchivo(archivo1.getId(), requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/archivos"));
        verify(servicioArchivoMock, times(1)).eliminarArchivo(archivo1.getId());
        verify(redirectAttributesMock, times(1)).addFlashAttribute("mensaje", "Archivo eliminado exitosamente.");
    }

    @Test
    public void eliminarArchivoSinUsuarioDeberiaRedirigirConError() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

        ModelAndView modelAndView = controladorArchivo.eliminarArchivo(archivo1.getId(), requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/archivos"));
        verify(redirectAttributesMock, times(1)).addFlashAttribute("error", "No tenes permiso para eliminar archivos.");
    }

    @Test
    public void eliminarArchivoConAlumnoDeberiaRedirigirConError() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumno1);

        ModelAndView modelAndView = controladorArchivo.eliminarArchivo(archivo1.getId(), requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/archivos"));
        verify(redirectAttributesMock, times(1)).addFlashAttribute("error", "No tenes permiso para eliminar archivos.");
    }

    @Test
    public void eliminarArchivoConProfesorSinPermisosDeberiaRedirigirConError() {
        Profesor otroProfesor = new Profesor();
        otroProfesor.setId(5L); // ID diferente para evitar conflictos
        otroProfesor.setNombre("nombreprofesor2");

        when(sessionMock.getAttribute("USUARIO")).thenReturn(otroProfesor);
        when(servicioArchivoMock.buscarArchivoPorId(archivo1.getId())).thenReturn(archivo1);

        ModelAndView modelAndView = controladorArchivo.eliminarArchivo(archivo1.getId(), requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/archivos"));
        verify(redirectAttributesMock, times(1)).addFlashAttribute("error", "No tenes permiso para eliminar este archivo.");
    }

    @Test
    public void eliminarArchivoConExceptionDeberiaRedirigirConError() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesor1);
        when(servicioArchivoMock.buscarArchivoPorId(archivo1.getId())).thenReturn(archivo1);
        doThrow(new RuntimeException("Error al eliminar")).when(servicioArchivoMock).eliminarArchivo(archivo1.getId());

        ModelAndView modelAndView = controladorArchivo.eliminarArchivo(archivo1.getId(), requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/archivos"));
        verify(redirectAttributesMock, times(1)).addFlashAttribute(eq("error"), contains("Error al eliminar el archivo"));
    }

    @Test
    public void mostrarArchivosConProfesorSinArchivosDeberiaRetornarListaVacia() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesor1);
        when(servicioArchivoMock.obtenerArchivosPorProfesor(profesor1.getId())).thenReturn(Arrays.asList());
        when(repositorioUsuarioMock.obtenerAlumnosDeProfesor(profesor1.getId())).thenReturn(Arrays.asList());

        ModelAndView modelAndView = controladorArchivo.mostrarArchivos(requestMock, null, null);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("pantallaarchivos"));
        List<Archivo> archivos = (List<Archivo>) modelAndView.getModel().get("archivos");
        assertEquals(0, archivos.size());
    }

    @Test
    public void mostrarArchivosConAlumnoSinArchivosCompartidosDeberiaRetornarListaVacia() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumno1);
        when(servicioArchivoMock.obtenerArchivosCompartidosConAlumnoPorSusProfesores(alumno1.getId()))
                .thenReturn(Arrays.asList());
        when(repositorioUsuarioMock.obtenerProfesoresDeAlumno(alumno1.getId())).thenReturn(Arrays.asList());

        ModelAndView modelAndView = controladorArchivo.mostrarArchivos(requestMock, null, null);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("pantallaarchivos"));
        List<Archivo> archivos = (List<Archivo>) modelAndView.getModel().get("archivos");
        assertEquals(0, archivos.size());
    }
}