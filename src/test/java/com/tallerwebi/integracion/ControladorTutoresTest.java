package com.tallerwebi.integracion;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.servicios.*;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.presentacion.ControladorTutores;
import com.tallerwebi.presentacion.DatosLogin;

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
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SpringWebTestConfig.class, HibernateTestConfig.class, ControladorTutoresTest.TestConfig.class})
public class ControladorTutoresTest {

    private Alumno alumnoReal;
    private Profesor profesorReal;
    private Profesor profesor2Real;
    private Tema temaMatematicas;
    private Tema temaFisica;
    private List<Usuario> profesoresMock;
    private List<Tema> temasMock;
    private ExperienciaEstudiantil experienciaMock;
    private FeedbackProfesor feedbackMock;

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    @Autowired
    private RepositorioUsuario repositorioUsuario;
    @Autowired
    private ServicioFeedback servicioFeedback;
    @Autowired
    private ServicioExperiencia servicioExperiencia;
    @Autowired
    private ServicioLogin servicioLogin;
    @Autowired
    private ServicioTema servicioTema;
    @Autowired
    private ServicioSuscripcion servicioSuscripcion;

    @Configuration
    static class TestConfig {
        @Bean
        @Primary
        public RepositorioUsuario repositorioUsuarioMock() {
            return mock(RepositorioUsuario.class);
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
        public ServicioLogin servicioLoginMock() {
            return mock(ServicioLogin.class);
        }

        @Bean
        @Primary
        public ServicioTema servicioTemaMock() {
            return mock(ServicioTema.class);
        }

        @Bean
        @Primary
        public ServicioSuscripcion servicioSuscripcionMock() {
            return mock(ServicioSuscripcion.class);
        }
    }

    @BeforeEach
    public void init() {
        alumnoReal = new Alumno();
        alumnoReal.setId(1L);
        alumnoReal.setEmail("alumno@unlam.com");
        alumnoReal.setNombre("Juan");
        alumnoReal.setApellido("Pérez");
        alumnoReal.setPassword("password123");

        temaMatematicas = new Tema();
        temaMatematicas.setId(1L);
        temaMatematicas.setNombre("Matemáticas");

        temaFisica = new Tema();
        temaFisica.setId(2L);
        temaFisica.setNombre("Física");

        profesorReal = new Profesor();
        profesorReal.setId(2L);
        profesorReal.setEmail("profesor@unlam.com");
        profesorReal.setNombre("Carlos");
        profesorReal.setApellido("López");
        profesorReal.setPassword("password123");
        profesorReal.setTema(temaMatematicas);
        profesorReal.setModalidadPreferida(ModalidadPreferida.PRESENCIAL);

        profesor2Real = new Profesor();
        profesor2Real.setId(3L);
        profesor2Real.setEmail("profesor2@unlam.com");
        profesor2Real.setNombre("María");
        profesor2Real.setApellido("González");
        profesor2Real.setPassword("password123");
        profesor2Real.setTema(temaFisica);
        profesor2Real.setModalidadPreferida(ModalidadPreferida.VIRTUAL);

        profesoresMock = Arrays.asList(profesorReal, profesor2Real);
        temasMock = Arrays.asList(temaMatematicas, temaFisica);

        experienciaMock = new ExperienciaEstudiantil();
        experienciaMock.setId(1L);
        experienciaMock.setProfesor(profesorReal);
        experienciaMock.setInstitucion("Universidad Nacional");
        experienciaMock.setDescripcion("Tecnitura en desarrollo web");
        experienciaMock.setFecha("2022-2025");

        feedbackMock = new FeedbackProfesor();
        feedbackMock.setId(1L);
        feedbackMock.setProfesor(profesorReal);
        feedbackMock.setAlumno(alumnoReal);
        feedbackMock.setCalificacion(5);
        feedbackMock.setComentario("Excelente profesor");
        feedbackMock.setFechaCreacion(LocalDateTime.now());

        reset(repositorioUsuario, servicioFeedback, servicioExperiencia,
                servicioLogin, servicioTema, servicioSuscripcion);

        when(servicioLogin.obtenerProfesores()).thenReturn(profesoresMock);
        when(servicioTema.obtenerTodosLosTemas()).thenReturn(temasMock);
        when(repositorioUsuario.buscar("profesor@unlam.com")).thenReturn(profesorReal);
        when(repositorioUsuario.buscarPorId(2L)).thenReturn(profesorReal);
        when(servicioExperiencia.obtenerExperienciasPorProfesor(2L)).thenReturn(Arrays.asList(experienciaMock));
        when(servicioFeedback.obtenerFeedbacksPorProfesor(2L)).thenReturn(Arrays.asList(feedbackMock));
        when(servicioFeedback.calcularPromedioCalificacion(2L)).thenReturn(5.0);
        when(servicioFeedback.contarFeedbackPorProfesor(2L)).thenReturn(1);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void queAlVerTutoresMuestraLaListaCompleta() throws Exception {
        MvcResult result = mockMvc.perform(get("/verTutores"))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("verTutores"));
        assertThat(modelAndView.getModel().get("listaProfesores"), is(profesoresMock));
        assertThat(modelAndView.getModel().get("listaTemas"), is(temasMock));

        verify(servicioLogin).obtenerProfesores();
        verify(servicioTema).obtenerTodosLosTemas();
    }

