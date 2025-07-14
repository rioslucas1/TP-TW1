package com.tallerwebi.integracion;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.servicios.ServicioArchivo;
import com.tallerwebi.dominio.servicios.ServicioLogin;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Archivo;
import com.tallerwebi.dominio.entidades.Tema;
import com.tallerwebi.presentacion.ControladorArchivo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SpringWebTestConfig.class, HibernateTestConfig.class, ControladorArchivoTest.TestConfig.class})
public class ControladorArchivoTest {


    private Alumno alumnoReal;
    private Profesor profesorReal;
    private Archivo archivoReal;
    private List<Archivo> archivosMock;
    private List<Profesor> profesoresMock;
    private List<Alumno> alumnosMock;

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    @Autowired
    private ServicioArchivo servicioArchivo;

    @Autowired
    private ServicioLogin servicioLogin;

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    @Configuration
    static class TestConfig {
        @Bean
        @Primary
        public ServicioArchivo servicioArchivoMock() {
            return mock(ServicioArchivo.class);
        }

        @Bean
        @Primary
        public ServicioLogin servicioLoginMock() {
            return mock(ServicioLogin.class);
        }

        @Bean
        @Primary
        public RepositorioUsuario repositorioUsuarioMock() {
            return mock(RepositorioUsuario.class);
        }
    }

    @BeforeEach
    public void init() throws IOException {
        alumnoReal = new Alumno();
        alumnoReal.setId(1L);
        alumnoReal.setEmail("alumno@unlam.com");
        alumnoReal.setNombre("Alumno Test");
        alumnoReal.setApellido("Apellido Test");
        alumnoReal.setPassword("password123");

        profesorReal = new Profesor();
        profesorReal.setId(2L);
        profesorReal.setEmail("profesor@unlam.com");
        profesorReal.setNombre("Profesor Test");
        profesorReal.setApellido("Apellido Test");
        profesorReal.setPassword("password123");

        Tema tema = new Tema();
        tema.setId(1L);
        tema.setNombre("Matemáticas");
        profesorReal.setTema(tema);

        Path tempFile = Files.createTempFile("test", ".pdf");
        Files.write(tempFile, "contenido test".getBytes());

        archivoReal = new Archivo();
        archivoReal.setId(1L);
        archivoReal.setNombre("test.pdf");
        archivoReal.setTipoContenido("application/pdf");
        archivoReal.setRutaAlmacenamiento(tempFile.toString());
        archivoReal.setProfesor(profesorReal);
        archivoReal.setAlumno(alumnoReal);
        archivoReal.setFechaSubida(LocalDateTime.now());

        archivosMock = Arrays.asList(archivoReal);
        profesoresMock = Arrays.asList(profesorReal);
        alumnosMock = Arrays.asList(alumnoReal);


        Mockito.reset(servicioArchivo, servicioLogin, repositorioUsuario);

        when(repositorioUsuario.obtenerProfesoresDeAlumno(1L)).thenReturn(profesoresMock);
        when(repositorioUsuario.obtenerAlumnosDeProfesor(2L)).thenReturn(alumnosMock);
        when(repositorioUsuario.buscarPorId(1L)).thenReturn(alumnoReal);
        when(repositorioUsuario.buscarPorId(2L)).thenReturn(profesorReal);
        when(repositorioUsuario.buscarProfesorConAlumnos(2L)).thenReturn(profesorReal);
        when(repositorioUsuario.alumnoPertenece(1L, 2L)).thenReturn(true);

        profesorReal.setAlumnos(Arrays.asList(alumnoReal));

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }




    @AfterEach
    public void cleanup() throws IOException {
        if (archivoReal != null && archivoReal.getRutaAlmacenamiento() != null) {
            Path filePath = Paths.get(archivoReal.getRutaAlmacenamiento());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            Path parentDir = filePath.getParent();
            if (parentDir != null && Files.exists(parentDir) &&
                    parentDir.getFileName().toString().startsWith("test-archivos")) {
                Files.delete(parentDir);
            }
        }
    }


