package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.entidades.EstadoDisponibilidad;
import com.tallerwebi.dominio.entidades.disponibilidadProfesor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ControladorReservaAlumnoTest {

	private ControladorReservaAlumno controladorReservaAlumno;
	private ControladorLogin controladorLogin;
	private Usuario usuarioProfesorMock;
	private Usuario usuarioAlumnoMock;
	private ControladorDisponibilidad controladorDisponibilidad;
	private ServicioDisponibilidadProfesor servicioDisponibilidadProfesorMock;
	private ServicioReservaAlumno servicioReservaAlumnoMock;
	private DatosLogin datosLoginMock;
	private HttpServletRequest requestMock;
	private HttpSession sessionMock;
	private ServicioLogin servicioLoginMock;
	private ServicioTema servicioTemaMock;
	private EstadoDisponibilidad estadoDisponibilidad;

	@BeforeEach
	public void init(){
		datosLoginMock = new DatosLogin("test@unlam.com", "123");

		// Mock para usuario profesor
		usuarioProfesorMock = mock(Usuario.class);
		when(usuarioProfesorMock.getEmail()).thenReturn("profesor@test.com");
		when(usuarioProfesorMock.getRol()).thenReturn("profesor");
		when(usuarioProfesorMock.getNombre()).thenReturn("Juan");

		// Mock para usuario estudiante
		usuarioAlumnoMock = mock(Usuario.class);
		when(usuarioAlumnoMock.getEmail()).thenReturn("estudiante@test.com");
		when(usuarioAlumnoMock.getRol()).thenReturn("estudiante");
		when(usuarioAlumnoMock.getNombre()).thenReturn("Juan2");

		requestMock = mock(HttpServletRequest.class);
		sessionMock = mock(HttpSession.class);
		when(requestMock.getSession()).thenReturn(sessionMock);

		servicioLoginMock = mock(ServicioLogin.class);
		servicioTemaMock = mock(ServicioTema.class);
		servicioReservaAlumnoMock = mock(ServicioReservaAlumno.class);
		controladorLogin = new ControladorLogin(servicioLoginMock, servicioTemaMock);

		servicioDisponibilidadProfesorMock = mock(ServicioDisponibilidadProfesor.class);
		controladorReservaAlumno = new ControladorReservaAlumno(servicioReservaAlumnoMock);
	}



	@Test
	public void deberiaRedirigirALoginSiAlumnoNoEstaLogueado() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(null);
		ModelAndView modelAndView = controladorReservaAlumno.verCalendarioProfesor("profesor@test.com", null, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
		verify(servicioReservaAlumnoMock, never()).obtenerDisponibilidadProfesor(anyString());
	}

	@Test
	public void deberiaCargarCalendarioConDisponibilidadesDelProfesor() {
		String emailProfesor = "profesor@test.com";
		LocalDate fechaInicio = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
		List<disponibilidadProfesor> disponibilidades = Arrays.asList(
				new disponibilidadProfesor(emailProfesor, "Lunes", "09:00", fechaInicio, EstadoDisponibilidad.DISPONIBLE),
				new disponibilidadProfesor(emailProfesor, "Martes", "10:00", fechaInicio.plusDays(1), EstadoDisponibilidad.DISPONIBLE)
		);
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioAlumnoMock);
		when(servicioReservaAlumnoMock.obtenerDisponibilidadProfesorPorSemana(
				eq(emailProfesor), any(LocalDate.class)))
				.thenReturn(disponibilidades);
		ModelAndView modelAndView = controladorReservaAlumno.verCalendarioProfesor(emailProfesor, null, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("calendario-reserva"));

		List<String> reales = (List<String>) modelAndView.getModel().get("disponibilidadesKeys");
		List<String> esperadas = Arrays.asList("Lunes-09:00", "Martes-10:00");
		assertTrue(reales.containsAll(esperadas) && esperadas.containsAll(reales));

		assertThat(modelAndView.getModel().get("emailProfesor").toString(), equalToIgnoringCase(emailProfesor));
		assertThat(modelAndView.getModel().get("nombreUsuario").toString(), equalToIgnoringCase("Juan2"));
		assertNotNull(modelAndView.getModel().get("fechaInicioSemana"));
		assertNotNull(modelAndView.getModel().get("diasConFecha"));
		assertNotNull(modelAndView.getModel().get("diasConFechas"));
		verify(servicioReservaAlumnoMock, times(1))
				.obtenerDisponibilidadProfesorPorSemana(eq(emailProfesor), any(LocalDate.class));
	}

	@Test
	public void deberiaCargarCalendarioConSemanaEspecifica() {
		String emailProfesor = "profesor@test.com";
		LocalDate fechaEspecifica = LocalDate.of(2024, 6, 10);
		String semanaParam = fechaEspecifica.toString();

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioAlumnoMock);
		when(servicioReservaAlumnoMock.obtenerDisponibilidadProfesorPorSemana(
				eq(emailProfesor), eq(fechaEspecifica)))
				.thenReturn(Arrays.asList());

		ModelAndView modelAndView = controladorReservaAlumno.verCalendarioProfesor(emailProfesor, semanaParam, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("calendario-reserva"));
		verify(servicioReservaAlumnoMock, times(1))
				.obtenerDisponibilidadProfesorPorSemana(emailProfesor, fechaEspecifica);
	}


	@Test
	public void deberiaDenegarAccesoAProfesor() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		ModelAndView modelAndView = controladorReservaAlumno.verCalendarioProfesor("profesor@test.com", null, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
		verify(servicioReservaAlumnoMock, never()).obtenerDisponibilidadProfesorPorSemana(anyString(), any(LocalDate.class));
	}

	@Test
	public void deberiaCargarEstadosMapCorrectamente() {
		String emailProfesor = "profesor@test.com";
		LocalDate fechaInicio = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
		List<disponibilidadProfesor> disponibilidades = Arrays.asList(
				new disponibilidadProfesor(emailProfesor, "Lunes", "09:00", EstadoDisponibilidad.DISPONIBLE),
				new disponibilidadProfesor(emailProfesor, "Martes", "10:00", EstadoDisponibilidad.OCUPADO),
				new disponibilidadProfesor(emailProfesor, "Miércoles", "11:00", EstadoDisponibilidad.RESERVADO)
		);
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioAlumnoMock);
		when(servicioReservaAlumnoMock.obtenerDisponibilidadProfesorPorSemana(
				eq(emailProfesor), any(LocalDate.class)))
				.thenReturn(disponibilidades);
		ModelAndView modelAndView = controladorReservaAlumno.verCalendarioProfesor(emailProfesor, null, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("calendario-reserva"));
		Map<String, String> estadosMap = (Map<String, String>) modelAndView.getModel().get("estadosMap");
		assertThat(estadosMap.get("Lunes-09:00"), equalToIgnoringCase("DISPONIBLE"));
		assertThat(estadosMap.get("Martes-10:00"), equalToIgnoringCase("OCUPADO"));
		assertThat(estadosMap.get("Miércoles-11:00"), equalToIgnoringCase("RESERVADO"));
	}

	@Test
	public void deberiaManejarExcepcionEnCargaDeCalendario() {

		String emailProfesor = "profesor@test.com";
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioAlumnoMock);
		when(servicioReservaAlumnoMock.obtenerDisponibilidadProfesorPorSemana(
				eq(emailProfesor), any(LocalDate.class)))
				.thenThrow(new RuntimeException("Error en base de datos"));

		ModelAndView modelAndView = controladorReservaAlumno.verCalendarioProfesor(emailProfesor, null, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("calendario-reserva"));

		List<String> disponibilidadesKeys = (List<String>) modelAndView.getModel().get("disponibilidadesKeys");
		assertTrue(disponibilidadesKeys.isEmpty());
	}

	@Test
	public void deberiaManejarSesionNula() {

		when(requestMock.getSession()).thenReturn(null);
		ModelAndView modelAndView = controladorReservaAlumno.verCalendarioProfesor("profesor@test.com", null, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
		verify(servicioReservaAlumnoMock, never()).obtenerDisponibilidadProfesorPorSemana(anyString(), any(LocalDate.class));
	}

	@Test
	public void deberiaManejarRequestNulo() {
		ModelAndView modelAndView = controladorReservaAlumno.verCalendarioProfesor("profesor@test.com", null, null);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
		verify(servicioReservaAlumnoMock, never()).obtenerDisponibilidadProfesorPorSemana(anyString(), any(LocalDate.class));
	}

	@Test
	public void deberiaManejarSemanaInvalida() {
		String emailProfesor = "profesor@test.com";
		String semanaInvalida = "fecha-invalida";
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioAlumnoMock);
		when(servicioReservaAlumnoMock.obtenerDisponibilidadProfesorPorSemana(
				eq(emailProfesor), any(LocalDate.class)))
				.thenReturn(Arrays.asList());
		ModelAndView modelAndView = controladorReservaAlumno.verCalendarioProfesor(emailProfesor, semanaInvalida, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("calendario-reserva"));
		verify(servicioReservaAlumnoMock, times(1))
				.obtenerDisponibilidadProfesorPorSemana(eq(emailProfesor), any(LocalDate.class));
	}

	@Test
	public void deberiaReservarClaseCorrectamente() {
		Long disponibilidadId = 1L;
		String emailProfesor = "profesor@test.com";
		String semanaActual = "2024-06-10";

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioAlumnoMock);
		ModelAndView modelAndView = controladorReservaAlumno.reservarClase(
				disponibilidadId, emailProfesor, semanaActual, requestMock);
		assertThat(modelAndView.getViewName(),
				equalToIgnoringCase("redirect:/calendario-reserva?emailProfesor=" + emailProfesor + "&semana=" + semanaActual));

		verify(servicioReservaAlumnoMock, times(1))
				.reservarClasePorId(disponibilidadId, usuarioAlumnoMock.getEmail());
	}

	@Test
	public void deberiaReservarClaseSinParametroSemana() {
		Long disponibilidadId = 1L;
		String emailProfesor = "profesor@test.com";

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioAlumnoMock);
		ModelAndView modelAndView = controladorReservaAlumno.reservarClase(
				disponibilidadId, emailProfesor, null, requestMock);
		assertThat(modelAndView.getViewName(),
				equalToIgnoringCase("redirect:/calendario-reserva?emailProfesor=" + emailProfesor));
		verify(servicioReservaAlumnoMock, times(1))
				.reservarClasePorId(disponibilidadId, usuarioAlumnoMock.getEmail());
	}

	@Test
	public void deberiaRedirigirALoginEnReservarSiUsuarioNoEstaLogueado() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(null);
		ModelAndView modelAndView = controladorReservaAlumno.reservarClase(
				1L, "profesor@test.com", null, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
		verify(servicioReservaAlumnoMock, never()).reservarClasePorId(anyLong(), anyString());
	}

	@Test
	public void deberiaDenegarReservaAProfesor() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		ModelAndView modelAndView = controladorReservaAlumno.reservarClase(
				1L, "profesor@test.com", null, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
		verify(servicioReservaAlumnoMock, never()).reservarClasePorId(anyLong(), anyString());
	}

	@Test
	public void deberiaManejarExcepcionEnReservarClase() {

		Long disponibilidadId = 1L;
		String emailProfesor = "profesor@test.com";


		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioAlumnoMock);
		when(usuarioAlumnoMock.getEmail()).thenReturn("alumno@test.com");
		doThrow(new RuntimeException("Error en base de datos"))
				.when(servicioReservaAlumnoMock)
				.reservarClasePorId(disponibilidadId, "alumno@test.com");
		ModelAndView modelAndView = controladorReservaAlumno.reservarClase(
				disponibilidadId, emailProfesor, null, requestMock);
		assertThat(modelAndView.getViewName(),
				equalToIgnoringCase("redirect:/calendario-reserva?emailProfesor=" + emailProfesor));

		verify(servicioReservaAlumnoMock, times(1))
				.reservarClasePorId(disponibilidadId, usuarioAlumnoMock.getEmail());
	}


	@Test
	public void deberiaManejarEmailProfesorNulo() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioAlumnoMock);
		when(servicioReservaAlumnoMock.obtenerDisponibilidadProfesorPorSemana(
				isNull(), any(LocalDate.class)))
				.thenReturn(Arrays.asList());
		ModelAndView modelAndView = controladorReservaAlumno.verCalendarioProfesor(null, null, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("calendario-reserva"));
		verify(servicioReservaAlumnoMock, times(1))
				.obtenerDisponibilidadProfesorPorSemana(isNull(), any(LocalDate.class));
	}

	@Test
	public void deberiaManejarEmailProfesorVacio() {
		String emailVacio = "";
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioAlumnoMock);
		when(servicioReservaAlumnoMock.obtenerDisponibilidadProfesorPorSemana(
				eq(emailVacio), any(LocalDate.class)))
				.thenReturn(Arrays.asList());
		ModelAndView modelAndView = controladorReservaAlumno.verCalendarioProfesor(emailVacio, null, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("calendario-reserva"));
		verify(servicioReservaAlumnoMock, times(1))
				.obtenerDisponibilidadProfesorPorSemana(eq(emailVacio), any(LocalDate.class));
	}

	@Test
	public void deberiaManejarSemanaVacia() {
		String emailProfesor = "profesor@test.com";
		String semanaVacia = "";

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioAlumnoMock);
		when(servicioReservaAlumnoMock.obtenerDisponibilidadProfesorPorSemana(
				eq(emailProfesor), any(LocalDate.class)))
				.thenReturn(Arrays.asList());
		ModelAndView modelAndView = controladorReservaAlumno.verCalendarioProfesor(emailProfesor, semanaVacia, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("calendario-reserva"));
		verify(servicioReservaAlumnoMock, times(1))
				.obtenerDisponibilidadProfesorPorSemana(eq(emailProfesor), any(LocalDate.class));
	}

	@Test
	public void deberiaPermitirAccesoAEstudiante() {
		Usuario estudiante = mock(Usuario.class);
		when(estudiante.getEmail()).thenReturn("estudiante@test.com");
		when(estudiante.getRol()).thenReturn("estudiante");
		when(estudiante.getNombre()).thenReturn("Juan Estudiante");
		when(sessionMock.getAttribute("USUARIO")).thenReturn(estudiante);
		when(servicioReservaAlumnoMock.obtenerDisponibilidadProfesorPorSemana(
				anyString(), any(LocalDate.class)))
				.thenReturn(Arrays.asList());
		ModelAndView modelAndView = controladorReservaAlumno.verCalendarioProfesor("profesor@test.com", null, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("calendario-reserva"));
		verify(servicioReservaAlumnoMock, times(1))
				.obtenerDisponibilidadProfesorPorSemana(anyString(), any(LocalDate.class));
	}

	@Test
	public void deberiaReservarConSemanaConEspacios() {
		Long disponibilidadId = 1L;
		String emailProfesor = "profesor@test.com";
		String semanaConEspacios = " 2024-06-10 ";

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioAlumnoMock);
		ModelAndView modelAndView = controladorReservaAlumno.reservarClase(
				disponibilidadId, emailProfesor, semanaConEspacios, requestMock);
		assertThat(modelAndView.getViewName(),
				equalToIgnoringCase("redirect:/calendario-reserva?emailProfesor=" + emailProfesor + "&semana=" + semanaConEspacios));

		verify(servicioReservaAlumnoMock, times(1))
				.reservarClasePorId(disponibilidadId, usuarioAlumnoMock.getEmail());
	}

	@Test
	public void deberiaFuncionarConDisponibilidadIdNegativo() {
		Long disponibilidadId = -1L;
		String emailProfesor = "profesor@test.com";

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioAlumnoMock);
		ModelAndView modelAndView = controladorReservaAlumno.reservarClase(
				disponibilidadId, emailProfesor, null, requestMock);
		assertThat(modelAndView.getViewName(),
				equalToIgnoringCase("redirect:/calendario-reserva?emailProfesor=" + emailProfesor));

		verify(servicioReservaAlumnoMock, times(1))
				.reservarClasePorId(disponibilidadId, usuarioAlumnoMock.getEmail());
	}


}