    @Test
    public void queAlVerTutoresConFiltroTemaFiltraCorrectamente() throws Exception {
        MvcResult result = mockMvc.perform(get("/verTutores")
                        .param("temaId", "1"))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("verTutores"));
        assertThat(modelAndView.getModel().get("temaIdSeleccionado"), is(1L));

        @SuppressWarnings("unchecked")
        List<Usuario> profesoresFiltrados = (List<Usuario>) modelAndView.getModel().get("listaProfesores");
        assertThat(profesoresFiltrados, hasSize(1));
        assertThat(profesoresFiltrados.get(0), is(profesorReal));
    }

    @Test
    public void queAlVerTutoresConFiltroModalidadFiltraCorrectamente() throws Exception {
        MvcResult result = mockMvc.perform(get("/verTutores")
                        .param("modalidad", "PRESENCIAL"))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("verTutores"));
        assertThat(modelAndView.getModel().get("modalidadSeleccionada"), is(ModalidadPreferida.PRESENCIAL));

        @SuppressWarnings("unchecked")
        List<Usuario> profesoresFiltrados = (List<Usuario>) modelAndView.getModel().get("listaProfesores");
        assertThat(profesoresFiltrados, hasSize(1));
        assertThat(profesoresFiltrados.get(0), is(profesorReal));
    }

    @Test
    public void queAlVerTutoresConBusquedaFiltraCorrectamente() throws Exception {
        MvcResult result = mockMvc.perform(get("/verTutores")
                        .param("query", "Carlos"))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("verTutores"));
        assertThat(modelAndView.getModel().get("query"), is("Carlos"));

        @SuppressWarnings("unchecked")
        List<Usuario> profesoresFiltrados = (List<Usuario>) modelAndView.getModel().get("listaProfesores");
        assertThat(profesoresFiltrados, hasSize(1));
        assertThat(profesoresFiltrados.get(0), is(profesorReal));
    }

    @Test
    public void queAlVerTutoresConUsuarioLogueadoMuestraInformacionDelUsuario() throws Exception {
        MvcResult result = mockMvc.perform(get("/verTutores")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getModel().get("nombreUsuario"), is("Juan"));
        assertThat(modelAndView.getModel().get("rol"), is("ALUMNO"));
    }

    @Test
    public void queAlVerPerfilDeProfesorMuestraElPerfilCompleto() throws Exception {
        MvcResult result = mockMvc.perform(get("/verPerfilDeProfesor")
                        .param("email", "profesor@unlam.com")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("verPerfilDeProfesor"));
        assertThat(modelAndView.getModel().get("profesor"), is(profesorReal));
        assertThat(modelAndView.getModel().get("experiencias"), is(Arrays.asList(experienciaMock)));
        assertThat(modelAndView.getModel().get("feedbacks"), is(Arrays.asList(feedbackMock)));
        assertThat(modelAndView.getModel().get("promedioCalificacion"), is(5.0));
        assertThat(modelAndView.getModel().get("totalResenas"), is(1));
        assertThat(modelAndView.getModel().get("editandoFeedback"), is(false));

        verify(repositorioUsuario).buscar("profesor@unlam.com");
        verify(servicioExperiencia).obtenerExperienciasPorProfesor(2L);
        verify(servicioFeedback).obtenerFeedbacksPorProfesor(2L);
        verify(servicioFeedback).calcularPromedioCalificacion(2L);
        verify(servicioFeedback).contarFeedbackPorProfesor(2L);
    }

    @Test
    public void queAlVerPerfilDeProfesorInexistenteRedirigeATutores() throws Exception {
        when(repositorioUsuario.buscar("inexistente@unlam.com")).thenReturn(null);

        mockMvc.perform(get("/verPerfilDeProfesor")
                        .param("email", "inexistente@unlam.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/verTutores"));
    }

    @Test
    public void queAlSuscribirseAProfesorConUsuarioValidoSeaSuscribeCorrectamente() throws Exception {
        when(servicioSuscripcion.suscribirAlumnoAProfesor(1L, 2L)).thenReturn(true);

        MvcResult result = mockMvc.perform(post("/suscribirseAProfesor")
                        .param("profesorId", "2")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("verPerfilDeProfesor"));
        assertThat(modelAndView.getModel().get("mensaje"), is("Te has suscrito exitosamente al profesor"));

        verify(servicioSuscripcion).suscribirAlumnoAProfesor(1L, 2L);
        verify(repositorioUsuario).buscarPorId(2L);
    }

    @Test
    public void queAlSuscribirseAProfesorSinUsuarioLogueadoRedirigeALogin() throws Exception {
        MvcResult result = mockMvc.perform(post("/suscribirseAProfesor")
                        .param("profesorId", "2"))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("login"));
        assertThat(modelAndView.getModel().get("error"), is("Debes iniciar sesión para suscribirte a un profesor"));
        assertThat(modelAndView.getModel().get("datosLogin"), is(instanceOf(DatosLogin.class)));
    }

    @Test
    public void queAlDesuscribirseDeProfesorConUsuarioValidoSeDesuscribeCorrectamente() throws Exception {
        when(servicioSuscripcion.desuscribirAlumnoDeProfesor(1L, 2L)).thenReturn(true);

        MvcResult result = mockMvc.perform(post("/desuscribirseDeProfesor")
                        .param("profesorId", "2")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("verPerfilDeProfesor"));
        assertThat(modelAndView.getModel().get("mensaje"), is("Te has desuscrito exitosamente del profesor"));

        verify(servicioSuscripcion).desuscribirAlumnoDeProfesor(1L, 2L);
        verify(repositorioUsuario).buscarPorId(2L);
    }


    @Test
    public void queAlDejarFeedbackYaExistenteDevuelveError() throws Exception {
        when(servicioFeedback.alumnoYaDejoFeedback(1L, 2L)).thenReturn(true);

        MvcResult result = mockMvc.perform(post("/dejarFeedback")
                        .param("profesorId", "2")
                        .param("calificacion", "5")
                        .param("comentario", "Excelente profesor")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("verPerfilDeProfesor"));
        assertThat(modelAndView.getModel().get("error"), is("Ya le dejaste feedback para este profesor."));

        verify(servicioFeedback, times(2)).alumnoYaDejoFeedback(1L, 2L);
        verify(servicioFeedback, never()).guardar(any(FeedbackProfesor.class));
    }

    @Test
    public void queAlDejarFeedbackConDatosValidosSeGuardaCorrectamente() throws Exception {
        when(servicioFeedback.alumnoYaDejoFeedback(1L, 2L)).thenReturn(false);
        when(servicioSuscripcion.estaAlumnoSuscritoAProfesor(1L, 2L)).thenReturn(true);

        MvcResult result = mockMvc.perform(post("/dejarFeedback")
                        .param("profesorId", "2")
                        .param("calificacion", "5")
                        .param("comentario", "Excelente profesor")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("verPerfilDeProfesor"));
        assertThat(modelAndView.getModel().get("mensaje"), is("Feedback enviado exitosamente"));

        verify(servicioFeedback, times(2)).alumnoYaDejoFeedback(1L, 2L);
        verify(servicioSuscripcion, times(2)).estaAlumnoSuscritoAProfesor(1L, 2L);
        verify(servicioFeedback).guardar(any(FeedbackProfesor.class));
    }

    @Test
    public void queAlDejarFeedbackSinSuscripcionDevuelveError() throws Exception {
        when(servicioFeedback.alumnoYaDejoFeedback(1L, 2L)).thenReturn(false);
        when(servicioSuscripcion.estaAlumnoSuscritoAProfesor(1L, 2L)).thenReturn(false);

        MvcResult result = mockMvc.perform(post("/dejarFeedback")
                        .param("profesorId", "2")
                        .param("calificacion", "5")
                        .param("comentario", "Excelente profesor")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("verPerfilDeProfesor"));
        assertThat(modelAndView.getModel().get("error"), is("Tenes que estar suscrito al profesor para poder dejar una reseña"));

        verify(servicioFeedback, never()).guardar(any(FeedbackProfesor.class));
    }

    @Test
    public void queAlDejarFeedbackConCalificacionInvalidaDevuelveError() throws Exception {
        when(servicioFeedback.alumnoYaDejoFeedback(1L, 2L)).thenReturn(false);
        when(servicioSuscripcion.estaAlumnoSuscritoAProfesor(1L, 2L)).thenReturn(true);

        MvcResult result = mockMvc.perform(post("/dejarFeedback")
                        .param("profesorId", "2")
                        .param("calificacion", "6")
                        .param("comentario", "Excelente profesor")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("verPerfilDeProfesor"));
        assertThat(modelAndView.getModel().get("error"), is("La calificación debe estar entre 1 y 5 estrellas"));

        verify(servicioFeedback, never()).guardar(any(FeedbackProfesor.class));
    }

    @Test
    public void queAlEditarFeedbackConFeedbackExistenteActivaModoEdicion() throws Exception {
        when(servicioFeedback.buscarFeedbackDeAlumnoParaProfesor(1L, 2L)).thenReturn(feedbackMock);

        MvcResult result = mockMvc.perform(post("/editarFeedback")
                        .param("profesorId", "2")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("verPerfilDeProfesor"));
        assertThat(modelAndView.getModel().get("editandoFeedback"), is(true));

        verify(servicioFeedback).buscarFeedbackDeAlumnoParaProfesor(1L, 2L);
    }

    @Test
    public void queAlEditarFeedbackSinFeedbackExistenteDevuelveError() throws Exception {
        when(servicioFeedback.buscarFeedbackDeAlumnoParaProfesor(1L, 2L)).thenReturn(null);

        MvcResult result = mockMvc.perform(post("/editarFeedback")
                        .param("profesorId", "2")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("verPerfilDeProfesor"));
        assertThat(modelAndView.getModel().get("error"), is("No tenes feedback para editar"));
        assertThat(modelAndView.getModel().get("editandoFeedback"), is(false));
    }

    @Test
    public void queAlActualizarFeedbackConDatosValidosSeActualiza() throws Exception {
        when(servicioFeedback.obtenerPorId(1L)).thenReturn(feedbackMock);

        MvcResult result = mockMvc.perform(post("/actualizarFeedback")
                        .param("profesorId", "2")
                        .param("feedbackId", "1")
                        .param("calificacion", "4")
                        .param("comentario", "Muy buen profesor")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("verPerfilDeProfesor"));
        assertThat(modelAndView.getModel().get("mensaje"), is("Feedback actualizado exitosamente"));
        assertThat(modelAndView.getModel().get("editandoFeedback"), is(false));

        verify(servicioFeedback).obtenerPorId(1L);
        verify(servicioFeedback).guardar(feedbackMock);
    }

    @Test
    public void queAlCancelarEdicionFeedbackDesactivaModoEdicion() throws Exception {
        MvcResult result = mockMvc.perform(post("/cancelarEdicionFeedback")
                        .param("profesorId", "2")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("verPerfilDeProfesor"));
        assertThat(modelAndView.getModel().get("editandoFeedback"), is(false));
    }

    @Test
    public void queAlBorrarFeedbackConFeedbackValidoSeElimina() throws Exception {
        when(servicioFeedback.obtenerPorId(1L)).thenReturn(feedbackMock);

        MvcResult result = mockMvc.perform(post("/borrarFeedback")
                        .param("profesorId", "2")
                        .param("feedbackId", "1")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("verPerfilDeProfesor"));
        assertThat(modelAndView.getModel().get("mensaje"), is("Feedback eliminado exitosamente"));

        verify(servicioFeedback).obtenerPorId(1L);
        verify(servicioFeedback).eliminar(1L);
    }

    @Test
    public void queAlBorrarFeedbackInexistenteDevuelveError() throws Exception {
        when(servicioFeedback.obtenerPorId(1L)).thenReturn(null);

        MvcResult result = mockMvc.perform(post("/borrarFeedback")
                        .param("profesorId", "2")
                        .param("feedbackId", "1")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("verPerfilDeProfesor"));
        assertThat(modelAndView.getModel().get("error"), is("Feedback no encontrado"));

        verify(servicioFeedback).obtenerPorId(1L);
        verify(servicioFeedback, never()).eliminar(1L);
    }

    @Test
    public void queAlBorrarFeedbackDeOtroUsuarioDevuelveError() throws Exception {
        Alumno otroAlumno = new Alumno();
        otroAlumno.setId(99L);
        feedbackMock.setAlumno(otroAlumno);

        when(servicioFeedback.obtenerPorId(1L)).thenReturn(feedbackMock);

        MvcResult result = mockMvc.perform(post("/borrarFeedback")
                        .param("profesorId", "2")
                        .param("feedbackId", "1")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("verPerfilDeProfesor"));
        assertThat(modelAndView.getModel().get("error"), is("No tenes permisos para borrar este feedback"));

        verify(servicioFeedback, never()).eliminar(1L);
    }

    @Test
    public void queAlAccederSinUsuarioLogueadoEnAccionesCriticasRedirigeALogin() throws Exception {
        MvcResult result = mockMvc.perform(post("/dejarFeedback")
                        .param("profesorId", "2")
                        .param("calificacion", "5")
                        .param("comentario", "Excelente profesor"))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("login"));
        assertThat(modelAndView.getModel().get("error"), is("Debes iniciar sesión para dejar feedback"));
        assertThat(modelAndView.getModel().get("datosLogin"), is(instanceOf(DatosLogin.class)));
    }
}