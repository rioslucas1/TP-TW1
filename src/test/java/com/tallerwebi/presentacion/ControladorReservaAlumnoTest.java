package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.*;
import com.tallerwebi.dominio.entidades.EstadoDisponibilidad;
import com.tallerwebi.dominio.entidades.disponibilidadProfesor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
import com.tallerwebi.presentacion.ControladorReservaAlumno;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ControladorReservaAlumnoTest {

	private ControladorReservaAlumno controladorReservaAlumno;
	private ControladorLogin controladorLogin;
	private Usuario usuarioProfesorMock;
	private Usuario usuarioEstudianteMock;
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
		usuarioEstudianteMock = mock(Usuario.class);
		when(usuarioEstudianteMock.getEmail()).thenReturn("estudiante@test.com");
		when(usuarioEstudianteMock.getRol()).thenReturn("estudiante");
		when(usuarioEstudianteMock.getNombre()).thenReturn("Juan2");

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
		// Preparación
		when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

		// Ejecución
		ModelAndView modelAndView = controladorReservaAlumno.verCalendarioProfesor("profesor@test.com", requestMock);

		// Validación
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
		verify(servicioReservaAlumnoMock, never()).obtenerDisponibilidadProfesor(anyString());
	}

	@Test
	public void deberiaCargarCalendarioDeProfesorConDisponibilidades() {

		String emailProfesor = "profesor@test.com";
		List<disponibilidadProfesor> disponibilidades = Arrays.asList(
				new disponibilidadProfesor(emailProfesor, "Lunes", "09:00", EstadoDisponibilidad.DISPONIBLE),
				new disponibilidadProfesor(emailProfesor, "Martes", "10:00", EstadoDisponibilidad.OCUPADO),
				new disponibilidadProfesor(emailProfesor, "Miércoles", "11:00", EstadoDisponibilidad.RESERVADO)
		);

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioEstudianteMock);
		when(servicioReservaAlumnoMock.obtenerDisponibilidadProfesor(emailProfesor))
				.thenReturn(disponibilidades);


		ModelAndView modelAndView = controladorReservaAlumno.verCalendarioProfesor(emailProfesor, requestMock);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("calendario-reserva"));
		assertThat(modelAndView.getModel().get("emailProfesor").toString(), equalToIgnoringCase(emailProfesor));
		assertThat(modelAndView.getModel().get("nombreUsuario").toString(), equalToIgnoringCase("Juan2"));


		Map<String, String> estadosMap = (Map<String, String>) modelAndView.getModel().get("estadosMap");
		assertThat(estadosMap.get("Lunes-09:00"), equalToIgnoringCase("DISPONIBLE"));
		assertThat(estadosMap.get("Martes-10:00"), equalToIgnoringCase("OCUPADO"));
		assertThat(estadosMap.get("Miércoles-11:00"), equalToIgnoringCase("RESERVADO"));

		verify(servicioReservaAlumnoMock, times(1)).obtenerDisponibilidadProfesor(emailProfesor);
	}

	@Test
	public void deberiaDenegarAccessoACalendarioSiUsuarioEsProfesor() {
		// Preparación
		String emailProfesor = "profesor@test.com";
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		ModelAndView modelAndView = controladorReservaAlumno.verCalendarioProfesor("profesor@test.com", requestMock);
		// Validación
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
		verify(servicioReservaAlumnoMock, times(0)).obtenerDisponibilidadProfesor(emailProfesor);
	}

	@Test
	public void deberiaPermitirReservarHorarioDisponible() {
		// Preparación

		String emailProfesor = "profesor@test.com";

		List<disponibilidadProfesor> disponibilidades = Arrays.asList(
				new disponibilidadProfesor(emailProfesor, "Lunes", "09:00", EstadoDisponibilidad.DISPONIBLE),
				new disponibilidadProfesor(emailProfesor, "Martes", "10:00", EstadoDisponibilidad.OCUPADO)
		);

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioEstudianteMock);

		// Ejecución
		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad("Lunes", "09:00", requestMock);
		ModelAndView modelAndView = controladorReservaAlumno.verCalendarioProfesor("profesor@test.com", requestMock);

		// Validación
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-alumno/" + emailProfesor));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.marcarComoReservado(emailProfesor, "Lunes", "09:00");
	}

}