    @Test
    public void queAlAccederAArchivosConAlumnoMuestraLosArchivosCompartidos() throws Exception {
        when(servicioArchivo.obtenerArchivosCompartidosConAlumnoPorSusProfesores(1L)).thenReturn(archivosMock);

        MvcResult result = mockMvc.perform(get("/archivos")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("pantallaarchivos"));
        assertThat(modelAndView.getModel().get("nombreUsuario"), is("Alumno Test"));
        assertThat(modelAndView.getModel().get("rol"), is("alumno"));
        assertThat(modelAndView.getModel().get("archivos"), is(archivosMock));
        assertThat(modelAndView.getModel().get("listaProfesores"), is(profesoresMock));

        verify(servicioArchivo).obtenerArchivosCompartidosConAlumnoPorSusProfesores(1L);
        verify(repositorioUsuario).obtenerProfesoresDeAlumno(1L);
    }

    @Test
    public void queAlAccederAArchivosConProfesorMuestraLosArchivosDelProfesor() throws Exception {
        when(servicioArchivo.obtenerArchivosPorProfesor(2L)).thenReturn(archivosMock);

        MvcResult result = mockMvc.perform(get("/archivos")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("pantallaarchivos"));
        assertThat(modelAndView.getModel().get("nombreUsuario"), is("Profesor Test"));
        assertThat(modelAndView.getModel().get("rol"), is("profesor"));
        assertThat(modelAndView.getModel().get("archivos"), is(archivosMock));
        assertThat(modelAndView.getModel().get("listaAlumnos"), is(alumnosMock));

        verify(servicioArchivo).obtenerArchivosPorProfesor(2L);
        verify(repositorioUsuario, times(2)).obtenerAlumnosDeProfesor(2L);
    }

    @Test
    public void queAlAccederAArchivosSinUsuarioEnSesionRedirigeALogin() throws Exception {
        mockMvc.perform(get("/archivos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void queAlBuscarArchivosComoAlumnoDevuelveResultadosFiltrados() throws Exception {
        when(servicioArchivo.buscarArchivosAlumno(1L, "test")).thenReturn(archivosMock);

        MvcResult result = mockMvc.perform(get("/archivos")
                        .param("busqueda", "test")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("pantallaarchivos"));
        assertThat(modelAndView.getModel().get("archivos"), is(archivosMock));
        assertThat(modelAndView.getModel().get("busquedaActual"), is("test"));

        verify(servicioArchivo).buscarArchivosAlumno(1L, "test");
    }

    @Test
    public void queAlBuscarArchivosComoProfesorDevuelveResultadosFiltrados() throws Exception {
        when(servicioArchivo.buscarArchivosProfesor(2L, "test")).thenReturn(archivosMock);

        MvcResult result = mockMvc.perform(get("/archivos")
                        .param("busqueda", "test")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("pantallaarchivos"));
        assertThat(modelAndView.getModel().get("archivos"), is(archivosMock));
        assertThat(modelAndView.getModel().get("busquedaActual"), is("test"));

        verify(servicioArchivo).buscarArchivosProfesor(2L, "test");
    }

    @Test
    public void queAlFiltrarArchivosPorPersonaComoAlumnoDevuelveArchivosCompartidos() throws Exception {
        when(servicioArchivo.obtenerArchivosCompartidosEntreProfesorYAlumno(2L, 1L)).thenReturn(archivosMock);

        MvcResult result = mockMvc.perform(get("/archivos")
                        .param("filtroPersona", "2")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("pantallaarchivos"));
        assertThat(modelAndView.getModel().get("archivos"), is(archivosMock));
        assertThat(modelAndView.getModel().get("filtroPersonaActual"), is(2L));

        verify(servicioArchivo).obtenerArchivosCompartidosEntreProfesorYAlumno(2L, 1L);
    }

    @Test
    public void queAlAccederASubirArchivoComoProfesorMuestraElFormulario() throws Exception {
        MvcResult result = mockMvc.perform(get("/archivos/subir")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("subir-archivo"));
        assertThat(modelAndView.getModel().get("profesorId"), is(2L));
        assertThat(modelAndView.getModel().get("nombreUsuario"), is("Profesor Test"));
        assertThat(modelAndView.getModel().get("rol"), is("profesor"));
        assertThat(modelAndView.getModel().get("alumnosDelProfesor"), is(instanceOf(List.class)));

        verify(repositorioUsuario).buscarProfesorConAlumnos(2L);
    }

    @Test
    public void queAlAccederASubirArchivoComoAlumnoRedirigeAArchivos() throws Exception {
        mockMvc.perform(get("/archivos/subir")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/archivos"));
    }

    @Test
    public void queAlAccederASubirArchivoSinUsuarioRedirigeAArchivos() throws Exception {
        mockMvc.perform(get("/archivos/subir"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/archivos"));
    }

    @Test
    public void queAlSubirArchivoValidoComoProfesorRedirigeAArchivos() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "contenido test".getBytes());

        doNothing().when(servicioArchivo).subirArchivo(any(), eq(2L), eq(1L));

        mockMvc.perform(multipart("/archivos/subir")
                        .file(file)
                        .param("profesorId", "2")
                        .param("alumnoId", "1")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/archivos"));

        verify(servicioArchivo).subirArchivo(any(), eq(2L), eq(1L));
    }

    @Test
    public void queAlSubirArchivoSinSeleccionarArchivoRedirigeConError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "", "application/pdf", new byte[0]);

        mockMvc.perform(multipart("/archivos/subir")
                        .file(file)
                        .param("profesorId", "2")
                        .param("alumnoId", "1")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/archivos/subir"));
    }

    @Test
    public void queAlSubirArchivoComoAlumnoRedirigeConError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "contenido test".getBytes());

        mockMvc.perform(multipart("/archivos/subir")
                        .file(file)
                        .param("profesorId", "2")
                        .param("alumnoId", "1")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/archivos"));
    }

    @Test
    public void queAlSubirArchivoConProfesorIncorrectoRedirigeConError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "contenido test".getBytes());

        mockMvc.perform(multipart("/archivos/subir")
                        .file(file)
                        .param("profesorId", "3")
                        .param("alumnoId", "1")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/archivos"));
    }

    @Test
    public void queAlSubirArchivoConAlumnoNoSuscriptoRedirigeConError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "contenido test".getBytes());

