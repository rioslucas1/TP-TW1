package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.dominio.servicios.ServicioDisponibilidadProfesor;
import com.tallerwebi.dominio.servicios.ServicioLogin;
import com.tallerwebi.dominio.servicios.ServicioReservaAlumno;
import com.tallerwebi.dominio.servicios.ServicioTema;
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
import static org.hamcrest.Matchers.equalTo;
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
	private static LocalDate diaLunes = LocalDate.of(2025, 6, 9);
	private static LocalDate diaMartes = LocalDate.of(2025, 6, 10);
	private static LocalDate diaMiercoles = LocalDate.of(2025, 6, 11);
	private static LocalDate diaJueves = LocalDate.of(2025, 6, 12);
	private static LocalDate diaViernes = LocalDate.of(2025, 6, 13);
	private static LocalDate diaSabado = LocalDate.of(2025, 6, 14);
	private static LocalDate diaDomingo = LocalDate.of(2025, 6, 15);
	String emailProfesor = "profesor@test.com";
	String emailAlumno = "alumno@test.com";


	@BeforeEach
	public void init(){
		datosLoginMock = new DatosLogin("test@unlam.com", "123");

		usuarioProfesorMock = mock(Profesor.class);
		when(usuarioProfesorMock.getEmail()).thenReturn(emailProfesor);
		when(usuarioProfesorMock.getNombre()).thenReturn("Juan");
		usuarioAlumnoMock = mock(Alumno.class);
		when(usuarioAlumnoMock.getEmail()).thenReturn(emailAlumno);
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
		ModelAndView modelAndView = controladorReservaAlumno.verCalendarioProfesor(emailProfesor, null, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
		verify(servicioReservaAlumnoMock, never()).obtenerDisponibilidadProfesorPorSemana(anyString(), any(LocalDate.class));
	}

	@Test
	public void deberiaCargarCalendarioConDisponibilidadesDelProfesor() {
		List<disponibilidadProfesor> disponibilidades = Arrays.asList(
				new disponibilidadProfesor(emailProfesor, "Lunes", "09:00", diaLunes, EstadoDisponibilidad.DISPONIBLE),
				new disponibilidadProfesor(emailProfesor, "Martes", "10:00", diaLunes.plusDays(1), EstadoDisponibilidad.DISPONIBLE)
		);
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioAlumnoMock);
		when(servicioReservaAlumnoMock.obtenerDisponibilidadProfesorPorSemana(eq(emailProfesor), any(LocalDate.class)))
				.thenReturn(disponibilidades);
		ModelAndView resultado = controladorReservaAlumno.verCalendarioProfesor(emailProfesor, null, requestMock);
		assertThat(resultado.getViewName(), equalToIgnoringCase("calendario-reserva"));
		List<String> disponibilidadesKeys = (List<String>) resultado.getModel().get("disponibilidadesKeys");
		assertNotNull(disponibilidadesKeys);
		assertThat(disponibilidadesKeys.size(), equalTo(2));
		assertTrue(disponibilidadesKeys.contains("Lunes-09:00"));
		assertTrue(disponibilidadesKeys.contains("Martes-10:00"));
		assertThat(resultado.getModel().get("emailProfesor"), equalTo(emailProfesor));
		assertThat(resultado.getModel().get("nombreUsuario"), equalTo("Juan2"));
		assertNotNull(resultado.getModel().get("fechaInicioSemana"));
		assertNotNull(resultado.getModel().get("diasConFecha"));
		assertNotNull(resultado.getModel().get("diasConFechas"));
		assertNotNull(resultado.getModel().get("estadosMap"));
		assertNotNull(resultado.getModel().get("idsMap"));
		verify(servicioReservaAlumnoMock, times(1))
				.obtenerDisponibilidadProfesorPorSemana(eq(emailProfesor), any(LocalDate.class));
	}

	@Test
	public void deberiaCargarCalendarioConSemanaEspecifica() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioAlumnoMock);
		when(servicioReservaAlumnoMock.obtenerDisponibilidadProfesorPorSemana(
				eq(emailProfesor), eq(diaLunes)))
				.thenReturn(Arrays.asList());
		ModelAndView modelAndView = controladorReservaAlumno.verCalendarioProfesor(emailProfesor, diaLunes.toString(), requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("calendario-reserva"));
		verify(servicioReservaAlumnoMock, times(1))
				.obtenerDisponibilidadProfesorPorSemana(emailProfesor, diaLunes);
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
		ModelAndView modelAndView = controladorReservaAlumno.verCalendarioProfesor(emailProfesor, null, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
		verify(servicioReservaAlumnoMock, never()).obtenerDisponibilidadProfesorPorSemana(anyString(), any(LocalDate.class));
	}

	@Test
	public void deberiaManejarRequestNulo() {
		ModelAndView modelAndView = controladorReservaAlumno.verCalendarioProfesor(emailProfesor, null, null);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
		verify(servicioReservaAlumnoMock, never()).obtenerDisponibilidadProfesorPorSemana(anyString(), any(LocalDate.class));
	}

	@Test
	public void deberiaManejarSemanaInvalida() {
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
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioAlumnoMock);
		ModelAndView modelAndView = controladorReservaAlumno.reservarClase(
				disponibilidadId, emailProfesor, diaLunes.toString(), requestMock);
		assertThat(modelAndView.getViewName(),
				equalToIgnoringCase("redirect:/calendario-reserva?emailProfesor=" + emailProfesor + "&semana=" + diaLunes));

		verify(servicioReservaAlumnoMock, times(1))
				.reservarClasePorId(disponibilidadId, usuarioAlumnoMock.getEmail());
	}

	@Test
	public void deberiaReservarClaseSinParametroSemana() {
		Long disponibilidadId = 1L;
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
				1L, emailProfesor, null, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
		verify(servicioReservaAlumnoMock, never()).reservarClasePorId(anyLong(), anyString());
	}

	@Test
	public void deberiaDenegarReservaAProfesor() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		ModelAndView modelAndView = controladorReservaAlumno.reservarClase(
				1L, emailProfesor, null, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
		verify(servicioReservaAlumnoMock, never()).reservarClasePorId(anyLong(), anyString());
	}

	@Test
	public void deberiaManejarExcepcionEnReservarClase() {

		Long disponibilidadId = 1L;
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
				.reservarClasePorId(disponibilidadId, emailAlumno);
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
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioAlumnoMock);
		when(servicioReservaAlumnoMock.obtenerDisponibilidadProfesorPorSemana(
				anyString(), any(LocalDate.class)))
				.thenReturn(Arrays.asList());
		ModelAndView modelAndView = controladorReservaAlumno.verCalendarioProfesor(emailProfesor, null, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("calendario-reserva"));
		verify(servicioReservaAlumnoMock, times(1))
				.obtenerDisponibilidadProfesorPorSemana(anyString(), any(LocalDate.class));
	}

	@Test
	public void deberiaReservarConSemanaConEspacios() {
		Long disponibilidadId = 1L;
		String semanaConEspacios = " 2025-06-12 ";

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioAlumnoMock);
		ModelAndView modelAndView = controladorReservaAlumno.reservarClase(
				disponibilidadId, emailProfesor, semanaConEspacios, requestMock);
		assertThat(modelAndView.getViewName(),
				equalToIgnoringCase("redirect:/calendario-reserva?emailProfesor=" + emailProfesor + "&semana=" + semanaConEspacios));

		verify(servicioReservaAlumnoMock, times(1))
				.reservarClasePorId(disponibilidadId, emailAlumno);
	}

}