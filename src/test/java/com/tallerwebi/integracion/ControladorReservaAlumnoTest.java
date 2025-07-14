package com.tallerwebi.integracion;

import com.tallerwebi.dominio.servicios.ServicioReservaAlumno;
import com.tallerwebi.dominio.servicios.ServicioMeet;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Tema;
import com.tallerwebi.dominio.entidades.Clase;
import com.tallerwebi.dominio.entidades.EstadoDisponibilidad;
import com.tallerwebi.presentacion.ControladorReservaAlumno;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
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

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SpringWebTestConfig.class, HibernateTestConfig.class, ControladorReservaAlumnoTest.TestConfig.class})
public class ControladorReservaAlumnoTest {

    private Alumno alumnoReal;
    private Profesor profesorReal;
    private Tema temaReal;
    private List<Clase> disponibilidadesMock;
    private LocalDate fechaInicioSemana;
    private String emailProfesor = "profesor@unlam.com";
    private String emailAlumno = "alumno@unlam.com";

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    @Autowired
    private ServicioReservaAlumno servicioReservaAlumno;

    @Autowired
    private ServicioMeet servicioMeet;

    @Configuration
    static class TestConfig {
        @Bean
        @Primary
        public ServicioReservaAlumno servicioReservaAlumnoMock() {
            return mock(ServicioReservaAlumno.class);
        }

        @Bean
        @Primary
        public ServicioMeet servicioMeetMock() {
            return mock(ServicioMeet.class);
        }
    }

