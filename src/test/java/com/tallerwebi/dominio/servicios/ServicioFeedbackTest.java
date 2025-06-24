package com.tallerwebi.dominio.servicios;
import com.tallerwebi.dominio.RepositorioFeedback;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.FeedbackProfesor;
import com.tallerwebi.dominio.entidades.Profesor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServicioFeedbackTest {

    private ServicioFeedback servicioFeedback;
    private RepositorioFeedback repositorioFeedbackMock;
    private FeedbackProfesor feedbackMock;
    private Profesor profesorMock;
    private Alumno alumnoMock;

    @BeforeEach
    public void init() {
        repositorioFeedbackMock = mock(RepositorioFeedback.class);
        servicioFeedback = new ServicioFeedbackImpl(repositorioFeedbackMock);

        profesorMock = mock(Profesor.class);
        when(profesorMock.getId()).thenReturn(1L);
        when(profesorMock.getNombre()).thenReturn("Juan");
        when(profesorMock.getApellido()).thenReturn("Pérez");

        alumnoMock = mock(Alumno.class);
        when(alumnoMock.getId()).thenReturn(1L);
        when(alumnoMock.getNombre()).thenReturn("María");
        when(alumnoMock.getApellido()).thenReturn("González");

        feedbackMock = mock(FeedbackProfesor.class);
        when(feedbackMock.getId()).thenReturn(1L);
        when(feedbackMock.getProfesor()).thenReturn(profesorMock);
        when(feedbackMock.getAlumno()).thenReturn(alumnoMock);
        when(feedbackMock.getCalificacion()).thenReturn(5);
        when(feedbackMock.getComentario()).thenReturn("Excelente profesor");
        when(feedbackMock.getFechaCreacion()).thenReturn(LocalDateTime.now());
        when(feedbackMock.getNombreEstudiante()).thenReturn("María González");
    }

    @Test
    public void guardarFeedbackValidoDeberiaRetornarFeedbackGuardado() {
        when(repositorioFeedbackMock.guardar(feedbackMock)).thenReturn(feedbackMock);

        FeedbackProfesor feedbackGuardado = servicioFeedback.guardar(feedbackMock);

        assertNotNull(feedbackGuardado);
        assertEquals(feedbackMock, feedbackGuardado);
        verify(repositorioFeedbackMock, times(1)).guardar(feedbackMock);
    }

    @Test
    public void guardarFeedbackNullDeberiaLlamarRepositorio() {
        when(repositorioFeedbackMock.guardar(null)).thenReturn(null);

        FeedbackProfesor feedbackGuardado = servicioFeedback.guardar(null);

        assertNull(feedbackGuardado);
        verify(repositorioFeedbackMock, times(1)).guardar(null);
    }

    @Test
    public void obtenerFeedbacksPorProfesorConIdValidoDeberiaRetornarListaDeFeedbacks() {
        Long profesorId = 1L;
        FeedbackProfesor feedback1 = mock(FeedbackProfesor.class);
        FeedbackProfesor feedback2 = mock(FeedbackProfesor.class);
        List<FeedbackProfesor> feedbacksEsperados = Arrays.asList(feedback1, feedback2);

        when(repositorioFeedbackMock.obtenerPorProfesorId(profesorId)).thenReturn(feedbacksEsperados);

        List<FeedbackProfesor> feedbacksObtenidos = servicioFeedback.obtenerFeedbacksPorProfesor(profesorId);

        assertNotNull(feedbacksObtenidos);
        assertEquals(2, feedbacksObtenidos.size());
        assertThat(feedbacksObtenidos, containsInAnyOrder(feedback1, feedback2));
        verify(repositorioFeedbackMock, times(1)).obtenerPorProfesorId(profesorId);
    }

    @Test
    public void obtenerFeedbacksPorProfesorSinFeedbacksDeberiaRetornarListaVacia() {
        Long profesorId = 1L;
        List<FeedbackProfesor> listaVacia = Arrays.asList();

        when(repositorioFeedbackMock.obtenerPorProfesorId(profesorId)).thenReturn(listaVacia);

        List<FeedbackProfesor> feedbacksObtenidos = servicioFeedback.obtenerFeedbacksPorProfesor(profesorId);

        assertNotNull(feedbacksObtenidos);
        assertEquals(0, feedbacksObtenidos.size());
        assertEquals(listaVacia, feedbacksObtenidos);
        verify(repositorioFeedbackMock, times(1)).obtenerPorProfesorId(profesorId);
    }

    @Test
    public void obtenerFeedbacksPorProfesorConIdNullDeberiaLlamarRepositorio() {
        Long profesorId = null;
        when(repositorioFeedbackMock.obtenerPorProfesorId(profesorId)).thenReturn(Arrays.asList());

        List<FeedbackProfesor> feedbacksObtenidos = servicioFeedback.obtenerFeedbacksPorProfesor(profesorId);

        assertNotNull(feedbacksObtenidos);
        verify(repositorioFeedbackMock, times(1)).obtenerPorProfesorId(profesorId);
    }

    @Test
    public void obtenerFeedbackPorIdConIdValidoDeberiaRetornarFeedback() {
        Long feedbackId = 1L;
        when(repositorioFeedbackMock.buscarPorId(feedbackId)).thenReturn(feedbackMock);

        FeedbackProfesor feedbackObtenido = servicioFeedback.obtenerPorId(feedbackId);

        assertNotNull(feedbackObtenido);
        assertEquals(feedbackMock, feedbackObtenido);
        verify(repositorioFeedbackMock, times(1)).buscarPorId(feedbackId);
    }

    @Test
    public void obtenerFeedbackPorIdConIdInexistenteDeberiaRetornarNull() {
        Long feedbackId = 999L;
        when(repositorioFeedbackMock.buscarPorId(feedbackId)).thenReturn(null);

        FeedbackProfesor feedbackObtenido = servicioFeedback.obtenerPorId(feedbackId);

        assertNull(feedbackObtenido);
        verify(repositorioFeedbackMock, times(1)).buscarPorId(feedbackId);
    }

    @Test
    public void obtenerFeedbackPorIdConIdNullDeberiaLlamarRepositorio() {
        Long feedbackId = null;
        when(repositorioFeedbackMock.buscarPorId(feedbackId)).thenReturn(null);

        FeedbackProfesor feedbackObtenido = servicioFeedback.obtenerPorId(feedbackId);

        assertNull(feedbackObtenido);
        verify(repositorioFeedbackMock, times(1)).buscarPorId(feedbackId);
    }

    @Test
    public void eliminarFeedbackConIdValidoDeberiaLlamarRepositorio() {
        Long feedbackId = 1L;

        servicioFeedback.eliminar(feedbackId);

        verify(repositorioFeedbackMock, times(1)).eliminar(feedbackId);
    }

    @Test
    public void eliminarFeedbackConIdNullDeberiaLlamarRepositorio() {
        Long feedbackId = null;

        servicioFeedback.eliminar(feedbackId);

        verify(repositorioFeedbackMock, times(1)).eliminar(feedbackId);
    }

    @Test
    public void obtenerTodosFeedbacksDeberiaRetornarListaCompleta() {
        FeedbackProfesor feedback1 = mock(FeedbackProfesor.class);
        FeedbackProfesor feedback2 = mock(FeedbackProfesor.class);
        FeedbackProfesor feedback3 = mock(FeedbackProfesor.class);
        List<FeedbackProfesor> feedbacksEsperados = Arrays.asList(feedback1, feedback2, feedback3);

        when(repositorioFeedbackMock.obtenerTodos()).thenReturn(feedbacksEsperados);

        List<FeedbackProfesor> feedbacksObtenidos = servicioFeedback.obtenerTodos();

        assertNotNull(feedbacksObtenidos);
        assertEquals(3, feedbacksObtenidos.size());
        assertThat(feedbacksObtenidos, containsInAnyOrder(feedback1, feedback2, feedback3));
        verify(repositorioFeedbackMock, times(1)).obtenerTodos();
    }

    @Test
    public void obtenerTodosFeedbacksSinFeedbacksDeberiaRetornarListaVacia() {
        List<FeedbackProfesor> listaVacia = Arrays.asList();
        when(repositorioFeedbackMock.obtenerTodos()).thenReturn(listaVacia);

        List<FeedbackProfesor> feedbacksObtenidos = servicioFeedback.obtenerTodos();

        assertNotNull(feedbacksObtenidos);
        assertEquals(0, feedbacksObtenidos.size());
        assertEquals(listaVacia, feedbacksObtenidos);
        verify(repositorioFeedbackMock, times(1)).obtenerTodos();
    }


    @Test
    public void calcularPromedioCalificacionConFeedbacksValidosDeberiaRetornarPromedio() {
        Long profesorId = 1L;
        FeedbackProfesor feedback1 = mock(FeedbackProfesor.class);
        FeedbackProfesor feedback2 = mock(FeedbackProfesor.class);
        FeedbackProfesor feedback3 = mock(FeedbackProfesor.class);

        when(feedback1.getCalificacion()).thenReturn(4);
        when(feedback2.getCalificacion()).thenReturn(5);
        when(feedback3.getCalificacion()).thenReturn(3);

        List<FeedbackProfesor> feedbacks = Arrays.asList(feedback1, feedback2, feedback3);
        when(repositorioFeedbackMock.obtenerPorProfesorId(profesorId)).thenReturn(feedbacks);

        Double promedio = servicioFeedback.calcularPromedioCalificacion(profesorId);

        assertNotNull(promedio);
        assertEquals(4.0, promedio, 0.01);
        verify(repositorioFeedbackMock, times(1)).obtenerPorProfesorId(profesorId);
    }

    @Test
    public void calcularPromedioCalificacionSinFeedbacksDeberiaRetornarCero() {
        Long profesorId = 1L;
        List<FeedbackProfesor> listaVacia = Arrays.asList();
        when(repositorioFeedbackMock.obtenerPorProfesorId(profesorId)).thenReturn(listaVacia);

        Double promedio = servicioFeedback.calcularPromedioCalificacion(profesorId);

        assertEquals(0.0, promedio, 0.01);
        verify(repositorioFeedbackMock, times(1)).obtenerPorProfesorId(profesorId);
    }

    @Test
    public void obtenerFeedbacksPorProfesorConUnSoloFeedbackDeberiaRetornarListaConUnElemento() {
        Long profesorId = 1L;
        List<FeedbackProfesor> feedbacksEsperados = Arrays.asList(feedbackMock);

        when(repositorioFeedbackMock.obtenerPorProfesorId(profesorId)).thenReturn(feedbacksEsperados);

        List<FeedbackProfesor> feedbacksObtenidos = servicioFeedback.obtenerFeedbacksPorProfesor(profesorId);

        assertNotNull(feedbacksObtenidos);
        assertEquals(1, feedbacksObtenidos.size());
        assertEquals(feedbackMock, feedbacksObtenidos.get(0));
        verify(repositorioFeedbackMock, times(1)).obtenerPorProfesorId(profesorId);
    }

}