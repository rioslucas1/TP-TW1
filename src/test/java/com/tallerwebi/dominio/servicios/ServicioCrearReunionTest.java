/*package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.RepositorioReservaAlumno;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.disponibilidadProfesor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServicioCrearReunionTest {

    private ServicioReservaAlumno servicioReservaAlumno;
    private RepositorioReservaAlumno repositorioReservaAlumnoMock;
    private ServicioCrearReunion servicioCrearReunion;
    private Alumno alumnoMock;
    private Profesor profesorMock;
    private disponibilidadProfesor disponibilidadMock;

    @BeforeEach
    public void init() {
        repositorioReservaAlumnoMock = mock(RepositorioReservaAlumno.class);
        servicioCrearReunion = mock(ServicioCrearReunion.class);
        servicioReservaAlumno = new ServicioReservaAlumnoMeetImpl(
                repositorioReservaAlumnoMock, servicioCrearReunion);

        alumnoMock = mock(Alumno.class);
        when(alumnoMock.getEmail()).thenReturn("alumno@unlam.com");
        when(alumnoMock.getNombre()).thenReturn("Juan");
        when(alumnoMock.getApellido()).thenReturn("Perez");

        profesorMock = mock(Profesor.class);
        when(profesorMock.getEmail()).thenReturn("profesor@unlam.com");
        when(profesorMock.getNombre()).thenReturn("Ana");
        when(profesorMock.getApellido()).thenReturn("Garcia");

        disponibilidadMock = mock(disponibilidadProfesor.class);
        when(disponibilidadMock.getId()).thenReturn(1L);
        when(disponibilidadMock.getEmailProfesor()).thenReturn("profesor@unlam.com");
        when(disponibilidadMock.getMailAlumno()).thenReturn("alumno@unlam.com");
        when(disponibilidadMock.getDiaSemana()).thenReturn("Lunes");
        when(disponibilidadMock.getHora()).thenReturn("09:00");
        when(disponibilidadMock.getFechaEspecifica()).thenReturn(LocalDate.now().plusDays(1));
        when(disponibilidadMock.getProfesor()).thenReturn(profesorMock);
        when(disponibilidadMock.getAlumno()).thenReturn(alumnoMock);
        when(disponibilidadMock.isDisponible()).thenReturn(true);
    }

    @Test
    public void reservarClasePorIdDeberiaCrearReunionMeetYGuardarEnlace() {

        Long disponibilidadId = 1L;
        String enlaceMeetEsperado = "https://meet.google.com/abc-defg-hij";

        when(repositorioReservaAlumnoMock.buscarPorId(disponibilidadId))
                .thenReturn(disponibilidadMock);
        when(servicioCrearReunion.crearReunionMeet(disponibilidadMock))
                .thenReturn(enlaceMeetEsperado);

        // When
        servicioReservaAlumno.reservarClasePorId(disponibilidadId, alumnoMock);

        // Then
        verify(disponibilidadMock, times(1)).marcarComoReservado();
        verify(disponibilidadMock, times(1)).setAlumno(alumnoMock);
        verify(servicioCrearReunion, times(1)).crearReunionMeet(disponibilidadMock);
        verify(disponibilidadMock, times(1)).setEnlace_meet(enlaceMeetEsperado);
        verify(repositorioReservaAlumnoMock, times(1)).guardar(disponibilidadMock);
    }

    @Test
    public void reservarClaseDeberiaCrearReunionMeetYGuardarEnlace() {
        // Given
        String emailProfesor = "profesor@unlam.com";
        String diaSemana = "Lunes";
        String hora = "09:00";
        String enlaceMeetEsperado = "https://meet.google.com/xyz-abcd-efg";

        when(repositorioReservaAlumnoMock.buscarPorProfesorDiaHora(emailProfesor, diaSemana, hora))
                .thenReturn(disponibilidadMock);
        when(servicioCrearReunion.crearReunionMeet(disponibilidadMock))
                .thenReturn(enlaceMeetEsperado);

        // When
        servicioReservaAlumno.reservarClase(emailProfesor, diaSemana, hora, alumnoMock);

        // Then
        verify(disponibilidadMock, times(1)).marcarComoReservado();
        verify(disponibilidadMock, times(1)).setAlumno(alumnoMock);
        verify(servicioCrearReunion, times(1)).crearReunionMeet(disponibilidadMock);
        verify(disponibilidadMock, times(1)).setEnlace_meet(enlaceMeetEsperado);
        verify(repositorioReservaAlumnoMock, times(1)).guardar(disponibilidadMock);
    }

    @Test
    public void reservarClaseCuandoFallaCreacionMeetDeberiaLanzarExcepcion() {
        // Given
        Long disponibilidadId = 1L;
        String mensajeError = "Error al crear reunión de Google Meet";

        when(repositorioReservaAlumnoMock.buscarPorId(disponibilidadId))
                .thenReturn(disponibilidadMock);
        when(servicioCrearReunion.crearReunionMeet(disponibilidadMock))
                .thenThrow(new RuntimeException(mensajeError));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            servicioReservaAlumno.reservarClasePorId(disponibilidadId, alumnoMock);
        });

        assertEquals(mensajeError, exception.getMessage());
        verify(disponibilidadMock, times(1)).marcarComoReservado();
        verify(disponibilidadMock, times(1)).setAlumno(alumnoMock);
        verify(servicioCrearReunion, times(1)).crearReunionMeet(disponibilidadMock);
        verify(repositorioReservaAlumnoMock, never()).guardar(any());
    }

    @Test
    public void reservarClaseConHorarioNoDisponibleNoDeberiaCrearReunionMeet() {
        // Given
        Long disponibilidadId = 1L;

        when(disponibilidadMock.isDisponible()).thenReturn(false);
        when(repositorioReservaAlumnoMock.buscarPorId(disponibilidadId))
                .thenReturn(disponibilidadMock);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            servicioReservaAlumno.reservarClasePorId(disponibilidadId, alumnoMock);
        });

        assertEquals("El horario no está disponible para reservar", exception.getMessage());
        verify(servicioCrearReunion, never()).crearReunionMeet(any());
        verify(disponibilidadMock, never()).setEnlace_meet(any());
        verify(repositorioReservaAlumnoMock, never()).guardar(any());
    }

    @Test
    public void reservarClaseConHorarioInexistenteNoDeberiaCrearReunionMeet() {
        // Given
        Long disponibilidadId = 999L;

        when(repositorioReservaAlumnoMock.buscarPorId(disponibilidadId))
                .thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            servicioReservaAlumno.reservarClasePorId(disponibilidadId, alumnoMock);
        });

        assertEquals("El horario no está disponible para reservar", exception.getMessage());
        verify(servicioCrearReunion, never()).crearReunionMeet(any());
        verify(repositorioReservaAlumnoMock, never()).guardar(any());
    }


}
 */