    @BeforeEach
    public void init() {

        alumnoReal = new Alumno();
        alumnoReal.setId(1L);
        alumnoReal.setEmail(emailAlumno);
        alumnoReal.setNombre("Alumno Test");
        alumnoReal.setPassword("password123");
        alumnoReal.setRol("alumno");

        temaReal = new Tema();
        temaReal.setId(1L);
        temaReal.setNombre("Matemáticas");

        profesorReal = new Profesor();
        profesorReal.setId(2L);
        profesorReal.setEmail(emailProfesor);
        profesorReal.setNombre("Profesor Test");
        profesorReal.setPassword("password123");
        profesorReal.setTema(temaReal);
        profesorReal.setRol("profesor");

        fechaInicioSemana = LocalDate.now().with(DayOfWeek.MONDAY);
        Clase disponibilidad1 = new Clase();
        disponibilidad1.setId(1L);
        disponibilidad1.setDiaSemana("Lunes");
        disponibilidad1.setHora("10:00");
        disponibilidad1.setProfesor(profesorReal);
        disponibilidad1.setEstado(EstadoDisponibilidad.DISPONIBLE);
        disponibilidad1.setFechaEspecifica(fechaInicioSemana);

        Clase disponibilidad2 = new Clase();
        disponibilidad2.setId(2L);
        disponibilidad2.setDiaSemana("Miércoles");
        disponibilidad2.setHora("14:00");
        disponibilidad2.setProfesor(profesorReal);
        disponibilidad2.setEstado(EstadoDisponibilidad.RESERVADO);
        disponibilidad2.setFechaEspecifica(fechaInicioSemana.plusDays(2));
        disponibilidad2.setAlumno(alumnoReal);

        disponibilidadesMock = Arrays.asList(disponibilidad1, disponibilidad2);
        Mockito.reset(servicioReservaAlumno, servicioMeet);

        when(servicioReservaAlumno.estaSuscritoAProfesor(eq(alumnoReal.getId()), eq(emailProfesor)))
                .thenReturn(true);

        when(servicioReservaAlumno.obtenerDisponibilidadProfesorPorSemana(eq(emailProfesor), any(LocalDate.class)))
                .thenReturn(disponibilidadesMock);

        when(servicioReservaAlumno.obtenerDisponibilidadPorId(1L))
                .thenReturn(disponibilidad1);

        when(servicioReservaAlumno.obtenerDisponibilidadPorId(2L))
                .thenReturn(disponibilidad2);

        when(servicioReservaAlumno.obtenerClasesPorProfesorYAlumno(emailProfesor, emailAlumno))
                .thenReturn(Arrays.asList(disponibilidad2));

        doNothing().when(servicioReservaAlumno).reservarClasePorId(anyLong(), any(Alumno.class));
        doNothing().when(servicioReservaAlumno).actualizarClase(any(Clase.class));

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void queAlAccederACalendarioReservaSinUsuarioEnSesionRedirigeALogin() throws Exception {
        mockMvc.perform(get("/calendario-reserva")
                        .param("emailProfesor", emailProfesor))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void queAlAccederACalendarioReservaConProfesorRedirigeAHome() throws Exception {
        mockMvc.perform(get("/calendario-reserva")
                        .param("emailProfesor", emailProfesor)
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    public void queAlAccederACalendarioReservaConAlumnoNoSuscritoRedirigeAHome() throws Exception {
        when(servicioReservaAlumno.estaSuscritoAProfesor(eq(alumnoReal.getId()), eq(emailProfesor)))
                .thenReturn(false);

        mockMvc.perform(get("/calendario-reserva")
                        .param("emailProfesor", emailProfesor)
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    public void queAlAccederACalendarioReservaConAlumnoSuscritoMuestraLaPagina() throws Exception {
        MvcResult result = mockMvc.perform(get("/calendario-reserva")
                        .param("emailProfesor", emailProfesor)
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("calendario-reserva"));
        assertThat(modelAndView.getModel().get("nombreUsuario"), is("Alumno Test"));
        assertThat(modelAndView.getModel().get("emailProfesor"), is(emailProfesor));
        assertThat(modelAndView.getModel().get("disponibilidades"), is(disponibilidadesMock));
        assertThat(modelAndView.getModel().get("fechaInicioSemana"), is(notNullValue()));
        assertThat(modelAndView.getModel().get("diasConFecha"), is(notNullValue()));
        assertThat(modelAndView.getModel().get("disponibilidadesKeys"), is(notNullValue()));
        assertThat(modelAndView.getModel().get("estadosMap"), is(notNullValue()));
        assertThat(modelAndView.getModel().get("idsMap"), is(notNullValue()));

        verify(servicioReservaAlumno).estaSuscritoAProfesor(eq(alumnoReal.getId()), eq(emailProfesor));
        verify(servicioReservaAlumno).obtenerDisponibilidadProfesorPorSemana(eq(emailProfesor), any(LocalDate.class));
    }

    @Test
    public void queAlAccederACalendarioReservaConParametroSemanaMuestraLaSemanaCorrecta() throws Exception {
        String fechaSemana = "2025-07-14";

        MvcResult result = mockMvc.perform(get("/calendario-reserva")
                        .param("emailProfesor", emailProfesor)
                        .param("semana", fechaSemana)
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("calendario-reserva"));
        verify(servicioReservaAlumno).obtenerDisponibilidadProfesorPorSemana(eq(emailProfesor), eq(LocalDate.parse(fechaSemana)));
    }

    @Test
    public void queAlAccederACalendarioReservaConFechaInvalidaUsaFechaActual() throws Exception {
        MvcResult result = mockMvc.perform(get("/calendario-reserva")
                        .param("emailProfesor", emailProfesor)
                        .param("semana", "fecha-invalida")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("calendario-reserva"));
        verify(servicioReservaAlumno).obtenerDisponibilidadProfesorPorSemana(eq(emailProfesor), any(LocalDate.class));
    }

    @Test
    public void queAlReservarClaseSinUsuarioEnSesionRedirigeALogin() throws Exception {
        mockMvc.perform(post("/reservar-clase")
                        .param("disponibilidadId", "1")
                        .param("emailProfesor", emailProfesor))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void queAlReservarClaseConProfesorRedirigeAHome() throws Exception {
        mockMvc.perform(post("/reservar-clase")
                        .param("disponibilidadId", "1")
                        .param("emailProfesor", emailProfesor)
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    public void queAlReservarClaseConAlumnoValidoLlamaAlServicio() throws Exception {
        mockMvc.perform(post("/reservar-clase")
                        .param("disponibilidadId", "1")
                        .param("emailProfesor", emailProfesor)
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/calendario-reserva?emailProfesor=" + emailProfesor));

        verify(servicioReservaAlumno).reservarClasePorId(eq(1L), eq(alumnoReal));
    }

    @Test
    public void queAlReservarClaseConSemanaActualMantieneLaSemana() throws Exception {
        String semanaActual = "2025-07-14";

        mockMvc.perform(post("/reservar-clase")
                        .param("disponibilidadId", "1")
                        .param("emailProfesor", emailProfesor)
                        .param("semanaActual", semanaActual)
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/calendario-reserva?emailProfesor=" + emailProfesor + "&semana=" + semanaActual));

        verify(servicioReservaAlumno).reservarClasePorId(eq(1L), eq(alumnoReal));
    }

    @Test
    public void queAlReservarClaseConExcepcionEnServicioRedirigeACalendario() throws Exception {
        doThrow(new RuntimeException("Horario no disponible"))
                .when(servicioReservaAlumno).reservarClasePorId(eq(1L), eq(alumnoReal));

        mockMvc.perform(post("/reservar-clase")
                        .param("disponibilidadId", "1")
                        .param("emailProfesor", emailProfesor)
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/calendario-reserva?emailProfesor=" + emailProfesor));

        verify(servicioReservaAlumno).reservarClasePorId(eq(1L), eq(alumnoReal));
    }

    @Test
    public void queAlObtenerLinkMeetConDisponibilidadValidaDevuelveLink() throws Exception {
        Clase claseConMeet = new Clase();
        claseConMeet.setId(1L);
        claseConMeet.setEnlace_meet("https://meet.jit.si/clase-1");

        when(servicioReservaAlumno.obtenerDisponibilidadPorId(1L))
                .thenReturn(claseConMeet);

        mockMvc.perform(get("/obtener-link-meet")
                        .param("disponibilidadId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("https://meet.jit.si/clase-1"));
    }

    @Test
    public void queAlObtenerLinkMeetConDisponibilidadSinLinkDevuelveNull() throws Exception {
        Clase claseSinMeet = new Clase();
        claseSinMeet.setId(1L);
        claseSinMeet.setEnlace_meet(null);

        when(servicioReservaAlumno.obtenerDisponibilidadPorId(1L))
                .thenReturn(claseSinMeet);

        mockMvc.perform(get("/obtener-link-meet")
                        .param("disponibilidadId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void queAlObtenerLinkMeetConDisponibilidadInexistenteDevuelveNull() throws Exception {
        when(servicioReservaAlumno.obtenerDisponibilidadPorId(999L))
                .thenReturn(null);

        mockMvc.perform(get("/obtener-link-meet")
                        .param("disponibilidadId", "999"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    public void queAlAccederAClasesIntercambioSinUsuarioEnSesionRedirigeALogin() throws Exception {
        mockMvc.perform(get("/clases-intercambio")
                        .param("emailProfesor", emailProfesor)
                        .param("emailAlumno", emailAlumno))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void queAlAccederAClasesIntercambioConUsuarioNoAutorizadoRedirigeAHome() throws Exception {
        Alumno otroAlumno = new Alumno();
        otroAlumno.setId(3L);
        otroAlumno.setEmail("otro@unlam.com");
        otroAlumno.setNombre("Otro Alumno");

        mockMvc.perform(get("/clases-intercambio")
                        .param("emailProfesor", emailProfesor)
                        .param("emailAlumno", emailAlumno)
                        .sessionAttr("USUARIO", otroAlumno))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home?error=No+tienes+permiso+para+ver+estas+clases."));
    }

    @Test
    public void queAlAccederAClasesIntercambioConProfesorAutorizadoMuestraLaPagina() throws Exception {
        MvcResult result = mockMvc.perform(get("/clases-intercambio")
                        .param("emailProfesor", emailProfesor)
                        .param("emailAlumno", emailAlumno)
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("clases-intercambio"));
        assertThat(modelAndView.getModel().get("nombreUsuario"), is("Profesor Test"));
        assertThat(modelAndView.getModel().get("emailProfesor"), is(emailProfesor));
        assertThat(modelAndView.getModel().get("emailAlumno"), is(emailAlumno));
        assertThat(modelAndView.getModel().get("rol"), is("profesor"));
        assertThat(modelAndView.getModel().get("clases"), is(notNullValue()));

        verify(servicioReservaAlumno).obtenerClasesPorProfesorYAlumno(emailProfesor, emailAlumno);
    }

    @Test
    public void queAlAccederAClasesIntercambioConAlumnoAutorizadoMuestraLaPagina() throws Exception {
        MvcResult result = mockMvc.perform(get("/clases-intercambio")
                        .param("emailProfesor", emailProfesor)
                        .param("emailAlumno", emailAlumno)
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("clases-intercambio"));
        assertThat(modelAndView.getModel().get("nombreUsuario"), is("Alumno Test"));
        assertThat(modelAndView.getModel().get("emailProfesor"), is(emailProfesor));
        assertThat(modelAndView.getModel().get("emailAlumno"), is(emailAlumno));
        assertThat(modelAndView.getModel().get("rol"), is("alumno"));

        verify(servicioReservaAlumno).obtenerClasesPorProfesorYAlumno(emailProfesor, emailAlumno);
    }

    @Test
    public void queAlCrearReunionMeetSinUsuarioEnSesionRedirigeALogin() throws Exception {
        mockMvc.perform(post("/clase/1/crear-reunion-meet")
                        .param("emailProfesor", emailProfesor)
                        .param("emailAlumno", emailAlumno))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void queAlCrearReunionMeetConUsuarioNoAutorizadoRedirigeALogin() throws Exception {
        mockMvc.perform(post("/clase/1/crear-reunion-meet")
                        .param("emailProfesor", emailProfesor)
                        .param("emailAlumno", emailAlumno)
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void queAlCrearReunionMeetConProfesorAutorizadoLlamaAlServicio() throws Exception {
        Clase claseSinMeet = new Clase();
        claseSinMeet.setId(1L);
        claseSinMeet.setEnlace_meet(null);

        when(servicioReservaAlumno.obtenerDisponibilidadPorId(1L))
                .thenReturn(claseSinMeet);

        mockMvc.perform(post("/clase/1/crear-reunion-meet")
                        .param("emailProfesor", emailProfesor)
                        .param("emailAlumno", emailAlumno)
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clases-intercambio?emailProfesor=" + emailProfesor + "&emailAlumno=" + emailAlumno));

        verify(servicioReservaAlumno).obtenerDisponibilidadPorId(1L);
        verify(servicioReservaAlumno).actualizarClase(any(Clase.class));
    }

    @Test
    public void queAlCrearReunionMeetConClaseQueYaTieneLinkNoLaActualiza() throws Exception {
        Clase claseConMeet = new Clase();
        claseConMeet.setId(1L);
        claseConMeet.setEnlace_meet("https://meet.jit.si/clase-1");

        when(servicioReservaAlumno.obtenerDisponibilidadPorId(1L))
                .thenReturn(claseConMeet);

        mockMvc.perform(post("/clase/1/crear-reunion-meet")
                        .param("emailProfesor", emailProfesor)
                        .param("emailAlumno", emailAlumno)
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clases-intercambio?emailProfesor=" + emailProfesor + "&emailAlumno=" + emailAlumno));

        verify(servicioReservaAlumno).obtenerDisponibilidadPorId(1L);
        verify(servicioReservaAlumno, never()).actualizarClase(any(Clase.class));
    }

    @Test
    public void queAlCrearReunionMeetConExcepcionEnServicioRedirigeAClasesIntercambio() throws Exception {
        when(servicioReservaAlumno.obtenerDisponibilidadPorId(1L))
                .thenThrow(new RuntimeException("Error de base de datos"));

        mockMvc.perform(post("/clase/1/crear-reunion-meet")
                        .param("emailProfesor", emailProfesor)
                        .param("emailAlumno", emailAlumno)
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clases-intercambio?emailProfesor=" + emailProfesor + "&emailAlumno=" + emailAlumno));

        verify(servicioReservaAlumno).obtenerDisponibilidadPorId(1L);
        verify(servicioReservaAlumno, never()).actualizarClase(any(Clase.class));
    }

    @Test
    public void queAlCrearReunionMeetConClaseNulaNoLaActualiza() throws Exception {
        when(servicioReservaAlumno.obtenerDisponibilidadPorId(1L))
                .thenReturn(null);

        mockMvc.perform(post("/clase/1/crear-reunion-meet")
                        .param("emailProfesor", emailProfesor)
                        .param("emailAlumno", emailAlumno)
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clases-intercambio?emailProfesor=" + emailProfesor + "&emailAlumno=" + emailAlumno));

        verify(servicioReservaAlumno).obtenerDisponibilidadPorId(1L);
        verify(servicioReservaAlumno, never()).actualizarClase(any(Clase.class));
    }
}