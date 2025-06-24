package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.RepositorioReservaAlumno;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.dominio.entidades.Clase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServicioReservaAlumnoImplTest {

	private ServicioReservaAlumno servicioReservaAlumno;
	private RepositorioReservaAlumno repositorioReservaAlumnoMock;
	private Alumno alumnoMock;
	private Profesor profesorMock;
	private Usuario usuarioMock;
	private Clase disponibilidadMock;

	@BeforeEach
	public void init() {
		repositorioReservaAlumnoMock = mock(RepositorioReservaAlumno.class);
		servicioReservaAlumno = new ServicioReservaAlumnoImpl(repositorioReservaAlumnoMock);

		alumnoMock = mock(Alumno.class);
		when(alumnoMock.getEmail()).thenReturn("alumno@unlam.com");
		when(alumnoMock.getNombre()).thenReturn("Juan");
		when(alumnoMock.getApellido()).thenReturn("Perez");

		disponibilidadMock = mock(Clase.class);
		when(disponibilidadMock.getId()).thenReturn(1L);
		when(disponibilidadMock.getEmailProfesor()).thenReturn("profesor@unlam.com");
		when(disponibilidadMock.getDiaSemana()).thenReturn("Lunes");
		when(disponibilidadMock.getHora()).thenReturn("09:00");
		when(disponibilidadMock.isDisponible()).thenReturn(true);
	}

	@Test
	public void obtenerDisponibilidadProfesorDeberiaRetornarListaDeDisponibilidades() {
		String emailProfesor = "profesor@unlam.com";
		Clase disponibilidad1 = mock(Clase.class);
		Clase disponibilidad2 = mock(Clase.class);
		List<Clase> disponibilidadesEsperadas = Arrays.asList(disponibilidad1, disponibilidad2);

		when(repositorioReservaAlumnoMock.buscarPorProfesor(emailProfesor)).thenReturn(disponibilidadesEsperadas);

		List<Clase> disponibilidadesObtenidas =
				servicioReservaAlumno.obtenerDisponibilidadProfesor(emailProfesor);

		assertNotNull(disponibilidadesObtenidas);
		assertEquals(2, disponibilidadesObtenidas.size());
		assertThat(disponibilidadesObtenidas, containsInAnyOrder(disponibilidad1, disponibilidad2));
		verify(repositorioReservaAlumnoMock, times(1)).buscarPorProfesor(emailProfesor);
	}

	@Test
	public void obtenerDisponibilidadProfesorSinDisponibilidadesDeberiaRetornarListaVacia() {
		String emailProfesor = "profesor@unlam.com";
		List<Clase> listaVacia = Arrays.asList();

		when(repositorioReservaAlumnoMock.buscarPorProfesor(emailProfesor)).thenReturn(listaVacia);

		List<Clase> disponibilidadesObtenidas =
				servicioReservaAlumno.obtenerDisponibilidadProfesor(emailProfesor);

		assertNotNull(disponibilidadesObtenidas);
		assertEquals(0, disponibilidadesObtenidas.size());
		verify(repositorioReservaAlumnoMock, times(1)).buscarPorProfesor(emailProfesor);
	}

	@Test
	public void reservarClaseConHorarioDisponibleDeberiaReservarExitosamente() {
		String emailProfesor = "profesor@unlam.com";
		String diaSemana = "Lunes";
		String hora = "09:00";

		when(repositorioReservaAlumnoMock.buscarPorProfesorDiaHora(emailProfesor, diaSemana, hora))
				.thenReturn(disponibilidadMock);

		servicioReservaAlumno.reservarClase(emailProfesor, diaSemana, hora, alumnoMock);

		verify(disponibilidadMock, times(1)).marcarComoReservado();
		verify(disponibilidadMock, times(1)).setAlumno(alumnoMock);
		verify(repositorioReservaAlumnoMock, times(1)).guardar(disponibilidadMock);
	}

	@Test
	public void reservarClaseConHorarioNoDisponibleDeberiaLanzarExcepcion() {
		String emailProfesor = "profesor@unlam.com";
		String diaSemana = "Lunes";
		String hora = "09:00";

		when(disponibilidadMock.isDisponible()).thenReturn(false);
		when(repositorioReservaAlumnoMock.buscarPorProfesorDiaHora(emailProfesor, diaSemana, hora))
				.thenReturn(disponibilidadMock);

		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			servicioReservaAlumno.reservarClase(emailProfesor, diaSemana, hora, alumnoMock);
		});

		assertEquals("El horario no está disponible para reservar", exception.getMessage());
		verify(disponibilidadMock, never()).marcarComoReservado();
		verify(disponibilidadMock, never()).setAlumno(any());
		verify(repositorioReservaAlumnoMock, never()).guardar(any());
	}

	@Test
	public void reservarClaseConHorarioInexistenteDeberiaLanzarExcepcion() {
		String emailProfesor = "profesor@unlam.com";
		String diaSemana = "Lunes";
		String hora = "09:00";

		when(repositorioReservaAlumnoMock.buscarPorProfesorDiaHora(emailProfesor, diaSemana, hora))
				.thenReturn(null);

		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			servicioReservaAlumno.reservarClase(emailProfesor, diaSemana, hora, alumnoMock);
		});

		assertEquals("El horario no está disponible para reservar", exception.getMessage());
		verify(repositorioReservaAlumnoMock, never()).guardar(any());
	}

	@Test
	public void reservarClasePorIdConHorarioDisponibleDeberiaReservarExitosamente() {
		Long disponibilidadId = 1L;

		when(repositorioReservaAlumnoMock.buscarPorId(disponibilidadId)).thenReturn(disponibilidadMock);

		servicioReservaAlumno.reservarClasePorId(disponibilidadId, alumnoMock);

		verify(disponibilidadMock, times(1)).marcarComoReservado();
		verify(disponibilidadMock, times(1)).setAlumno(alumnoMock);
		verify(repositorioReservaAlumnoMock, times(1)).guardar(disponibilidadMock);
	}

	@Test
	public void reservarClasePorIdConHorarioNoDisponibleDeberiaLanzarExcepcion() {
		Long disponibilidadId = 1L;

		when(disponibilidadMock.isDisponible()).thenReturn(false);
		when(repositorioReservaAlumnoMock.buscarPorId(disponibilidadId)).thenReturn(disponibilidadMock);

		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			servicioReservaAlumno.reservarClasePorId(disponibilidadId, alumnoMock);
		});

		assertEquals("El horario no está disponible para reservar", exception.getMessage());
		verify(disponibilidadMock, never()).marcarComoReservado();
		verify(disponibilidadMock, never()).setAlumno(any());
		verify(repositorioReservaAlumnoMock, never()).guardar(any());
	}

	@Test
	public void reservarClasePorIdConIdInexistenteDeberiaLanzarExcepcion() {
		Long disponibilidadId = 999L;

		when(repositorioReservaAlumnoMock.buscarPorId(disponibilidadId)).thenReturn(null);

		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			servicioReservaAlumno.reservarClasePorId(disponibilidadId, alumnoMock);
		});

		assertEquals("El horario no está disponible para reservar", exception.getMessage());
		verify(repositorioReservaAlumnoMock, never()).guardar(any());
	}

	@Test
	public void obtenerDisponibilidadProfesorPorSemanaDeberiaRetornarDisponibilidadesDeTodosLosDias() {
		String emailProfesor = "profesor@unlam.com";
		LocalDate fechaInicioSemana = LocalDate.of(2025, 6, 16);

		Clase disponibilidadLunes = mock(Clase.class);
		Clase disponibilidadMartes = mock(Clase.class);
		Clase disponibilidadMiercoles = mock(Clase.class);

		when(repositorioReservaAlumnoMock.buscarPorProfesorDiaFecha(emailProfesor, "Lunes", fechaInicioSemana))
				.thenReturn(Arrays.asList(disponibilidadLunes));
		when(repositorioReservaAlumnoMock.buscarPorProfesorDiaFecha(emailProfesor, "Martes", fechaInicioSemana.plusDays(1)))
				.thenReturn(Arrays.asList(disponibilidadMartes));
		when(repositorioReservaAlumnoMock.buscarPorProfesorDiaFecha(emailProfesor, "Miércoles", fechaInicioSemana.plusDays(2)))
				.thenReturn(Arrays.asList(disponibilidadMiercoles));
		when(repositorioReservaAlumnoMock.buscarPorProfesorDiaFecha(emailProfesor, "Jueves", fechaInicioSemana.plusDays(3)))
				.thenReturn(Arrays.asList());
		when(repositorioReservaAlumnoMock.buscarPorProfesorDiaFecha(emailProfesor, "Viernes", fechaInicioSemana.plusDays(4)))
				.thenReturn(Arrays.asList());
		when(repositorioReservaAlumnoMock.buscarPorProfesorDiaFecha(emailProfesor, "Sábado", fechaInicioSemana.plusDays(5)))
				.thenReturn(Arrays.asList());
		when(repositorioReservaAlumnoMock.buscarPorProfesorDiaFecha(emailProfesor, "Domingo", fechaInicioSemana.plusDays(6)))
				.thenReturn(Arrays.asList());

		List<Clase> disponibilidadesSemanales =
				servicioReservaAlumno.obtenerDisponibilidadProfesorPorSemana(emailProfesor, fechaInicioSemana);

		assertNotNull(disponibilidadesSemanales);
		assertEquals(3, disponibilidadesSemanales.size());
		assertThat(disponibilidadesSemanales, containsInAnyOrder(disponibilidadLunes, disponibilidadMartes, disponibilidadMiercoles));

		verify(repositorioReservaAlumnoMock, times(1)).buscarPorProfesorDiaFecha(emailProfesor, "Lunes", fechaInicioSemana);
		verify(repositorioReservaAlumnoMock, times(1)).buscarPorProfesorDiaFecha(emailProfesor, "Martes", fechaInicioSemana.plusDays(1));
		verify(repositorioReservaAlumnoMock, times(1)).buscarPorProfesorDiaFecha(emailProfesor, "Miércoles", fechaInicioSemana.plusDays(2));
		verify(repositorioReservaAlumnoMock, times(1)).buscarPorProfesorDiaFecha(emailProfesor, "Jueves", fechaInicioSemana.plusDays(3));
		verify(repositorioReservaAlumnoMock, times(1)).buscarPorProfesorDiaFecha(emailProfesor, "Viernes", fechaInicioSemana.plusDays(4));
		verify(repositorioReservaAlumnoMock, times(1)).buscarPorProfesorDiaFecha(emailProfesor, "Sábado", fechaInicioSemana.plusDays(5));
		verify(repositorioReservaAlumnoMock, times(1)).buscarPorProfesorDiaFecha(emailProfesor, "Domingo", fechaInicioSemana.plusDays(6));
	}

	@Test
	public void obtenerDisponibilidadProfesorPorSemanaSinDisponibilidadesDeberiaRetornarListaVacia() {
		String emailProfesor = "profesor@unlam.com";
		LocalDate fechaInicioSemana = LocalDate.of(2024, 1, 1);

		for (int i = 0; i < 7; i++) {
			String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
			when(repositorioReservaAlumnoMock.buscarPorProfesorDiaFecha(
					emailProfesor, dias[i], fechaInicioSemana.plusDays(i)))
					.thenReturn(Arrays.asList());
		}

		List<Clase> disponibilidadesSemanales =
				servicioReservaAlumno.obtenerDisponibilidadProfesorPorSemana(emailProfesor, fechaInicioSemana);

		assertNotNull(disponibilidadesSemanales);
		assertEquals(0, disponibilidadesSemanales.size());
	}

	@Test
	public void obtenerDisponibilidadProfesorConEmailNullDeberiaLlamarRepositorio() {
		String emailProfesor = null;
		when(repositorioReservaAlumnoMock.buscarPorProfesor(emailProfesor)).thenReturn(Arrays.asList());

		List<Clase> disponibilidades = servicioReservaAlumno.obtenerDisponibilidadProfesor(emailProfesor);

		assertNotNull(disponibilidades);
		assertEquals(0, disponibilidades.size());
		verify(repositorioReservaAlumnoMock, times(1)).buscarPorProfesor(emailProfesor);
	}

	@Test
	public void reservarClaseConParametrosNullDeberiaLanzarExcepcion() {
		when(repositorioReservaAlumnoMock.buscarPorProfesorDiaHora(null, null, null))
				.thenReturn(null);

		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			servicioReservaAlumno.reservarClase(null, null, null, alumnoMock);
		});

		assertEquals("El horario no está disponible para reservar", exception.getMessage());
	}

	@Test
	public void reservarClasePorIdConIdNullDeberiaLanzarExcepcion() {
		when(repositorioReservaAlumnoMock.buscarPorId(null)).thenReturn(null);
		RuntimeException exception = assertThrows(RuntimeException.class, () -> {
			servicioReservaAlumno.reservarClasePorId(null, alumnoMock);
		});

		assertEquals("El horario no está disponible para reservar", exception.getMessage());
	}
}