        when(repositorioUsuario.alumnoPertenece(1L, 2L)).thenReturn(false);

        mockMvc.perform(multipart("/archivos/subir")
                        .file(file)
                        .param("profesorId", "2")
                        .param("alumnoId", "1")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/archivos/subir"));
    }

    @Test
    public void queAlSubirArchivoConIOExceptionRedirigeConError() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "contenido test".getBytes());

        doThrow(new IOException("Error de E/S")).when(servicioArchivo).subirArchivo(any(), eq(2L), eq(1L));

        mockMvc.perform(multipart("/archivos/subir")
                        .file(file)
                        .param("profesorId", "2")
                        .param("alumnoId", "1")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/archivos"));
    }

    @Test
    public void queAlVerArchivoComoProfesorPropietarioDevuelveElArchivo() throws Exception {
        when(servicioArchivo.buscarArchivoPorId(1L)).thenReturn(archivoReal);

        mockMvc.perform(get("/archivos/ver/1")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"test.pdf\""));

        verify(servicioArchivo).buscarArchivoPorId(1L);
    }

    @Test
    public void queAlVerArchivoComoAlumnoPropietarioDevuelveElArchivo() throws Exception {
        when(servicioArchivo.buscarArchivoPorId(1L)).thenReturn(archivoReal);

        mockMvc.perform(get("/archivos/ver/1")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"test.pdf\""));

        verify(servicioArchivo).buscarArchivoPorId(1L);
    }

    @Test
    public void queAlVerArchivoSinPermisoDevuelveForbidden() throws Exception {
        Profesor otroProfesor = new Profesor();
        otroProfesor.setId(3L);
        otroProfesor.setNombre("Otro Profesor");

        when(servicioArchivo.buscarArchivoPorId(1L)).thenReturn(archivoReal);

        mockMvc.perform(get("/archivos/ver/1")
                        .sessionAttr("USUARIO", otroProfesor))
                .andExpect(status().isForbidden());

        verify(servicioArchivo).buscarArchivoPorId(1L);
    }

    @Test
    public void queAlVerArchivoInexistenteDevuelveNotFound() throws Exception {
        when(servicioArchivo.buscarArchivoPorId(999L)).thenReturn(null);

        mockMvc.perform(get("/archivos/ver/999")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().isNotFound());

        verify(servicioArchivo).buscarArchivoPorId(999L);
    }

    @Test
    public void queAlDescargarArchivoComoProfesorPropietarioDevuelveElArchivo() throws Exception {
        when(servicioArchivo.buscarArchivoPorId(1L)).thenReturn(archivoReal);

        mockMvc.perform(get("/archivos/descargar/1")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.pdf\""));

        verify(servicioArchivo).buscarArchivoPorId(1L);
    }

    @Test
    public void queAlDescargarArchivoComoAlumnoPropietarioDevuelveElArchivo() throws Exception {
        when(servicioArchivo.buscarArchivoPorId(1L)).thenReturn(archivoReal);

        mockMvc.perform(get("/archivos/descargar/1")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.pdf\""));

        verify(servicioArchivo).buscarArchivoPorId(1L);
    }

    @Test
    public void queAlDescargarArchivoSinPermisoDevuelveForbidden() throws Exception {
        Profesor otroProfesor = new Profesor();
        otroProfesor.setId(3L);
        otroProfesor.setNombre("Otro Profesor");

        when(servicioArchivo.buscarArchivoPorId(1L)).thenReturn(archivoReal);

        mockMvc.perform(get("/archivos/descargar/1")
                        .sessionAttr("USUARIO", otroProfesor))
                .andExpect(status().isForbidden());

        verify(servicioArchivo).buscarArchivoPorId(1L);
    }

    @Test
    public void queAlEliminarArchivoComoProfesorPropietarioRedirigeAArchivos() throws Exception {
        when(servicioArchivo.buscarArchivoPorId(1L)).thenReturn(archivoReal);
        doNothing().when(servicioArchivo).eliminarArchivo(1L);

        mockMvc.perform(post("/archivos/eliminar/1")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/archivos"));

        verify(servicioArchivo).buscarArchivoPorId(1L);
        verify(servicioArchivo).eliminarArchivo(1L);
    }

    @Test
    public void queAlEliminarArchivoComoAlumnoRedirigeConError() throws Exception {
        mockMvc.perform(post("/archivos/eliminar/1")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/archivos"));

        verify(servicioArchivo, never()).eliminarArchivo(anyLong());
    }

    @Test
    public void queAlEliminarArchivoSinUsuarioRedirigeConError() throws Exception {
        mockMvc.perform(post("/archivos/eliminar/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/archivos"));

        verify(servicioArchivo, never()).eliminarArchivo(anyLong());
    }

    @Test
    public void queAlEliminarArchivoDeOtroProfesorRedirigeConError() throws Exception {
        Profesor otroProfesor = new Profesor();
        otroProfesor.setId(3L);
        otroProfesor.setNombre("Otro Profesor");

        when(servicioArchivo.buscarArchivoPorId(1L)).thenReturn(archivoReal);

        mockMvc.perform(post("/archivos/eliminar/1")
                        .sessionAttr("USUARIO", otroProfesor))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/archivos"));

        verify(servicioArchivo).buscarArchivoPorId(1L);
        verify(servicioArchivo, never()).eliminarArchivo(anyLong());
    }

    @Test
    public void queAlEliminarArchivoConExceptionRedirigeConError() throws Exception {
        when(servicioArchivo.buscarArchivoPorId(1L)).thenReturn(archivoReal);
        doThrow(new RuntimeException("Error al eliminar")).when(servicioArchivo).eliminarArchivo(1L);

        mockMvc.perform(post("/archivos/eliminar/1")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/archivos"));

        verify(servicioArchivo).buscarArchivoPorId(1L);
        verify(servicioArchivo).eliminarArchivo(1L);
    }
}