package com.tallerwebi.integracion;

import com.tallerwebi.dominio.servicios.ServicioTema;
import com.tallerwebi.dominio.servicios.ServicioFeedback;
import com.tallerwebi.dominio.servicios.ServicioExperiencia;
import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.presentacion.ControladorPerfilProfesor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SpringWebTestConfig.class, HibernateTestConfig.class, ControladorPerfilProfesorTest.TestConfig.class})
public class ControladorPerfilProfesorTest {


    private Profesor profesorReal;
    private Alumno alumnoReal;
    private Tema temaReal;
    private List<Tema> temasMock;
    private List<ExperienciaEstudiantil> experienciasMock;
    private List<FeedbackProfesor> feedbacksMock;
    private ExperienciaEstudiantil experienciaReal;
    private FeedbackProfesor feedbackReal;

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    @Autowired
    private ServicioTema servicioTema;

    @Autowired
    private ServicioFeedback servicioFeedback;

    @Autowired
    private ServicioExperiencia servicioExperiencia;

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    @Configuration
    static class TestConfig {
        @Bean
        @Primary
        public ServicioTema servicioTemaMock() {
            return mock(ServicioTema.class);
        }

        @Bean
        @Primary
        public ServicioFeedback servicioFeedbackMock() {
            return mock(ServicioFeedback.class);
        }

        @Bean
        @Primary
        public ServicioExperiencia servicioExperienciaMock() {
            return mock(ServicioExperiencia.class);
        }

        @Bean
        @Primary
        public RepositorioUsuario repositorioUsuarioMock() {
            return mock(RepositorioUsuario.class);
        }
    }

