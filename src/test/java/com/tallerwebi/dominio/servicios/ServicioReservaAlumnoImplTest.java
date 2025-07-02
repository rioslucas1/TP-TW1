package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.RepositorioReservaAlumno;
import com.tallerwebi.dominio.RepositorioUsuario;
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
	private RepositorioUsuario repositorioUsuarioMock;
	private Alumno alumnoMock;
	private Profesor profesorMock;
	private Usuario usuarioMock;
	private Clase disponibilidadMock;

	@BeforeEach
	public void init() {
		repositorioReservaAlumnoMock = mock(RepositorioReservaAlumno.class);
		repositorioUsuarioMock = mock(RepositorioUsuario.class);

		servicioReservaAlumno = new ServicioReservaAlumnoImpl(repositorioReservaAlumnoMock, repositorioUsuarioMock);

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


	@Test
	public void estaSuscritoAProfesorConAlumnoSuscritoDeberiaRetornarTrue() {
		Long alumnoId = 1L;
		String emailProfesor = "profesor@unlam.com";

		Profesor profesor = mock(Profesor.class);
		when(profesor.getEmail()).thenReturn(emailProfesor);

		List<Profesor> profesores = Arrays.asList(profesor);
		when(alumnoMock.getProfesores()).thenReturn(profesores);

		when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(alumnoMock);

		boolean resultado = servicioReservaAlumno.estaSuscritoAProfesor(alumnoId, emailProfesor);

		assertTrue(resultado);
		verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
	}

	@Test
	public void estaSuscritoAProfesorConAlumnoNoSuscritoDeberiaRetornarFalse() {
		Long alumnoId = 1L;
		String emailProfesor = "profesor@unlam.com";
		String emailProfesorDiferente = "otro@unlam.com";

		Profesor profesor = mock(Profesor.class);
		when(profesor.getEmail()).thenReturn(emailProfesorDiferente);

		List<Profesor> profesores = Arrays.asList(profesor);
		when(alumnoMock.getProfesores()).thenReturn(profesores);

		when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(alumnoMock);

		boolean resultado = servicioReservaAlumno.estaSuscritoAProfesor(alumnoId, emailProfesor);

		assertFalse(resultado);
		verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
	}

	@Test
	public void estaSuscritoAProfesorConAlumnoSinProfesoresDeberiaRetornarFalse() {
		Long alumnoId = 1L;
		String emailProfesor = "profesor@unlam.com";


		List<Profesor> profesoresVacia = Arrays.asList();
		when(alumnoMock.getProfesores()).thenReturn(profesoresVacia);


		when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(alumnoMock);

		boolean resultado = servicioReservaAlumno.estaSuscritoAProfesor(alumnoId, emailProfesor);

		assertFalse(resultado);
		verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
	}

	@Test
	public void estaSuscritoAProfesorConUsuarioQueNoEsAlumnoDeberiaRetornarFalse() {
		Long usuarioId = 1L;
		String emailProfesor = "profesor@unlam.com";

		Usuario usuarioNoAlumno = mock(Profesor.class);
		when(repositorioUsuarioMock.buscarPorId(usuarioId)).thenReturn(usuarioNoAlumno);

		boolean resultado = servicioReservaAlumno.estaSuscritoAProfesor(usuarioId, emailProfesor);

		assertFalse(resultado);
		verify(repositorioUsuarioMock, times(1)).buscarPorId(usuarioId);
	}

	@Test
	public void estaSuscritoAProfesorConUsuarioNuloDeberiaRetornarFalse() {
		Long usuarioId = 999L;
		String emailProfesor = "profesor@unlam.com";

		when(repositorioUsuarioMock.buscarPorId(usuarioId)).thenReturn(null);

		boolean resultado = servicioReservaAlumno.estaSuscritoAProfesor(usuarioId, emailProfesor);

		assertFalse(resultado);
		verify(repositorioUsuarioMock, times(1)).buscarPorId(usuarioId);
	}

	@Test
	public void estaSuscritoAProfesorConVariosProfesoresTodosEmailsDiferentesDeberiaRetornarFalse() {
		Long alumnoId = 1L;
		String emailProfesorBuscado = "profesor@unlam.com";

		Profesor profesor1 = mock(Profesor.class);
		when(profesor1.getEmail()).thenReturn("profesor1@unlam.com");

		Profesor profesor2 = mock(Profesor.class);
		when(profesor2.getEmail()).thenReturn("profesor2@unlam.com");

		Profesor profesor3 = mock(Profesor.class);
		when(profesor3.getEmail()).thenReturn("profesor3@unlam.com");

		List<Profesor> profesores = Arrays.asList(profesor1, profesor2, profesor3);
		when(alumnoMock.getProfesores()).thenReturn(profesores);

		when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(alumnoMock);

		boolean resultado = servicioReservaAlumno.estaSuscritoAProfesor(alumnoId, emailProfesorBuscado);

		assertFalse(resultado);
		verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
	}

	@Test
	public void estaSuscritoAProfesorConVariosProfesoresTodosYUnoCoincideDeberiaRetornarTrue() {
		Long alumnoId = 1L;
		String emailProfesorBuscado = "profesor@unlam.com";

		Profesor profesor1 = mock(Profesor.class);
		when(profesor1.getEmail()).thenReturn("profesor1@unlam.com");

		Profesor profesor2 = mock(Profesor.class);
		when(profesor2.getEmail()).thenReturn(emailProfesorBuscado);

		Profesor profesor3 = mock(Profesor.class);
		when(profesor3.getEmail()).thenReturn("profesor3@unlam.com");

		List<Profesor> profesores = Arrays.asList(profesor1, profesor2, profesor3);
		when(alumnoMock.getProfesores()).thenReturn(profesores);

		when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(alumnoMock);

		boolean resultado = servicioReservaAlumno.estaSuscritoAProfesor(alumnoId, emailProfesorBuscado);

		assertTrue(resultado);
		verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
	}

	@Test
	public void obtenerClasesPorProfesorYAlumnoDeberiaRetornarListaDeClasesCuandoExisten() {

		String emailProfesor = "profesor@unlam.com";
		String emailAlumno = "alumno@unlam.com";

		Clase clase1 = mock(Clase.class);
		when(clase1.getEmailProfesor()).thenReturn(emailProfesor);
		when(clase1.getMailAlumno()).thenReturn(emailAlumno);

		Clase clase2 = mock(Clase.class);
		when(clase2.getEmailProfesor()).thenReturn(emailProfesor);
		when(clase2.getMailAlumno()).thenReturn(emailAlumno);

		List<Clase> clasesEsperadas = Arrays.asList(clase1, clase2);
		when(repositorioReservaAlumnoMock.buscarClasesPorProfesorYAlumno(emailProfesor, emailAlumno))
				.thenReturn(clasesEsperadas);


		List<Clase> clasesObtenidas =
				servicioReservaAlumno.obtenerClasesPorProfesorYAlumno(emailProfesor, emailAlumno);


		assertNotNull(clasesObtenidas);
		assertEquals(2, clasesObtenidas.size());
		assertThat(clasesObtenidas, containsInAnyOrder(clase1, clase2));
		verify(repositorioReservaAlumnoMock, times(1)).buscarClasesPorProfesorYAlumno(emailProfesor, emailAlumno);
	}


	@Test
	public void obtenerClasesPorProfesorYAlumnoSinClasesDeberiaRetornarListaVacia() {

		String emailProfesor = "profesor@unlam.com";
		String emailAlumno = "alumno@unlam.com";

		List<Clase> listaVacia = Arrays.asList();
		when(repositorioReservaAlumnoMock.buscarClasesPorProfesorYAlumno(emailProfesor, emailAlumno))
				.thenReturn(listaVacia);


		List<Clase> clasesObtenidas =
				servicioReservaAlumno.obtenerClasesPorProfesorYAlumno(emailProfesor, emailAlumno);


		assertNotNull(clasesObtenidas);
		assertTrue(clasesObtenidas.isEmpty());
		verify(repositorioReservaAlumnoMock, times(1)).buscarClasesPorProfesorYAlumno(emailProfesor, emailAlumno);
	}


	@Test
	public void obtenerClasesPorProfesorYAlumnoConEmailProfesorNuloDeberiaRetornarListaVacia() {

		String emailProfesor = null;
		String emailAlumno = "alumno@unlam.com";

		when(repositorioReservaAlumnoMock.buscarClasesPorProfesorYAlumno(emailProfesor, emailAlumno))
				.thenReturn(Arrays.asList());


		List<Clase> clasesObtenidas =
				servicioReservaAlumno.obtenerClasesPorProfesorYAlumno(emailProfesor, emailAlumno);

		assertNotNull(clasesObtenidas);
		assertTrue(clasesObtenidas.isEmpty());
		verify(repositorioReservaAlumnoMock, times(1)).buscarClasesPorProfesorYAlumno(emailProfesor, emailAlumno);
	}

	@Test
	public void obtenerClasesPorProfesorYAlumnoConEmailAlumnoNuloDeberiaRetornarListaVacia() {

		String emailProfesor = "profesor@unlam.com";
		String emailAlumno = null;

		when(repositorioReservaAlumnoMock.buscarClasesPorProfesorYAlumno(emailProfesor, emailAlumno))
				.thenReturn(Arrays.asList());

		List<Clase> clasesObtenidas =
				servicioReservaAlumno.obtenerClasesPorProfesorYAlumno(emailProfesor, emailAlumno);


		assertNotNull(clasesObtenidas);
		assertTrue(clasesObtenidas.isEmpty());
		verify(repositorioReservaAlumnoMock, times(1)).buscarClasesPorProfesorYAlumno(emailProfesor, emailAlumno);
	}

}
