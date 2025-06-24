package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.dominio.servicios.ServicioTema;
import com.tallerwebi.dominio.servicios.ServicioFeedback;
import com.tallerwebi.dominio.servicios.ServicioExperiencia;
import com.tallerwebi.dominio.RepositorioUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ControladorPerfilProfesorTest {

    private ControladorPerfilProfesor controladorPerfilProfesor;
    private Profesor profesorMock;
    private Alumno alumnoMock;
    private Usuario usuarioGenericoMock;
    private Tema temaMock;
    private FeedbackProfesor feedbackMock;
    private ExperienciaEstudiantil experienciaMock;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;
    private ServicioTema servicioTemaMock;
    private ServicioFeedback servicioFeedbackMock;
    private ServicioExperiencia servicioExperienciaMock;
    private RepositorioUsuario repositorioUsuarioMock;
    private RedirectAttributes redirectAttributesMock;

    @BeforeEach
    public void init() {
        profesorMock = mock(Profesor.class);
        alumnoMock = mock(Alumno.class);
        usuarioGenericoMock = mock(Usuario.class);
        temaMock = mock(Tema.class);
        feedbackMock = mock(FeedbackProfesor.class);
        experienciaMock = mock(ExperienciaEstudiantil.class);
        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);
        servicioTemaMock = mock(ServicioTema.class);
        servicioFeedbackMock = mock(ServicioFeedback.class);
        servicioExperienciaMock = mock(ServicioExperiencia.class);
        repositorioUsuarioMock = mock(RepositorioUsuario.class);
        redirectAttributesMock = mock(RedirectAttributes.class);

        when(profesorMock.getId()).thenReturn(1L);
        when(profesorMock.getNombre()).thenReturn("Juan");
        when(profesorMock.getApellido()).thenReturn("Pérez");
        when(profesorMock.getEmail()).thenReturn("profesor@unlam.com");
        when(profesorMock.getDescripcion()).thenReturn("Profesor de matemáticas");
        when(profesorMock.getModalidadPreferida()).thenReturn(ModalidadPreferida.PRESENCIAL);

        when(temaMock.getId()).thenReturn(1L);
        when(temaMock.getNombre()).thenReturn("Matemáticas");

        when(requestMock.getSession()).thenReturn(sessionMock);

        controladorPerfilProfesor = new ControladorPerfilProfesor(
                servicioTemaMock,
                repositorioUsuarioMock,
                servicioFeedbackMock,
                servicioExperienciaMock
        );
    }

    @Test
    public void verPerfilProfesorConProfesorLogueadoDeberiaRetornarVistaConDatos() {

        when(profesorMock.getId()).thenReturn(1L);
        List<ExperienciaEstudiantil> experienciasProfesor = new ArrayList<>();
        experienciasProfesor.add(experienciaMock);
        when(profesorMock.getExperiencias()).thenReturn(experienciasProfesor);

        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarProfesorConExperiencias(1L)).thenReturn(profesorMock);

        List<FeedbackProfesor> feedbacks = Arrays.asList(feedbackMock);
        List<ExperienciaEstudiantil> experiencias = Arrays.asList(experienciaMock);
        List<Tema> temas = Arrays.asList(temaMock);

        when(servicioFeedbackMock.obtenerFeedbacksPorProfesor(1L)).thenReturn(feedbacks);
        when(servicioExperienciaMock.obtenerExperienciasPorProfesor(1L)).thenReturn(experiencias);
        when(servicioTemaMock.obtenerTodos()).thenReturn(temas);
        when(servicioFeedbackMock.calcularPromedioCalificacion(1L)).thenReturn(4.5);
        when(servicioFeedbackMock.contarFeedbackPorProfesor(1L)).thenReturn(10);

        ModelAndView modelAndView = controladorPerfilProfesor.verPerfilProfesor(requestMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("verMiPerfilProfesor"));
        assertEquals(profesorMock, modelAndView.getModel().get("profesor"));
        assertEquals(feedbacks, modelAndView.getModel().get("feedbacks"));
        assertEquals(experiencias, modelAndView.getModel().get("experiencias"));
        assertEquals(temas, modelAndView.getModel().get("todosLosTemas"));
        assertEquals(4.5, modelAndView.getModel().get("promedioCalificacion"));
        assertEquals(10, modelAndView.getModel().get("totalResenas"));
        assertFalse((Boolean) modelAndView.getModel().get("esEdicion"));
    }


    @Test
    public void verPerfilProfesorSinUsuarioLogueadoDeberiaRedirigirALogin() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

        ModelAndView modelAndView = controladorPerfilProfesor.verPerfilProfesor(requestMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
        verify(servicioFeedbackMock, never()).obtenerFeedbacksPorProfesor(any());
    }

    @Test
    public void verPerfilProfesorConAlumnoLogueadoDeberiaRedirigirAHome() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);

        ModelAndView modelAndView = controladorPerfilProfesor.verPerfilProfesor(requestMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
        verify(servicioFeedbackMock, never()).obtenerFeedbacksPorProfesor(any());
    }

    @Test
    public void verPerfilProfesorConUsuarioGenericoDeberiaRedirigirAHome() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioGenericoMock);

        ModelAndView modelAndView = controladorPerfilProfesor.verPerfilProfesor(requestMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
        verify(servicioFeedbackMock, never()).obtenerFeedbacksPorProfesor(any());
    }

    @Test
    public void verPerfilProfesorConPromedioCeroDeberiaRetornarCeroEnPromedio() {
        when(profesorMock.getId()).thenReturn(1L);
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarProfesorConExperiencias(1L)).thenReturn(profesorMock);
        when(servicioFeedbackMock.obtenerFeedbacksPorProfesor(1L)).thenReturn(new ArrayList<>());
        when(servicioExperienciaMock.obtenerExperienciasPorProfesor(1L)).thenReturn(new ArrayList<>());
        when(servicioTemaMock.obtenerTodos()).thenReturn(new ArrayList<>());
        when(servicioFeedbackMock.calcularPromedioCalificacion(1L)).thenReturn(null);
        when(servicioFeedbackMock.contarFeedbackPorProfesor(1L)).thenReturn(null);

        ModelAndView modelAndView = controladorPerfilProfesor.verPerfilProfesor(requestMock);

        assertEquals(0.0, modelAndView.getModel().get("promedioCalificacion"));
        assertEquals(0, modelAndView.getModel().get("totalResenas"));
    }


    @Test
    public void editarPerfilProfesorConProfesorLogueadoDeberiaRetornarVistaEnModoEdicion() {

        when(profesorMock.getId()).thenReturn(1L);
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarProfesorConExperiencias(1L)).thenReturn(profesorMock);
        List<Tema> temas = Arrays.asList(temaMock);
        List<ExperienciaEstudiantil> experiencias = Arrays.asList(experienciaMock);


        when(servicioExperienciaMock.obtenerExperienciasPorProfesor(1L)).thenReturn(experiencias);
        when(servicioTemaMock.obtenerTodos()).thenReturn(temas);

        ModelAndView modelAndView = controladorPerfilProfesor.editarPerfilProfesor(requestMock);
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("verMiPerfilProfesor"));
        assertEquals(profesorMock, modelAndView.getModel().get("profesor"));
        assertEquals(temas, modelAndView.getModel().get("todosLosTemas"));
        assertEquals(experiencias, modelAndView.getModel().get("experiencias"));
        assertTrue((Boolean) modelAndView.getModel().get("esEdicion"));
    }

    @Test
    public void editarPerfilProfesorSinUsuarioLogueadoDeberiaRedirigirALogin() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

        ModelAndView modelAndView = controladorPerfilProfesor.editarPerfilProfesor(requestMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
        verify(servicioTemaMock, never()).obtenerTodos();
    }

    @Test
    public void editarPerfilProfesorConAlumnoDeberiaRedirigirAHome() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);

        ModelAndView modelAndView = controladorPerfilProfesor.editarPerfilProfesor(requestMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
        verify(servicioTemaMock, never()).obtenerTodos();
    }

    @Test
    public void actualizarPerfilProfesorConDatosValidosDeberiaActualizarYRedirigir() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(profesorMock);
        when(servicioTemaMock.obtenerPorId(1L)).thenReturn(temaMock);

        String nombre = "Juan Carlos";
        String apellido = "Pérez";
        String descripcion = "Nueva descripción";
        String modalidadPreferida = "VIRTUAL";
        Long temaId = 1L;

        ModelAndView modelAndView = controladorPerfilProfesor.actualizarPerfilProfesor(
                nombre, apellido, descripcion, modalidadPreferida, temaId, null, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/profesor/perfil"));
        verify(profesorMock).setNombre(nombre);
        verify(profesorMock).setApellido(apellido);
        verify(profesorMock).setDescripcion(descripcion);
        verify(profesorMock).setModalidadPreferida(ModalidadPreferida.VIRTUAL);
        verify(profesorMock).setTema(temaMock);
        verify(repositorioUsuarioMock).modificar(profesorMock);
        verify(sessionMock).setAttribute("USUARIO", profesorMock);
    }

    @Test
    public void actualizarPerfilProfesorConNombreVacioDeberiaMostrarError() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);

        ModelAndView modelAndView = controladorPerfilProfesor.actualizarPerfilProfesor(
                "", "Apellido", "Descripción", "PRESENCIAL", 1L, null, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/profesor/perfil/editar"));
        verify(repositorioUsuarioMock, never()).modificar(any());
    }

    @Test
    public void actualizarPerfilProfesorConApellidoVacioDeberiaMostrarError() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);

        ModelAndView modelAndView = controladorPerfilProfesor.actualizarPerfilProfesor(
                "Nombre", "", "Descripción", "PRESENCIAL", 1L, null, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/profesor/perfil/editar"));
        verify(repositorioUsuarioMock, never()).modificar(any());
    }

    @Test
    public void actualizarPerfilProfesorConFotoBase64ValidaDeberiaActualizarFoto() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(profesorMock);
        String fotoBase64Valida = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";

        ModelAndView modelAndView = controladorPerfilProfesor.actualizarPerfilProfesor(
                "Nombre", "Apellido", "Descripción", "PRESENCIAL", 1L, fotoBase64Valida, requestMock, redirectAttributesMock);

        verify(profesorMock).setFotoPerfil(fotoBase64Valida);
        verify(repositorioUsuarioMock).modificar(profesorMock);
    }

    @Test
    public void actualizarPerfilProfesorConFotoBase64InvalidaDeberiaMostrarError() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(profesorMock);
        String fotoBase64Invalida = "imagen_invalida";

        ModelAndView modelAndView = controladorPerfilProfesor.actualizarPerfilProfesor(
                "Nombre", "Apellido", "Descripción", "PRESENCIAL", 1L, fotoBase64Invalida, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/profesor/perfil/editar"));
        verify(profesorMock, never()).setFotoPerfil(any());
        verify(repositorioUsuarioMock, never()).modificar(any());
    }

    @Test
    public void actualizarPerfilProfesorSinUsuarioLogueadoDeberiaRedirigirALogin() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

        ModelAndView modelAndView = controladorPerfilProfesor.actualizarPerfilProfesor(
                "Nombre", "Apellido", "Descripción", "PRESENCIAL", 1L, null, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
        verify(repositorioUsuarioMock, never()).modificar(any());
    }

    @Test
    public void actualizarPerfilProfesorConAlumnoDeberiaRedirigirAHome() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);

        ModelAndView modelAndView = controladorPerfilProfesor.actualizarPerfilProfesor(
                "Nombre", "Apellido", "Descripción", "PRESENCIAL", 1L, null, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
        verify(repositorioUsuarioMock, never()).modificar(any());
    }

    @Test
    public void actualizarPerfilProfesorConModalidadVaciaDeberiaAsignarNull() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(profesorMock);

        ModelAndView modelAndView = controladorPerfilProfesor.actualizarPerfilProfesor(
                "Nombre", "Apellido", "Descripción", "", 1L, null, requestMock, redirectAttributesMock);

        verify(profesorMock).setModalidadPreferida(null);
        verify(repositorioUsuarioMock).modificar(profesorMock);
    }

    @Test
    public void actualizarPerfilProfesorConDescripcionVaciaDeberiaAsignarNull() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(profesorMock);

        ModelAndView modelAndView = controladorPerfilProfesor.actualizarPerfilProfesor(
                "Nombre", "Apellido", "", "PRESENCIAL", 1L, null, requestMock, redirectAttributesMock);

        verify(profesorMock).setDescripcion(null);
        verify(repositorioUsuarioMock).modificar(profesorMock);
    }

    @Test
    public void actualizarPerfilProfesorConErrorDeberiaAgregarMensajeDeError() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(profesorMock);
        doThrow(new RuntimeException("Error de base de datos")).when(repositorioUsuarioMock).modificar(profesorMock);

        ModelAndView modelAndView = controladorPerfilProfesor.actualizarPerfilProfesor(
                "Nombre", "Apellido", "Descripción", "PRESENCIAL", 1L, null, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/profesor/perfil"));
    }

    @Test
    public void eliminarFotoProfesorConProfesorLogueadoDeberiaEliminarFotoYRedirigir() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(profesorMock);

        ModelAndView modelAndView = controladorPerfilProfesor.eliminarFotoProfesor(requestMock, redirectAttributesMock);

        verify(profesorMock).setFotoPerfil(null);
        verify(repositorioUsuarioMock).modificar(profesorMock);
        verify(sessionMock).setAttribute("USUARIO", profesorMock);
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/profesor/perfil"));
    }

    @Test
    public void eliminarFotoProfesorSinUsuarioLogueadoDeberiaRedirigirALogin() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

        ModelAndView modelAndView = controladorPerfilProfesor.eliminarFotoProfesor(requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
        verify(repositorioUsuarioMock, never()).modificar(any());
    }

    @Test
    public void eliminarFotoProfesorConAlumnoDeberiaRedirigirAHome() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);

        ModelAndView modelAndView = controladorPerfilProfesor.eliminarFotoProfesor(requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
        verify(repositorioUsuarioMock, never()).modificar(any());
    }

    @Test
    public void eliminarFotoProfesorConErrorDeberiaAgregarMensajeDeError() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(profesorMock);
        doThrow(new RuntimeException("Error de base de datos")).when(repositorioUsuarioMock).modificar(profesorMock);

        ModelAndView modelAndView = controladorPerfilProfesor.eliminarFotoProfesor(requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/profesor/perfil"));
    }

    @Test
    public void agregarExperienciaConDatosValidosDeberiaAgregarExperienciaYRedirigir() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(profesorMock);

        String institucion = "Universidad";
        String descripcion = "Profesor de Programación";
        String fecha = "2020-2023";

        ModelAndView modelAndView = controladorPerfilProfesor.agregarExperiencia(
                institucion, descripcion, fecha, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/profesor/perfil"));
        verify(servicioExperienciaMock).guardar(any(ExperienciaEstudiantil.class));
    }

    @Test
    public void agregarExperienciaSinUsuarioLogueadoDeberiaRedirigirALogin() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

        ModelAndView modelAndView = controladorPerfilProfesor.agregarExperiencia(
                "Institución", "Descripción", "2020", requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
        verify(servicioExperienciaMock, never()).guardar(any());
    }

    @Test
    public void agregarExperienciaConAlumnoDeberiaRedirigirAHome() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);

        ModelAndView modelAndView = controladorPerfilProfesor.agregarExperiencia(
                "Institución", "Descripción", "2020", requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
        verify(servicioExperienciaMock, never()).guardar(any());
    }

    @Test
    public void agregarExperienciaConErrorDeberiaAgregarMensajeDeError() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(profesorMock);
        doThrow(new RuntimeException("Error de base de datos")).when(servicioExperienciaMock).guardar(any());

        ModelAndView modelAndView = controladorPerfilProfesor.agregarExperiencia(
                "Institución", "Descripción", "2020", requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/profesor/perfil"));
    }

    @Test
    public void eliminarExperienciaConIdValidoDeberiaEliminarExperienciaYRedirigir() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
        Long experienciaId = 1L;

        ModelAndView modelAndView = controladorPerfilProfesor.eliminarExperiencia(
                experienciaId, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/profesor/perfil"));
        verify(servicioExperienciaMock).eliminar(experienciaId);
    }

    @Test
    public void eliminarExperienciaSinUsuarioLogueadoDeberiaRedirigirALogin() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

        ModelAndView modelAndView = controladorPerfilProfesor.eliminarExperiencia(
                1L, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
        verify(servicioExperienciaMock, never()).eliminar(any());
    }

    @Test
    public void eliminarExperienciaConAlumnoDeberiaRedirigirAHome() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);

        ModelAndView modelAndView = controladorPerfilProfesor.eliminarExperiencia(
                1L, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
        verify(servicioExperienciaMock, never()).eliminar(any());
    }

    @Test
    public void eliminarExperienciaConErrorDeberiaAgregarMensajeDeError() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
        Long experienciaId = 1L;
        doThrow(new RuntimeException("Error de base de datos")).when(servicioExperienciaMock).eliminar(experienciaId);

        ModelAndView modelAndView = controladorPerfilProfesor.eliminarExperiencia(
                experienciaId, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/profesor/perfil"));
    }

    @Test
    public void actualizarPerfilProfesorConTemaInvalidoDeberiaMantenerseSinCambios() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(profesorMock);
        when(servicioTemaMock.obtenerPorId(999L)).thenReturn(null); // Tema no encontrado

        ModelAndView modelAndView = controladorPerfilProfesor.actualizarPerfilProfesor(
                "Nombre", "Apellido", "Descripción", "PRESENCIAL", 999L, null, requestMock, redirectAttributesMock);

        verify(profesorMock, never()).setTema(any());
        verify(repositorioUsuarioMock).modificar(profesorMock);
    }

    @Test
    public void actualizarPerfilProfesorConModalidadInvalidaDeberiaAsignarNull() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(profesorMock);

        ModelAndView modelAndView = controladorPerfilProfesor.actualizarPerfilProfesor(
                "Nombre", "Apellido", "Descripción", "MODALIDAD_INEXISTENTE", 1L, null, requestMock, redirectAttributesMock);

        verify(profesorMock).setModalidadPreferida(null);
        verify(repositorioUsuarioMock).modificar(profesorMock);
    }

    @Test
    public void actualizarPerfilProfesorConTemaIdNuloNoDeberiaModificarTema() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(profesorMock);

        ModelAndView modelAndView = controladorPerfilProfesor.actualizarPerfilProfesor(
                "Nombre", "Apellido", "Descripción", "PRESENCIAL", null, null, requestMock, redirectAttributesMock);

        verify(servicioTemaMock, never()).obtenerPorId(any());
        verify(profesorMock, never()).setTema(any());
        verify(repositorioUsuarioMock).modificar(profesorMock);
    }

    @Test
    public void actualizarPerfilProfesorConFotoBase64VaciaNoDeberiaModificarFoto() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(profesorMock);

        ModelAndView modelAndView = controladorPerfilProfesor.actualizarPerfilProfesor(
                "Nombre", "Apellido", "Descripción", "PRESENCIAL", 1L, "", requestMock, redirectAttributesMock);

        verify(profesorMock, never()).setFotoPerfil(any());
        verify(repositorioUsuarioMock).modificar(profesorMock);
    }

    @Test
    public void verPerfilProfesorConProfesorSinExperienciasDeberiaRetornarListaVacia() {
        when(profesorMock.getId()).thenReturn(1L);
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarProfesorConExperiencias(1L)).thenReturn(profesorMock);
        when(profesorMock.getExperiencias()).thenReturn(new ArrayList<>());

        List<ExperienciaEstudiantil> experienciasVacias = new ArrayList<>();
        List<FeedbackProfesor> feedbacksVacios = new ArrayList<>();
        List<Tema> temas = Arrays.asList(temaMock);

        when(servicioFeedbackMock.obtenerFeedbacksPorProfesor(1L)).thenReturn(feedbacksVacios);
        when(servicioExperienciaMock.obtenerExperienciasPorProfesor(1L)).thenReturn(experienciasVacias);
        when(servicioTemaMock.obtenerTodos()).thenReturn(temas);
        when(servicioFeedbackMock.calcularPromedioCalificacion(1L)).thenReturn(0.0);
        when(servicioFeedbackMock.contarFeedbackPorProfesor(1L)).thenReturn(0);

        ModelAndView modelAndView = controladorPerfilProfesor.verPerfilProfesor(requestMock);

        assertEquals(experienciasVacias, modelAndView.getModel().get("experiencias"));
        assertEquals(feedbacksVacios, modelAndView.getModel().get("feedbacks"));
        assertTrue(((List<?>) modelAndView.getModel().get("experiencias")).isEmpty());
    }

    @Test
    public void actualizarPerfilProfesorConNombreSoloEspaciosDeberiaDetectarloComoVacio() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);

        ModelAndView modelAndView = controladorPerfilProfesor.actualizarPerfilProfesor(
                "   ", "Apellido", "Descripción", "PRESENCIAL", 1L, null, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/profesor/perfil/editar"));
        verify(repositorioUsuarioMock, never()).modificar(any());
    }

    @Test
    public void actualizarPerfilProfesorConApellidoSoloEspaciosDeberiaDetectarloComoVacio() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);

        ModelAndView modelAndView = controladorPerfilProfesor.actualizarPerfilProfesor(
                "Nombre", "   ", "Descripción", "PRESENCIAL", 1L, null, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/profesor/perfil/editar"));
        verify(repositorioUsuarioMock, never()).modificar(any());
    }

    @Test
    public void actualizarPerfilProfesorConBase64ConCaracteresInvalidosDeberiaMostrarError() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(profesorMock);
        String fotoConCaracteresInvalidos = "data:image/png;base64,invalid@characters#here!";

        ModelAndView modelAndView = controladorPerfilProfesor.actualizarPerfilProfesor(
                "Nombre", "Apellido", "Descripción", "PRESENCIAL", 1L, fotoConCaracteresInvalidos, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/profesor/perfil/editar"));
    }

    @Test
    public void actualizarPerfilProfesorConBase64DeLongitudIncorrectaDeberiaMostrarError() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(profesorMock);
        String fotoLongitudIncorrecta = "data:image/png;base64,abc";

        ModelAndView modelAndView = controladorPerfilProfesor.actualizarPerfilProfesor(
                "Nombre", "Apellido", "Descripción", "PRESENCIAL", 1L, fotoLongitudIncorrecta, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/profesor/perfil/editar"));
    }
}