    @BeforeEach
    public void init() {
        temaReal = new Tema();
        temaReal.setId(1L);
        temaReal.setNombre("Matemáticas");

        profesorReal = new Profesor();
        profesorReal.setId(1L);
        profesorReal.setNombre("Juan");
        profesorReal.setApellido("Pérez");
        profesorReal.setEmail("profesor@unlam.com");
        profesorReal.setPassword("password123");
        profesorReal.setTema(temaReal);
        profesorReal.setDescripcion("Profesor de matemáticas con 10 años de experiencia");
        profesorReal.setModalidad(ModalidadPreferida.PRESENCIAL);


        alumnoReal = new Alumno();
        alumnoReal.setId(2L);
        alumnoReal.setNombre("Ana");
        alumnoReal.setApellido("García");
        alumnoReal.setEmail("alumno@unlam.com");
        experienciaReal = new ExperienciaEstudiantil();
        experienciaReal.setId(1L);
        experienciaReal.setInstitucion("Universidad Nacional de La Matanza");
        experienciaReal.setDescripcion("Tecnicatura en desarrollo web");
        experienciaReal.setFecha("2020-12-15");
        experienciaReal.setProfesor(profesorReal);

        feedbackReal = new FeedbackProfesor();
        feedbackReal.setId(1L);
        feedbackReal.setComentario("Excelente profesor, muy claro en sus explicaciones");
        feedbackReal.setCalificacion(5);
        feedbackReal.setFechaCreacion(LocalDateTime.now());
        feedbackReal.setProfesor(profesorReal);
        feedbackReal.setAlumno(alumnoReal);


        temasMock = Arrays.asList(temaReal);
        experienciasMock = Arrays.asList(experienciaReal);
        feedbacksMock = Arrays.asList(feedbackReal);


        reset(servicioTema, servicioFeedback, servicioExperiencia, repositorioUsuario);

        when(repositorioUsuario.buscarProfesorConExperiencias(1L)).thenReturn(profesorReal);
        when(repositorioUsuario.buscarPorId(1L)).thenReturn(profesorReal);
        when(servicioTema.obtenerTodos()).thenReturn(temasMock);
        when(servicioTema.obtenerPorId(1L)).thenReturn(temaReal);
        when(servicioExperiencia.obtenerExperienciasPorProfesor(1L)).thenReturn(experienciasMock);
        when(servicioFeedback.obtenerFeedbacksPorProfesor(1L)).thenReturn(feedbacksMock);
        when(servicioFeedback.calcularPromedioCalificacion(1L)).thenReturn(4.5);
        when(servicioFeedback.contarFeedbackPorProfesor(1L)).thenReturn(10);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void queAlVerPerfilProfesorMuestraLaInformacionCompleta() throws Exception {
        MvcResult result = mockMvc.perform(get("/profesor/perfil")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("verMiPerfilProfesor"));
        assertThat(modelAndView.getModel().get("profesor"), is(profesorReal));
        assertThat(modelAndView.getModel().get("feedbacks"), is(feedbacksMock));
        assertThat(modelAndView.getModel().get("experiencias"), is(experienciasMock));
        assertThat(modelAndView.getModel().get("todosLosTemas"), is(temasMock));
        assertThat(modelAndView.getModel().get("promedioCalificacion"), is(4.5));
        assertThat(modelAndView.getModel().get("totalResenas"), is(10));
        assertThat(modelAndView.getModel().get("esEdicion"), is(false));
        assertThat(modelAndView.getModel().get("fechasFormateadas"), is(instanceOf(Map.class)));

        verify(repositorioUsuario).buscarProfesorConExperiencias(1L);
        verify(servicioExperiencia).obtenerExperienciasPorProfesor(1L);
        verify(servicioFeedback).obtenerFeedbacksPorProfesor(1L);
        verify(servicioTema).obtenerTodos();
        verify(servicioFeedback).calcularPromedioCalificacion(1L);
        verify(servicioFeedback).contarFeedbackPorProfesor(1L);
    }

    @Test
    public void queAlVerPerfilSinUsuarioEnSesionRedirigeALogin() throws Exception {
        mockMvc.perform(get("/profesor/perfil"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void queAlVerPerfilConAlumnoEnSesionRedirigeAHome() throws Exception {
        mockMvc.perform(get("/profesor/perfil")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    public void queAlEditarPerfilProfesorMuestraFormularioDeEdicion() throws Exception {
        MvcResult result = mockMvc.perform(get("/profesor/perfil/editar")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("verMiPerfilProfesor"));
        assertThat(modelAndView.getModel().get("profesor"), is(profesorReal));
        assertThat(modelAndView.getModel().get("todosLosTemas"), is(temasMock));
        assertThat(modelAndView.getModel().get("experiencias"), is(experienciasMock));
        assertThat(modelAndView.getModel().get("esEdicion"), is(true));

        verify(repositorioUsuario).buscarProfesorConExperiencias(1L);
        verify(servicioExperiencia).obtenerExperienciasPorProfesor(1L);
        verify(servicioTema).obtenerTodos();
    }

    @Test
    public void queAlEditarPerfilSinUsuarioEnSesionRedirigeALogin() throws Exception {
        mockMvc.perform(get("/profesor/perfil/editar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void queAlEditarPerfilConAlumnoEnSesionRedirigeAHome() throws Exception {
        mockMvc.perform(get("/profesor/perfil/editar")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    public void queAlActualizarPerfilConDatosValidosActualizaCorrectamente() throws Exception {
        doNothing().when(repositorioUsuario).modificar(any(Profesor.class));

        mockMvc.perform(post("/profesor/perfil/actualizar")
                        .sessionAttr("USUARIO", profesorReal)
                        .param("nombre", "Carlos")
                        .param("apellido", "López")
                        .param("descripcion", "Nueva descripción")
                        .param("modalidadPreferida", "VIRTUAL")
                        .param("temaId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/perfil"));

        verify(repositorioUsuario).buscarPorId(1L);
        verify(servicioTema).obtenerPorId(1L);
        verify(repositorioUsuario).modificar(any(Profesor.class));
    }

    @Test
    public void queAlActualizarPerfilSinNombreDevuelveError() throws Exception {
        mockMvc.perform(post("/profesor/perfil/actualizar")
                        .sessionAttr("USUARIO", profesorReal)
                        .param("nombre", "")
                        .param("apellido", "López")
                        .param("descripcion", "Nueva descripción")
                        .param("modalidadPreferida", "VIRTUAL")
                        .param("temaId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/perfil/editar"));
    }

    @Test
    public void queAlActualizarPerfilSinApellidoDevuelveError() throws Exception {
        mockMvc.perform(post("/profesor/perfil/actualizar")
                        .sessionAttr("USUARIO", profesorReal)
                        .param("nombre", "Carlos")
                        .param("apellido", "")
                        .param("descripcion", "Nueva descripción")
                        .param("modalidadPreferida", "VIRTUAL")
                        .param("temaId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/perfil/editar"));
    }

    @Test
    public void queAlActualizarPerfilSinUsuarioEnSesionRedirigeALogin() throws Exception {
        mockMvc.perform(post("/profesor/perfil/actualizar")
                        .param("nombre", "Carlos")
                        .param("apellido", "López"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void queAlActualizarPerfilConAlumnoEnSesionRedirigeAHome() throws Exception {
        mockMvc.perform(post("/profesor/perfil/actualizar")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("nombre", "Carlos")
                        .param("apellido", "López"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    public void queAlActualizarPerfilConFotoBase64ValidaActualizaCorrectamente() throws Exception {
        String fotoBase64Valida = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAoKCgoKCgsMDAsPEA==";
        doNothing().when(repositorioUsuario).modificar(any(Profesor.class));

        mockMvc.perform(post("/profesor/perfil/actualizar")
                        .sessionAttr("USUARIO", profesorReal)
                        .param("nombre", "Carlos")
                        .param("apellido", "López")
                        .param("fotoBase64", fotoBase64Valida))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/perfil"));

        verify(repositorioUsuario).modificar(any(Profesor.class));
    }

    @Test
    public void queAlActualizarPerfilConFotoBase64InvalidaDevuelveError() throws Exception {
        String fotoBase64Invalida = "imagen-invalida";

        mockMvc.perform(post("/profesor/perfil/actualizar")
                        .sessionAttr("USUARIO", profesorReal)
                        .param("nombre", "Carlos")
                        .param("apellido", "López")
                        .param("fotoBase64", fotoBase64Invalida))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/perfil/editar"));
    }

    @Test
    public void queAlEliminarFotoPerfilEliminaCorrectamente() throws Exception {
        doNothing().when(repositorioUsuario).modificar(any(Profesor.class));

        mockMvc.perform(post("/profesor/perfil/eliminar-foto")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/perfil"));

        verify(repositorioUsuario).buscarPorId(1L);
        verify(repositorioUsuario).modificar(any(Profesor.class));
    }

    @Test
    public void queAlEliminarFotoSinUsuarioEnSesionRedirigeALogin() throws Exception {
        mockMvc.perform(post("/profesor/perfil/eliminar-foto"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void queAlEliminarFotoConAlumnoEnSesionRedirigeAHome() throws Exception {
        mockMvc.perform(post("/profesor/perfil/eliminar-foto")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    public void queAlAgregarExperienciaConDatosValidosAgregaCorrectamente() throws Exception {
        when(servicioExperiencia.guardar(any(ExperienciaEstudiantil.class))).thenReturn(experienciaReal);

        mockMvc.perform(post("/profesor/perfil/agregar-experiencia")
                        .sessionAttr("USUARIO", profesorReal)
                        .param("institucion", "Universidad de Buenos Aires")
                        .param("descripcion", "Maestría en Matemáticas")
                        .param("fecha", "2023-06-15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/perfil"));

        verify(repositorioUsuario).buscarPorId(1L);
        verify(servicioExperiencia).guardar(any(ExperienciaEstudiantil.class));
    }

    @Test
    public void queAlAgregarExperienciaSinUsuarioEnSesionRedirigeALogin() throws Exception {
        mockMvc.perform(post("/profesor/perfil/agregar-experiencia")
                        .param("institucion", "Universidad de Buenos Aires")
                        .param("descripcion", "Maestría en Matemáticas")
                        .param("fecha", "2023-06-15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void queAlAgregarExperienciaConAlumnoEnSesionRedirigeAHome() throws Exception {
        mockMvc.perform(post("/profesor/perfil/agregar-experiencia")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("institucion", "Universidad de Buenos Aires")
                        .param("descripcion", "Maestría en Matemáticas")
                        .param("fecha", "2023-06-15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    public void queAlEliminarExperienciaEliminaCorrectamente() throws Exception {
        doNothing().when(servicioExperiencia).eliminar(1L);

        mockMvc.perform(post("/profesor/perfil/eliminar-experiencia")
                        .sessionAttr("USUARIO", profesorReal)
                        .param("experienciaId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/perfil"));

        verify(servicioExperiencia).eliminar(1L);
    }

    @Test
    public void queAlEliminarExperienciaSinUsuarioEnSesionRedirigeALogin() throws Exception {
        mockMvc.perform(post("/profesor/perfil/eliminar-experiencia")
                        .param("experienciaId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void queAlEliminarExperienciaConAlumnoEnSesionRedirigeAHome() throws Exception {
        mockMvc.perform(post("/profesor/perfil/eliminar-experiencia")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("experienciaId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    public void queAlVerPerfilConFeedbacksVaciosMuestraPromedioEnCero() throws Exception {
        when(servicioFeedback.obtenerFeedbacksPorProfesor(1L)).thenReturn(Arrays.asList());
        when(servicioFeedback.calcularPromedioCalificacion(1L)).thenReturn(null);
        when(servicioFeedback.contarFeedbackPorProfesor(1L)).thenReturn(null);

        MvcResult result = mockMvc.perform(get("/profesor/perfil")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getModel().get("promedioCalificacion"), is(0.0));
        assertThat(modelAndView.getModel().get("totalResenas"), is(0));
    }

    @Test
    public void queAlVerPerfilConExperienciasVaciasMuestraListaVacia() throws Exception {
        when(servicioExperiencia.obtenerExperienciasPorProfesor(1L)).thenReturn(Arrays.asList());

        MvcResult result = mockMvc.perform(get("/profesor/perfil")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getModel().get("experiencias"), is(Arrays.asList()));
    }
}