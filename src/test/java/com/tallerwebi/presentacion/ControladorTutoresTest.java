package com.tallerwebi.presentacion;
import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.dominio.servicios.*;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

    public class ControladorTutoresTest {

        private ControladorTutores controladorTutores;
        private RepositorioUsuario repositorioUsuarioMock;
        private ServicioFeedback servicioFeedbackMock;
        private ServicioExperiencia servicioExperienciaMock;
        private ServicioLogin servicioLoginMock;
        private ServicioTema servicioTemaMock;
        private ServicioSuscripcion servicioSuscripcionMock;

        private HttpServletRequest requestMock;
        private HttpSession sessionMock;
        private Alumno alumnoMock;
        private Profesor profesorMock;
        private Tema temaMock;
        private FeedbackProfesor feedbackMock;
        private ExperienciaEstudiantil experienciaMock;

        @BeforeEach
        public void init() {
            repositorioUsuarioMock = mock(RepositorioUsuario.class);
            servicioFeedbackMock = mock(ServicioFeedback.class);
            servicioExperienciaMock = mock(ServicioExperiencia.class);
            servicioLoginMock = mock(ServicioLogin.class);
            servicioTemaMock = mock(ServicioTema.class);
            servicioSuscripcionMock = mock(ServicioSuscripcion.class);

            controladorTutores = new ControladorTutores(
                    repositorioUsuarioMock,
                    servicioFeedbackMock,
                    servicioExperienciaMock,
                    servicioLoginMock,
                    servicioTemaMock,
                    servicioSuscripcionMock
            );

            requestMock = mock(HttpServletRequest.class);
            sessionMock = mock(HttpSession.class);

            alumnoMock = mock(Alumno.class);
            when(alumnoMock.getId()).thenReturn(1L);
            when(alumnoMock.getNombre()).thenReturn("Juan");
            when(alumnoMock.getEmail()).thenReturn("alumno@unlam.com");

            profesorMock = mock(Profesor.class);
            when(profesorMock.getId()).thenReturn(2L);
            when(profesorMock.getNombre()).thenReturn("Carlos");
            when(profesorMock.getEmail()).thenReturn("profesor@unlam.com");

            temaMock = mock(Tema.class);
            when(temaMock.getId()).thenReturn(1L);
            when(temaMock.getNombre()).thenReturn("Matemáticas");
            when(profesorMock.getTema()).thenReturn(temaMock);

            feedbackMock = mock(FeedbackProfesor.class);
            experienciaMock = mock(ExperienciaEstudiantil.class);
        }

        @Test
        public void verTutoresConUsuarioLogueadoDeberiaRetornarListaDeProfesores() {

            when(requestMock.getSession()).thenReturn(sessionMock);
            when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);

            List<Usuario> profesores = Arrays.asList(profesorMock);
            when(servicioLoginMock.obtenerProfesores()).thenReturn(profesores);

            ModelAndView modelAndView = controladorTutores.verTutores(requestMock);

            assertThat(modelAndView.getViewName(), equalToIgnoringCase("verTutores"));
            assertEquals(modelAndView.getModel().get("listaProfesores"), profesores);
            assertEquals(modelAndView.getModel().get("nombreUsuario"), "Juan");
            verify(servicioLoginMock, times(1)).obtenerProfesores();
        }

        @Test
        public void verTutoresSinUsuarioLogueadoDeberiaRetornarListaDeProfesoresConNombreNull() {

            when(requestMock.getSession()).thenReturn(sessionMock);
            when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

            List<Usuario> profesores = Arrays.asList(profesorMock);
            when(servicioLoginMock.obtenerProfesores()).thenReturn(profesores);
            ModelAndView modelAndView = controladorTutores.verTutores(requestMock);
            assertThat(modelAndView.getViewName(), equalToIgnoringCase("verTutores"));
            assertEquals(modelAndView.getModel().get("listaProfesores"), profesores);
            assertEquals(modelAndView.getModel().get("nombreUsuario"), null);
        }

        @Test
        public void verTutoresConExcepcionDeberiaRetornarVistaConError() {

            when(requestMock.getSession()).thenReturn(sessionMock);
            when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
            when(servicioLoginMock.obtenerProfesores()).thenThrow(new RuntimeException("Error de base de datos"));
            ModelAndView modelAndView = controladorTutores.verTutores(requestMock);
            assertThat(modelAndView.getViewName(), equalToIgnoringCase("verTutores"));
            assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("Error al cargar los tutores"));
        }

        @Test
        public void verPerfilDeProfesorConEmailValidoDeberiaRetornarPerfilCompleto() {

            String email = "profesor@unlam.com";
            when(requestMock.getSession()).thenReturn(sessionMock);
            when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
            when(repositorioUsuarioMock.buscar(email)).thenReturn(profesorMock);

            List<ExperienciaEstudiantil> experiencias = Arrays.asList(experienciaMock);
            List<FeedbackProfesor> feedbacks = Arrays.asList(feedbackMock);

            when(servicioExperienciaMock.obtenerExperienciasPorProfesor(2L)).thenReturn(experiencias);
            when(servicioFeedbackMock.obtenerFeedbacksPorProfesor(2L)).thenReturn(feedbacks);
            when(servicioFeedbackMock.calcularPromedioCalificacion(2L)).thenReturn(4.5);
            when(servicioFeedbackMock.contarFeedbackPorProfesor(2L)).thenReturn(10);
            when(alumnoMock.estaSuscritoAProfesor(profesorMock)).thenReturn(false);
            ModelAndView modelAndView = controladorTutores.verPerfilDeProfesor(email, requestMock);
            assertThat(modelAndView.getViewName(), equalToIgnoringCase("verPerfilDeProfesor"));
            assertEquals(modelAndView.getModel().get("profesor"), profesorMock);
            assertEquals(modelAndView.getModel().get("feedbacks"), feedbacks);
            assertEquals(modelAndView.getModel().get("experiencias"), experiencias);
            assertEquals(modelAndView.getModel().get("promedioCalificacion"), 4.5);
            assertEquals(modelAndView.getModel().get("totalResenas"), 10);
            assertEquals(modelAndView.getModel().get("nombreUsuario"), "Juan");
            assertEquals(modelAndView.getModel().get("esVistaPublica"), true);
            assertEquals(modelAndView.getModel().get("yaSuscripto"), false);
        }

        @Test
        public void verPerfilDeProfesorConEmailInvalidoDeberiaRedirigirAVerTutores() {

            String email = "inexistente@unlam.com";
            when(repositorioUsuarioMock.buscar(email)).thenReturn(null);
            ModelAndView modelAndView = controladorTutores.verPerfilDeProfesor(email, requestMock);
            assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/verTutores"));
        }

        @Test
        public void verPerfilDeProfesorConUsuarioSuscritoDeberiaIndicarSuscripcion() {

            String email = "profesor@unlam.com";
            when(requestMock.getSession()).thenReturn(sessionMock);
            when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
            when(repositorioUsuarioMock.buscar(email)).thenReturn(profesorMock);
            when(servicioExperienciaMock.obtenerExperienciasPorProfesor(anyLong())).thenReturn(Arrays.asList());
            when(servicioFeedbackMock.obtenerFeedbacksPorProfesor(anyLong())).thenReturn(Arrays.asList());
            when(servicioFeedbackMock.calcularPromedioCalificacion(anyLong())).thenReturn(null);
            when(servicioFeedbackMock.contarFeedbackPorProfesor(anyLong())).thenReturn(null);
            when(servicioSuscripcionMock.estaAlumnoSuscritoAProfesor(anyLong(), anyLong())).thenReturn(true);
            ModelAndView modelAndView = controladorTutores.verPerfilDeProfesor(email, requestMock);
            assertEquals(true, modelAndView.getModel().get("yaSuscripto"));
            assertEquals(0.0, modelAndView.getModel().get("promedioCalificacion"));
            assertEquals(0, modelAndView.getModel().get("totalResenas"));
        }

        @Test
        public void suscribirseAProfesorConAlumnoLogueadoDeberiaRetornarExito() {
            // Given
            Long profesorId = 2L;
            when(requestMock.getSession()).thenReturn(sessionMock);
            when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
            when(servicioSuscripcionMock.suscribirAlumnoAProfesor(1L, profesorId)).thenReturn(true);
            when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(profesorMock);

            when(servicioExperienciaMock.obtenerExperienciasPorProfesor(profesorId)).thenReturn(Arrays.asList());
            when(servicioFeedbackMock.obtenerFeedbacksPorProfesor(profesorId)).thenReturn(Arrays.asList());
            when(servicioFeedbackMock.calcularPromedioCalificacion(profesorId)).thenReturn(4.0);
            when(servicioFeedbackMock.contarFeedbackPorProfesor(profesorId)).thenReturn(5);
            ModelAndView modelAndView = controladorTutores.suscribirseAProfesor(profesorId, requestMock);
            assertThat(modelAndView.getViewName(), equalToIgnoringCase("verPerfilDeProfesor"));
            assertThat(modelAndView.getModel().get("mensaje").toString(), equalToIgnoringCase("Te has suscrito exitosamente al profesor"));
            verify(servicioSuscripcionMock, times(1)).suscribirAlumnoAProfesor(1L, profesorId);
        }

        @Test
        public void suscribirseAProfesorSinUsuarioLogueadoDeberiaRedirigirALogin() {

            Long profesorId = 2L;
            when(requestMock.getSession()).thenReturn(sessionMock);
            when(sessionMock.getAttribute("USUARIO")).thenReturn(null);
            ModelAndView modelAndView = controladorTutores.suscribirseAProfesor(profesorId, requestMock);
            assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
        }

        @Test
        public void suscribirseAProfesorConProfesorLogueadoDeberiaRetornarError() {

            Long profesorId = 2L;
            when(requestMock.getSession()).thenReturn(sessionMock);
            when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
            when(servicioLoginMock.obtenerProfesores()).thenReturn(Arrays.asList(profesorMock));

            ModelAndView modelAndView = controladorTutores.suscribirseAProfesor(profesorId, requestMock);
            assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));

        }

        @Test
        public void suscribirseAProfesorYaExistenteDeberiaRetornarError() {

            Long profesorId = 2L;
            when(requestMock.getSession()).thenReturn(sessionMock);
            when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
            when(servicioSuscripcionMock.suscribirAlumnoAProfesor(1L, profesorId)).thenReturn(false);
            when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(profesorMock);

            when(servicioExperienciaMock.obtenerExperienciasPorProfesor(profesorId)).thenReturn(Arrays.asList());
            when(servicioFeedbackMock.obtenerFeedbacksPorProfesor(profesorId)).thenReturn(Arrays.asList());
            when(servicioFeedbackMock.calcularPromedioCalificacion(profesorId)).thenReturn(null);
            when(servicioFeedbackMock.contarFeedbackPorProfesor(profesorId)).thenReturn(null);

            ModelAndView modelAndView = controladorTutores.suscribirseAProfesor(profesorId, requestMock);

            assertThat(modelAndView.getViewName(), equalToIgnoringCase("verPerfilDeProfesor"));
            assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("Ya estás suscrito a este profesor o ocurrió un error"));
        }

        @Test
        public void desuscribirseDeProfesorConAlumnoLogueadoDeberiaRetornarExito() {

            Long profesorId = 2L;
            when(requestMock.getSession()).thenReturn(sessionMock);
            when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
            when(servicioSuscripcionMock.desuscribirAlumnoDeProfesor(1L, profesorId)).thenReturn(true);
            when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(profesorMock);

            when(servicioExperienciaMock.obtenerExperienciasPorProfesor(profesorId)).thenReturn(Arrays.asList());
            when(servicioFeedbackMock.obtenerFeedbacksPorProfesor(profesorId)).thenReturn(Arrays.asList());
            when(servicioFeedbackMock.calcularPromedioCalificacion(profesorId)).thenReturn(3.5);
            when(servicioFeedbackMock.contarFeedbackPorProfesor(profesorId)).thenReturn(8);
            ModelAndView modelAndView = controladorTutores.desuscribirseDeProfesor(profesorId, requestMock);
            assertThat(modelAndView.getViewName(), equalToIgnoringCase("verPerfilDeProfesor"));
            assertThat(modelAndView.getModel().get("mensaje").toString(), equalToIgnoringCase("Te has desuscrito exitosamente del profesor"));
            verify(servicioSuscripcionMock, times(1)).desuscribirAlumnoDeProfesor(1L, profesorId);
        }

        @Test
        public void desuscribirseDeProfesorSinUsuarioLogueadoDeberiaRedirigirALogin() {

            Long profesorId = 2L;
            when(requestMock.getSession()).thenReturn(sessionMock);
            when(sessionMock.getAttribute("USUARIO")).thenReturn(null);
            ModelAndView modelAndView = controladorTutores.desuscribirseDeProfesor(profesorId, requestMock);
            assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
            assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("Debes iniciar sesión para desuscribirte de un profesor"));
        }

        @Test
        public void desuscribirseDeProfesorConProfesorLogueadoDeberiaRetornarError() {
            Long profesorId = 2L;
            when(requestMock.getSession()).thenReturn(sessionMock);
            when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
            when(servicioLoginMock.obtenerProfesores()).thenReturn(Arrays.asList(profesorMock));
            ModelAndView modelAndView = controladorTutores.desuscribirseDeProfesor(profesorId, requestMock);
            assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));

        }

        @Test
        public void desuscribirseDeProfesorNoSuscritoDeberiaRetornarError() {

            Long profesorId = 2L;
            when(requestMock.getSession()).thenReturn(sessionMock);
            when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
            when(servicioSuscripcionMock.desuscribirAlumnoDeProfesor(1L, profesorId)).thenReturn(false);
            when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(profesorMock);

            when(servicioExperienciaMock.obtenerExperienciasPorProfesor(profesorId)).thenReturn(Arrays.asList());
            when(servicioFeedbackMock.obtenerFeedbacksPorProfesor(profesorId)).thenReturn(Arrays.asList());
            when(servicioFeedbackMock.calcularPromedioCalificacion(profesorId)).thenReturn(null);
            when(servicioFeedbackMock.contarFeedbackPorProfesor(profesorId)).thenReturn(null);
            ModelAndView modelAndView = controladorTutores.desuscribirseDeProfesor(profesorId, requestMock);
            assertThat(modelAndView.getViewName(), equalToIgnoringCase("verPerfilDeProfesor"));
            assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("No estabas suscrito a este profesor o ocurrió un error"));
        }

        @Test
        public void suscribirseAProfesorConExcepcionDeberiaRetornarError() {

            Long profesorId = 2L;
            when(requestMock.getSession()).thenReturn(sessionMock);
            when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
            when(servicioSuscripcionMock.suscribirAlumnoAProfesor(anyLong(), anyLong())).thenThrow(new RuntimeException("Error de base de datos"));
            when(servicioLoginMock.obtenerProfesores()).thenReturn(Arrays.asList(profesorMock));
            ModelAndView modelAndView = controladorTutores.suscribirseAProfesor(profesorId, requestMock);
            assertThat(modelAndView.getViewName(), equalToIgnoringCase("verTutores"));
            assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("Ocurrió un error al procesar la suscripción"));
        }

        @Test
        public void desuscribirseDeProfesorConExcepcionDeberiaRetornarError() {

            Long profesorId = 2L;
            when(requestMock.getSession()).thenReturn(sessionMock);
            when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
            when(servicioSuscripcionMock.desuscribirAlumnoDeProfesor(anyLong(), anyLong())).thenThrow(new RuntimeException("Error de base de datos"));
            when(servicioLoginMock.obtenerProfesores()).thenReturn(Arrays.asList(profesorMock));
            ModelAndView modelAndView = controladorTutores.desuscribirseDeProfesor(profesorId, requestMock);
            assertThat(modelAndView.getViewName(), equalToIgnoringCase("verTutores"));
            assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("Error al procesar la desuscripción"));
        }

        @Test
        public void verPerfilDeProfesorConExcepcionDeberiaRedirigirAVerTutores() {

            String email = "profesor@unlam.com";
            when(repositorioUsuarioMock.buscar(email)).thenThrow(new RuntimeException("Error de base de datos"));
            ModelAndView modelAndView = controladorTutores.verPerfilDeProfesor(email, requestMock);
            assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/verTutores"));
        }
        @Test
        public void verTutoresConListaVaciaDeberiaRetornarVistaSinErrores() {
            when(requestMock.getSession()).thenReturn(sessionMock);
            when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
            when(servicioLoginMock.obtenerProfesores()).thenReturn(Arrays.asList());

            ModelAndView modelAndView = controladorTutores.verTutores(requestMock);

            assertThat(modelAndView.getViewName(), equalToIgnoringCase("verTutores"));
            assertEquals(modelAndView.getModel().get("listaProfesores"), Arrays.asList());
        }
        @Test
        public void verPerfilDeProfesorConEmailVacioDeberiaRedirigirAVerTutores() {
            ModelAndView modelAndView = controladorTutores.verPerfilDeProfesor("", requestMock);
            assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/verTutores"));
        }
        @Test
        public void suscribirseAProfesorConIdNuloDeberiaRedirigirAVerTutores() {
            when(requestMock.getSession()).thenReturn(sessionMock);
            when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);

            ModelAndView modelAndView = controladorTutores.suscribirseAProfesor(null, requestMock);
            assertThat(modelAndView.getViewName(), equalToIgnoringCase("verTutores"));
        }
        @Test
        public void desuscribirseDeProfesorConIdNuloDeberiaRedirigirAVerTutores() {
            when(requestMock.getSession()).thenReturn(sessionMock);
            when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);

            ModelAndView modelAndView = controladorTutores.desuscribirseDeProfesor(null, requestMock);
            assertThat(modelAndView.getViewName(), equalToIgnoringCase("verTutores"));
        }
        @Test
        public void verPerfilDeProfesorConNombreConTildesYCaracteresEspeciales() {
            String email = "profesor@unlam.com";
            when(requestMock.getSession()).thenReturn(sessionMock);
            when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
            when(repositorioUsuarioMock.buscar(email)).thenReturn(profesorMock);
            when(profesorMock.getNombre()).thenReturn("José María Ñandú");
            when(servicioExperienciaMock.obtenerExperienciasPorProfesor(anyLong())).thenReturn(Arrays.asList());
            when(servicioFeedbackMock.obtenerFeedbacksPorProfesor(anyLong())).thenReturn(Arrays.asList());
            when(servicioFeedbackMock.contarFeedbackPorProfesor(anyLong())).thenReturn(0);
            when(servicioFeedbackMock.calcularPromedioCalificacion(anyLong())).thenReturn(null);
            when(servicioSuscripcionMock.estaAlumnoSuscritoAProfesor(anyLong(), anyLong())).thenReturn(false);

            ModelAndView modelAndView = controladorTutores.verPerfilDeProfesor(email, requestMock);

            assertEquals(profesorMock, modelAndView.getModel().get("profesor"));
            assertThat(modelAndView.getViewName(), equalToIgnoringCase("verPerfilDeProfesor"));
        }

        @Test
        public void suscribirseAProfesorInexistenteDeberiaRedirigirAVerTutores() {
            Long profesorId = 99L;
            when(requestMock.getSession()).thenReturn(sessionMock);
            when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
            when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(null);

            ModelAndView modelAndView = controladorTutores.suscribirseAProfesor(profesorId, requestMock);
            assertThat(modelAndView.getViewName(), equalToIgnoringCase("verTutores"));
        }
        @Test
        public void desuscribirseDeProfesorInexistenteDeberiaRedirigirAVerTutores() {
            Long profesorId = 99L;
            when(requestMock.getSession()).thenReturn(sessionMock);
            when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
            when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(null);

            ModelAndView modelAndView = controladorTutores.desuscribirseDeProfesor(profesorId, requestMock);
            assertThat(modelAndView.getViewName(), equalToIgnoringCase("verTutores"));
        }
        @Test
        public void verPerfilDeProfesorConUsuarioNoAlumnoDeberiaRedirigirAVerTutores() {
            Usuario usuarioGenerico = mock(Usuario.class);
            String email = "profesor@unlam.com";

            when(requestMock.getSession()).thenReturn(sessionMock);
            when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioGenerico);

            ModelAndView modelAndView = controladorTutores.verPerfilDeProfesor(email, requestMock);

            assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/verTutores"));
        }

    }
