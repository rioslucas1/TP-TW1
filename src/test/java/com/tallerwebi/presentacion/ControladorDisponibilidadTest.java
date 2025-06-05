package com.tallerwebi.presentacion;


import com.tallerwebi.dominio.ServicioDisponibilidadProfesor;
import com.tallerwebi.dominio.entidades.disponibilidadProfesor;
import com.tallerwebi.dominio.ServicioLogin;
import com.tallerwebi.dominio.ServicioTema;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ControladorDisponibilidadTest {

	private ControladorLogin controladorLogin;
	private Usuario usuarioProfesorMock;
	private Usuario usuarioEstudianteMock;
	private ControladorDisponibilidad controladorDisponibilidad;
	private ServicioDisponibilidadProfesor servicioDisponibilidadProfesorMock;
	private DatosLogin datosLoginMock;
	private HttpServletRequest requestMock;
	private HttpSession sessionMock;
	private ServicioLogin servicioLoginMock;
	private ServicioTema servicioTemaMock;

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

		requestMock = mock(HttpServletRequest.class);
		sessionMock = mock(HttpSession.class);
		when(requestMock.getSession()).thenReturn(sessionMock);

		servicioLoginMock = mock(ServicioLogin.class);
		servicioTemaMock = mock(ServicioTema.class);
		controladorLogin = new ControladorLogin(servicioLoginMock, servicioTemaMock);

		servicioDisponibilidadProfesorMock = mock(ServicioDisponibilidadProfesor.class);
		controladorDisponibilidad = new ControladorDisponibilidad(servicioDisponibilidadProfesorMock);
	}

	@Test
	public void deberiaCargarCalendarioConDisponibilidadesDelProfesor() {
		// Preparación
		List<disponibilidadProfesor> disponibilidades = Arrays.asList(
				new disponibilidadProfesor("profesor@test.com", "Lunes", "09:00"),
				new disponibilidadProfesor("profesor@test.com", "Martes", "10:00")
		);

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		when(servicioDisponibilidadProfesorMock.obtenerDisponibilidadProfesor("profesor@test.com"))
				.thenReturn(disponibilidades);

		// Ejecución
		ModelAndView modelAndView = controladorDisponibilidad.irACalendarioProfesor(requestMock);

		// Validación
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("calendario-profesor"));


		List<String> reales = (List<String>) modelAndView.getModel().get("disponibilidadesKeys");
		List<String> esperadas = Arrays.asList("Lunes-09:00", "Martes-10:00");
		assertTrue(reales.containsAll(esperadas) && esperadas.containsAll(reales));
		assertThat(modelAndView.getModel().get("emailProfesor").toString(), equalToIgnoringCase("profesor@test.com"));
		assertThat(modelAndView.getModel().get("nombreUsuario").toString(), equalToIgnoringCase("Juan"));
		verify(servicioDisponibilidadProfesorMock, times(1)).obtenerDisponibilidadProfesor("profesor@test.com");
	}

	@Test
	public void deberiaDenegarAccessoACalendarioSiUsuarioNoEsProfesor() {
		// Preparación
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioEstudianteMock);

		// Ejecución
		ModelAndView modelAndView = controladorDisponibilidad.irACalendarioProfesor(requestMock);

		// Validación
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
		verify(servicioDisponibilidadProfesorMock, never()).obtenerDisponibilidadProfesor(anyString());
	}

	@Test
	public void deberiaRedirigirALoginSiUsuarioNoEstaLogueado() {
		// Preparación
		when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

		// Ejecución
		ModelAndView modelAndView = controladorDisponibilidad.irACalendarioProfesor(requestMock);

		// Validación
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
		verify(servicioDisponibilidadProfesorMock, never()).obtenerDisponibilidadProfesor(anyString());
	}

	@Test
	public void deberiaToggleDisponibilidadCorrectamente() {
		// Preparación
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);

		// Ejecución
		ModelAndView modelAndView = controladorDisponibilidad.toggleDisponibilidad("Lunes", "09:00", requestMock);

		// Validación
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.toggleDisponibilidad("profesor@test.com", "Lunes", "09:00");
	}

	@Test
	public void deberiaRedirigirALoginEnToggleSiUsuarioNoEstaLogueado() {
		// Preparación
		when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

		// Ejecución
		ModelAndView modelAndView = controladorDisponibilidad.toggleDisponibilidad("Lunes", "09:00", requestMock);

		// Validación
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
		verify(servicioDisponibilidadProfesorMock, never()).toggleDisponibilidad(anyString(), anyString(), anyString());
	}

	@Test
	public void deberiaRedirigirAHomeEnToggleSiUsuarioNoEsProfesor() {
		// Preparación
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioEstudianteMock);

		// Ejecución
		ModelAndView modelAndView = controladorDisponibilidad.toggleDisponibilidad("Lunes", "09:00", requestMock);

		// Validación
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
		verify(servicioDisponibilidadProfesorMock, never()).toggleDisponibilidad(anyString(), anyString(), anyString());
	}

	@Test
	public void deberiaManejorParametrosNulosEnToggle() {
		// Preparación
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);

		// Ejecución
		ModelAndView modelAndView = controladorDisponibilidad.toggleDisponibilidad(null, "09:00", requestMock);

		// Validación
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, never()).toggleDisponibilidad(anyString(), anyString(), anyString());
	}

	@Test
	public void deberiaCargarCalendarioVacioSiNoHayDisponibilidades() {
		// Preparación
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		when(servicioDisponibilidadProfesorMock.obtenerDisponibilidadProfesor("profesor@test.com"))
				.thenReturn(Arrays.asList());

		// Ejecución
		ModelAndView modelAndView = controladorDisponibilidad.irACalendarioProfesor(requestMock);

		// Validación
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("calendario-profesor"));

		@SuppressWarnings("unchecked")
		List<String> disponibilidadesKeys = (List<String>) modelAndView.getModel().get("disponibilidadesKeys");
		assertTrue(disponibilidadesKeys.isEmpty());
	}

	@Test
	public void deberiaPermitirReservarPrimerHorario() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		when(servicioDisponibilidadProfesorMock.obtenerDisponibilidadProfesor("profesor@test.com"))
				.thenReturn(Arrays.asList());


		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad("Lunes", "09:00", requestMock);

		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.toggleDisponibilidad("profesor@test.com", "Lunes", "09:00");
	}


	@Test
	public void deberiaPermitirReservarMultiplesHorariosConsecutivos() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		List<disponibilidadProfesor> disponibilidadesExistentes = Arrays.asList(
				new disponibilidadProfesor("profesor@test.com", "Lunes", "09:00")
		);
		when(servicioDisponibilidadProfesorMock.obtenerDisponibilidadProfesor("profesor@test.com"))
				.thenReturn(disponibilidadesExistentes);

		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad("Lunes", "10:00", requestMock);

		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.toggleDisponibilidad("profesor@test.com", "Lunes", "10:00");
	}

	@Test
	public void deberiaPermitirReservarHastaCuatroHorarios() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		List<disponibilidadProfesor> disponibilidadesExistentes = Arrays.asList(
				new disponibilidadProfesor("profesor@test.com", "Lunes", "09:00"),
				new disponibilidadProfesor("profesor@test.com", "Lunes", "10:00"),
				new disponibilidadProfesor("profesor@test.com", "Martes", "09:00")
		);
		when(servicioDisponibilidadProfesorMock.obtenerDisponibilidadProfesor("profesor@test.com"))
				.thenReturn(disponibilidadesExistentes);


		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad("Martes", "10:00", requestMock);
		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.toggleDisponibilidad("profesor@test.com", "Martes", "10:00");
	}

	@Test
	public void deberiaPermitirReservarQuintoHorario() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		List<disponibilidadProfesor> disponibilidadesExistentes = Arrays.asList(
				new disponibilidadProfesor("profesor@test.com", "Lunes", "09:00"),
				new disponibilidadProfesor("profesor@test.com", "Lunes", "10:00"),
				new disponibilidadProfesor("profesor@test.com", "Martes", "09:00"),
				new disponibilidadProfesor("profesor@test.com", "Martes", "10:00")
		);
		when(servicioDisponibilidadProfesorMock.obtenerDisponibilidadProfesor("profesor@test.com"))
				.thenReturn(disponibilidadesExistentes);


		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad("Miércoles", "09:00", requestMock);


		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.toggleDisponibilidad("profesor@test.com", "Miércoles", "09:00");
	}

	@Test
	public void deberiaPermitirDesmarcarHorarioExistente() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		List<disponibilidadProfesor> disponibilidadesExistentes = Arrays.asList(
				new disponibilidadProfesor("profesor@test.com", "Lunes", "09:00")
		);
		when(servicioDisponibilidadProfesorMock.obtenerDisponibilidadProfesor("profesor@test.com"))
				.thenReturn(disponibilidadesExistentes);


		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad("Lunes", "09:00", requestMock);


		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.toggleDisponibilidad("profesor@test.com", "Lunes", "09:00");
	}

	@Test
	public void deberiaManejarCaracteresEspecialesEnParametros() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad("Miércoles", "09:00", requestMock);

		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.toggleDisponibilidad("profesor@test.com", "Miércoles", "09:00");
	}

	@Test
	public void deberiaManejarParametrosConEspacios() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad(" Lunes ", " 09:00 ", requestMock);

		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.toggleDisponibilidad("profesor@test.com", "Lunes", "09:00");
	}

	@Test
	public void deberiaFuncionarConTodosLosDiasDeLaSemana() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};

		for (String dia : dias) {
			ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad(dia, "09:00", requestMock);
			assertThat("Falló para el día: " + dia,
					resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		}

		verify(servicioDisponibilidadProfesorMock, times(7))
				.toggleDisponibilidad(eq("profesor@test.com"), anyString(), eq("09:00"));
	}

	@Test
	public void deberiaFuncionarConTodosLosHorarios() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		String[] horas = {"06:00", "07:00", "08:00", "09:00", "10:00", "11:00",
				"12:00", "13:00", "14:00", "15:00", "16:00", "17:00",
				"18:00", "19:00", "20:00", "21:00", "22:00", "23:00"};

		for (String hora : horas) {
			ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad("Lunes", hora, requestMock);
			assertThat("Falló para la hora: " + hora,
					resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		}

		verify(servicioDisponibilidadProfesorMock, times(18))
				.toggleDisponibilidad(eq("profesor@test.com"), eq("Lunes"), anyString());
	}

	@Test
	public void deberiaManejarMultiplesRequestsSimultaneos() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		ModelAndView resultado1 = controladorDisponibilidad.toggleDisponibilidad("Lunes", "09:00", requestMock);
		assertThat(resultado1.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));

		ModelAndView resultado2 = controladorDisponibilidad.toggleDisponibilidad("Lunes", "10:00", requestMock);
		assertThat(resultado2.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));


		ModelAndView resultado3 = controladorDisponibilidad.toggleDisponibilidad("Martes", "09:00", requestMock);
		assertThat(resultado3.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(3))
				.toggleDisponibilidad(eq("profesor@test.com"), anyString(), anyString());
	}

	@Test
	public void deberiaFuncionarConParametrosExactosDelHTML() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		String[] diasExactos = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
		String[] horasExactas = {"06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00",
				"13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00",
				"20:00", "21:00", "22:00", "23:00"};


		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad("Miércoles", "15:00", requestMock);
		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.toggleDisponibilidad("profesor@test.com", "Miércoles", "15:00");
	}
}