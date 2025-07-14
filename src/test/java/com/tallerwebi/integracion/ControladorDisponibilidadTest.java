package com.tallerwebi.integracion;

import com.tallerwebi.dominio.servicios.ServicioDisponibilidadProfesor;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Tema;
import com.tallerwebi.dominio.entidades.Clase;
import com.tallerwebi.dominio.entidades.EstadoDisponibilidad;
import com.tallerwebi.presentacion.ControladorDisponibilidad;

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
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SpringWebTestConfig.class, HibernateTestConfig.class, ControladorDisponibilidadTest.TestConfig.class})
public class ControladorDisponibilidadTest {


    private Alumno alumnoReal;
    private Profesor profesorReal;
    private Tema temaReal;
    private List<Clase> disponibilidadesMock;
    private LocalDate fechaInicioSemana;

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    @Autowired
    private ServicioDisponibilidadProfesor servicioDisponibilidadProfesor;

    @Configuration
    static class TestConfig {
        @Bean
        @Primary
        public ServicioDisponibilidadProfesor servicioDisponibilidadProfesorMock() {
            return mock(ServicioDisponibilidadProfesor.class);
        }
    }

    @BeforeEach
    public void init() {

        alumnoReal = new Alumno();
        alumnoReal.setId(1L);
        alumnoReal.setEmail("alumno@unlam.com");
        alumnoReal.setNombre("Alumno Test");
        alumnoReal.setPassword("password123");

        temaReal = new Tema();
        temaReal.setId(1L);
        temaReal.setNombre("Matemáticas");

        profesorReal = new Profesor();
        profesorReal.setId(2L);
        profesorReal.setEmail("profesor@unlam.com");
        profesorReal.setNombre("Profesor Test");
        profesorReal.setPassword("password123");
        profesorReal.setTema(temaReal);
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
        disponibilidad2.setEstado(EstadoDisponibilidad.OCUPADO);
        disponibilidad2.setFechaEspecifica(fechaInicioSemana.plusDays(2));

        disponibilidadesMock = Arrays.asList(disponibilidad1, disponibilidad2);

        Mockito.reset(servicioDisponibilidadProfesor);
        when(servicioDisponibilidadProfesor.obtenerDisponibilidadProfesorPorSemana(eq(profesorReal), any(LocalDate.class)))
                .thenReturn(disponibilidadesMock);
        doNothing().when(servicioDisponibilidadProfesor).toggleDisponibilidadConFecha(
                eq(profesorReal), anyString(), anyString(), any(LocalDate.class));
        doNothing().when(servicioDisponibilidadProfesor).reservarHorario(
                eq(profesorReal), anyString(), anyString());
        doNothing().when(servicioDisponibilidadProfesor).desagendarHorario(
                eq(profesorReal), anyString(), anyString());

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void queAlAccederACalendarioProfesorSinUsuarioEnSesionRedirigeALogin() throws Exception {
        mockMvc.perform(get("/calendario-profesor"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void queAlAccederACalendarioProfesorConAlumnoRedirigeAHome() throws Exception {
        mockMvc.perform(get("/calendario-profesor")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    public void queAlAccederACalendarioProfesorConProfesorMuestraLaPagina() throws Exception {
        MvcResult result = mockMvc.perform(get("/calendario-profesor")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("calendario-profesor"));
        assertThat(modelAndView.getModel().get("nombreUsuario"), is("Profesor Test"));
        assertThat(modelAndView.getModel().get("emailProfesor"), is("profesor@unlam.com"));
        assertThat(modelAndView.getModel().get("disponibilidades"), is(disponibilidadesMock));
        assertThat(modelAndView.getModel().get("fechaInicioSemana"), is(notNullValue()));
        assertThat(modelAndView.getModel().get("diasConFecha"), is(notNullValue()));
        assertThat(modelAndView.getModel().get("horasHabilitadasMap"), is(notNullValue()));

        verify(servicioDisponibilidadProfesor).obtenerDisponibilidadProfesorPorSemana(eq(profesorReal), any(LocalDate.class));
    }

    @Test
    public void queAlAccederACalendarioProfesorConParametroSemanaMuestraLaSemanaCorrecta() throws Exception {
        String fechaSemana = "2025-07-14";

        MvcResult result = mockMvc.perform(get("/calendario-profesor")
                        .param("semana", fechaSemana)
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("calendario-profesor"));
        verify(servicioDisponibilidadProfesor).obtenerDisponibilidadProfesorPorSemana(eq(profesorReal), eq(LocalDate.parse(fechaSemana)));
    }

    @Test
    public void queAlToggleDisponibilidadSinUsuarioEnSesionRedirigeALogin() throws Exception {
        mockMvc.perform(post("/toggle-disponibilidad")
                        .param("diaSemana", "Lunes")
                        .param("hora", "10:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void queAlToggleDisponibilidadConAlumnoRedirigeAHome() throws Exception {
        mockMvc.perform(post("/toggle-disponibilidad")
                        .param("diaSemana", "Lunes")
                        .param("hora", "10:00")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    public void queAlToggleDisponibilidadConParametrosValidosLlamaAlServicio() throws Exception {
        mockMvc.perform(post("/toggle-disponibilidad")
                        .param("diaSemana", "Lunes")
                        .param("hora", "10:00")
                        .param("fechaEspecifica", fechaInicioSemana.toString())
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/calendario-profesor"));

        verify(servicioDisponibilidadProfesor).toggleDisponibilidadConFecha(
                eq(profesorReal), eq("Lunes"), eq("10:00"), eq(fechaInicioSemana));
    }

    @Test
    public void queAlToggleDisponibilidadConParametrosInvalidosRedirigeACalendario() throws Exception {
        mockMvc.perform(post("/toggle-disponibilidad")
                        .param("diaSemana", "")
                        .param("hora", "10:00")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/calendario-profesor"));

        verify(servicioDisponibilidadProfesor, never()).toggleDisponibilidadConFecha(
                any(Profesor.class), anyString(), anyString(), any(LocalDate.class));
    }

    @Test
    public void queAlToggleDisponibilidadConHoraInvalidaRedirigeACalendario() throws Exception {
        mockMvc.perform(post("/toggle-disponibilidad")
                        .param("diaSemana", "Lunes")
                        .param("hora", "25:00")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/calendario-profesor"));

        verify(servicioDisponibilidadProfesor, never()).toggleDisponibilidadConFecha(
                any(Profesor.class), anyString(), anyString(), any(LocalDate.class));
    }

    @Test
    public void queAlToggleDisponibilidadConDiaInvalidoRedirigeACalendario() throws Exception {
        mockMvc.perform(post("/toggle-disponibilidad")
                        .param("diaSemana", "InvalidDay")
                        .param("hora", "10:00")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/calendario-profesor"));

        verify(servicioDisponibilidadProfesor, never()).toggleDisponibilidadConFecha(
                any(Profesor.class), anyString(), anyString(), any(LocalDate.class));
    }

    @Test
    public void queAlToggleDisponibilidadConSemanaActualMantieneLaSemana() throws Exception {
        String semanaActual = "2025-07-14";

        mockMvc.perform(post("/toggle-disponibilidad")
                        .param("diaSemana", "Lunes")
                        .param("hora", "10:00")
                        .param("semanaActual", semanaActual)
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/calendario-profesor?semana=" + semanaActual));

        verify(servicioDisponibilidadProfesor).toggleDisponibilidadConFecha(
                eq(profesorReal), eq("Lunes"), eq("10:00"), any(LocalDate.class));
    }

    @Test
    public void queAlReservarHorarioSinUsuarioEnSesionRedirigeALogin() throws Exception {
        mockMvc.perform(post("/reservar-horario")
                        .param("diaSemana", "Lunes")
                        .param("hora", "10:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void queAlReservarHorarioConAlumnoRedirigeAHome() throws Exception {
        mockMvc.perform(post("/reservar-horario")
                        .param("diaSemana", "Lunes")
                        .param("hora", "10:00")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    public void queAlReservarHorarioConParametrosValidosLlamaAlServicio() throws Exception {
        mockMvc.perform(post("/reservar-horario")
                        .param("diaSemana", "Lunes")
                        .param("hora", "10:00")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/calendario-profesor"));

        verify(servicioDisponibilidadProfesor).reservarHorario(profesorReal, "Lunes", "10:00");
    }

    @Test
    public void queAlReservarHorarioConParametrosInvalidosNoLlamaAlServicio() throws Exception {
        mockMvc.perform(post("/reservar-horario")
                        .param("diaSemana", "")
                        .param("hora", "10:00")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/calendario-profesor"));

        verify(servicioDisponibilidadProfesor, never()).reservarHorario(
                any(Profesor.class), anyString(), anyString());
    }

    @Test
    public void queAlDesagendarHorarioSinUsuarioEnSesionRedirigeALogin() throws Exception {
        mockMvc.perform(post("/desagendar-horario")
                        .param("diaSemana", "Lunes")
                        .param("hora", "10:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void queAlDesagendarHorarioConAlumnoRedirigeAHome() throws Exception {
        mockMvc.perform(post("/desagendar-horario")
                        .param("diaSemana", "Lunes")
                        .param("hora", "10:00")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    public void queAlDesagendarHorarioConParametrosValidosLlamaAlServicio() throws Exception {
        mockMvc.perform(post("/desagendar-horario")
                        .param("diaSemana", "Lunes")
                        .param("hora", "10:00")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/calendario-profesor"));

        verify(servicioDisponibilidadProfesor).desagendarHorario(profesorReal, "Lunes", "10:00");
    }

    @Test
    public void queAlDesagendarHorarioConParametrosInvalidosNoLlamaAlServicio() throws Exception {
        mockMvc.perform(post("/desagendar-horario")
                        .param("diaSemana", "Lunes")
                        .param("hora", "")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/calendario-profesor"));

        verify(servicioDisponibilidadProfesor, never()).desagendarHorario(
                any(Profesor.class), anyString(), anyString());
    }

    @Test
    public void queAlDesagendarHorarioConSemanaActualMantieneLaSemana() throws Exception {
        String semanaActual = "2025-07-14";

        mockMvc.perform(post("/desagendar-horario")
                        .param("diaSemana", "Lunes")
                        .param("hora", "10:00")
                        .param("semanaActual", semanaActual)
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/calendario-profesor?semana=" + semanaActual));

        verify(servicioDisponibilidadProfesor).desagendarHorario(profesorReal, "Lunes", "10:00");
    }


    @Test
    public void queAlToggleDisponibilidadConExcepcionEnServicioRedirigeACalendario() throws Exception {
        doThrow(new RuntimeException("Error de base de datos"))
                .when(servicioDisponibilidadProfesor).toggleDisponibilidadConFecha(
                        eq(profesorReal), eq("Lunes"), eq("10:00"), any(LocalDate.class));

        mockMvc.perform(post("/toggle-disponibilidad")
                        .param("diaSemana", "Lunes")
                        .param("hora", "10:00")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/calendario-profesor"));

        verify(servicioDisponibilidadProfesor).toggleDisponibilidadConFecha(
                eq(profesorReal), eq("Lunes"), eq("10:00"), any(LocalDate.class));
    }

    @Test
    public void queAlAccederACalendarioProfesorConFechaEspecificaInvalidaUsaFechaActual() throws Exception {
        MvcResult result = mockMvc.perform(get("/calendario-profesor")
                        .param("semana", "invalid-date")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("calendario-profesor"));
        verify(servicioDisponibilidadProfesor).obtenerDisponibilidadProfesorPorSemana(eq(profesorReal), any(LocalDate.class));
    }
}