package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.entidades.EstadoDisponibilidad;
import com.tallerwebi.dominio.entidades.disponibilidadProfesor;
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

import java.time.DayOfWeek;
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

		requestMock = mock(HttpServletRequest.class);
		sessionMock = mock(HttpSession.class);
		when(requestMock.getSession()).thenReturn(sessionMock);

		servicioDisponibilidadProfesorMock = mock(ServicioDisponibilidadProfesor.class);
		controladorDisponibilidad = new ControladorDisponibilidad(servicioDisponibilidadProfesorMock);
	}

    @Test
    public void deberiaCargarCalendarioConDisponibilidadesDelProfesor() {
        // Preparación
        LocalDate fechaInicio = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        List<disponibilidadProfesor> disponibilidades = Arrays.asList(
                new disponibilidadProfesor("profesor@test.com", "Lunes", "09:00", fechaInicio, EstadoDisponibilidad.DISPONIBLE),
                new disponibilidadProfesor("profesor@test.com", "Martes", "10:00", fechaInicio.plusDays(1), EstadoDisponibilidad.DISPONIBLE)
        );

        when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
        when(servicioDisponibilidadProfesorMock.obtenerDisponibilidadProfesorPorSemana(
                eq("profesor@test.com"), any(LocalDate.class)))
                .thenReturn(disponibilidades);

        ModelAndView modelAndView = controladorDisponibilidad.irACalendarioProfesor(null, requestMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("calendario-profesor"));

        List<String> reales = (List<String>) modelAndView.getModel().get("disponibilidadesKeys");
        List<String> esperadas = Arrays.asList("Lunes-09:00", "Martes-10:00");
        assertTrue(reales.containsAll(esperadas) && esperadas.containsAll(reales));
        assertThat(modelAndView.getModel().get("emailProfesor").toString(), equalToIgnoringCase("profesor@test.com"));
        assertThat(modelAndView.getModel().get("nombreUsuario").toString(), equalToIgnoringCase("Juan"));


        assertNotNull(modelAndView.getModel().get("fechaInicioSemana"));
        assertNotNull(modelAndView.getModel().get("diasConFecha"));
        assertNotNull(modelAndView.getModel().get("fechasSemanales"));
        assertNotNull(modelAndView.getModel().get("diasConFechas"));

        verify(servicioDisponibilidadProfesorMock, times(1))
                .obtenerDisponibilidadProfesorPorSemana(eq("profesor@test.com"), any(LocalDate.class));
    }

    @Test
    public void deberiaCargarCalendarioConParametroSemanaEspecifica() {

        LocalDate fechaEspecifica = LocalDate.of(2024, 6, 10);
        String semanaParam = fechaEspecifica.toString();

        when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
        when(servicioDisponibilidadProfesorMock.obtenerDisponibilidadProfesorPorSemana(
                eq("profesor@test.com"), eq(fechaEspecifica)))
                .thenReturn(Arrays.asList());

        ModelAndView modelAndView = controladorDisponibilidad.irACalendarioProfesor(semanaParam, requestMock);
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("calendario-profesor"));
        verify(servicioDisponibilidadProfesorMock, times(1))
                .obtenerDisponibilidadProfesorPorSemana("profesor@test.com", fechaEspecifica);
    }


	@Test
	public void deberiaDenegarAccessoACalendarioSiUsuarioNoEsProfesor() {
		// Preparación
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioEstudianteMock);

		// Ejecución
		ModelAndView modelAndView = controladorDisponibilidad.irACalendarioProfesor(null, requestMock);

		// Validación
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
		verify(servicioDisponibilidadProfesorMock, never()).obtenerDisponibilidadProfesor(anyString());
	}

	@Test
	public void deberiaRedirigirALoginSiUsuarioNoEstaLogueado() {
		// Preparación
		when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

		// Ejecución
		ModelAndView modelAndView = controladorDisponibilidad.irACalendarioProfesor(null, requestMock);

		// Validación
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
		verify(servicioDisponibilidadProfesorMock, never()).obtenerDisponibilidadProfesor(anyString());
	}

	@Test
	public void deberiaToggleDisponibilidadCorrectamente() {
		// Preparación
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);

		// Ejecución
		ModelAndView modelAndView = controladorDisponibilidad.toggleDisponibilidad("Lunes", "09:00", null, null, requestMock);

		// Validación
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
        verify(servicioDisponibilidadProfesorMock, times(1))
                .toggleDisponibilidadConFecha(eq("profesor@test.com"), eq("Lunes"), eq("09:00"), any(LocalDate.class));
	}

    @Test
    public void deberiaToggleDisponibilidadConFechaEspecifica() {
        // Preparación
        when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
        LocalDate fechaEspecifica = LocalDate.of(2024, 6, 10);

        // Ejecución
        ModelAndView modelAndView = controladorDisponibilidad.toggleDisponibilidad(
                "Lunes", "09:00", fechaEspecifica.toString(), null, requestMock);

        // Validación
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
        verify(servicioDisponibilidadProfesorMock, times(1))
                .toggleDisponibilidadConFecha("profesor@test.com", "Lunes", "09:00", fechaEspecifica);
    }


	@Test
	public void deberiaRedirigirALoginEnToggleSiUsuarioNoEstaLogueado() {
		// Preparación
		when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

		// Ejecución
		ModelAndView modelAndView = controladorDisponibilidad.toggleDisponibilidad("Lunes", "09:00", null, null, requestMock);

		// Validación
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
		verify(servicioDisponibilidadProfesorMock, never()).toggleDisponibilidad(anyString(), anyString(), anyString());
	}

	@Test
	public void deberiaRedirigirAHomeEnToggleSiUsuarioNoEsProfesor() {
		// Preparación
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioEstudianteMock);

		// Ejecución
		ModelAndView modelAndView = controladorDisponibilidad.toggleDisponibilidad("Lunes", "09:00", null, null, requestMock);

		// Validación
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
		verify(servicioDisponibilidadProfesorMock, never()).toggleDisponibilidad(anyString(), anyString(), anyString());
	}

	@Test
	public void deberiaManejorParametrosNulosEnToggle() {
		// Preparación
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);

		// Ejecución
		ModelAndView modelAndView = controladorDisponibilidad.toggleDisponibilidad(null, "09:00", null, null, requestMock);

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
		ModelAndView modelAndView = controladorDisponibilidad.irACalendarioProfesor(null, requestMock);

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


		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad("Lunes", "09:00", null, null, requestMock);

		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
        verify(servicioDisponibilidadProfesorMock, times(1))
                .toggleDisponibilidadConFecha(eq("profesor@test.com"), eq("Lunes"), eq("09:00"), any(LocalDate.class));
	}

	@Test
	public void deberiaPermitirReservarMultiplesHorariosConsecutivos() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		List<disponibilidadProfesor> disponibilidadesExistentes = Arrays.asList(
				new disponibilidadProfesor("profesor@test.com", "Lunes", "09:00")
		);
		when(servicioDisponibilidadProfesorMock.obtenerDisponibilidadProfesor("profesor@test.com"))
				.thenReturn(disponibilidadesExistentes);

		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad("Lunes", "10:00", null, null, requestMock);

		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
        verify(servicioDisponibilidadProfesorMock, times(1))
                .toggleDisponibilidadConFecha(eq("profesor@test.com"), eq("Lunes"), eq("10:00"), any(LocalDate.class));
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


		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad("Martes", "10:00", null, null, requestMock);
		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.toggleDisponibilidadConFecha(eq("profesor@test.com"), eq("Martes"), eq("10:00"), any(LocalDate.class));
	}

	@Test
	public void deberiaPermitirDesmarcarHorarioExistente() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		List<disponibilidadProfesor> disponibilidadesExistentes = Arrays.asList(
				new disponibilidadProfesor("profesor@test.com", "Lunes", "09:00")
		);
		when(servicioDisponibilidadProfesorMock.obtenerDisponibilidadProfesor("profesor@test.com"))
				.thenReturn(disponibilidadesExistentes);


		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad("Lunes", "09:00", null, null, requestMock);


		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.toggleDisponibilidadConFecha(eq("profesor@test.com"), eq("Lunes"), eq("09:00"), any(LocalDate.class));
	}

	@Test
	public void deberiaManejarCaracteresEspecialesEnParametros() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad("Miércoles", "09:00", null, null, requestMock);

		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.toggleDisponibilidadConFecha(eq("profesor@test.com"), eq("Miércoles"), eq("09:00"), any(LocalDate.class));
	}

	@Test
	public void deberiaManejarParametrosConEspacios() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad(" Lunes ", " 09:00 ", null, null, requestMock);

		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.toggleDisponibilidadConFecha(eq("profesor@test.com"), eq("Lunes"), eq("09:00"), any(LocalDate.class));
	}

	@Test
	public void deberiaFuncionarConTodosLosDiasDeLaSemana() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};

		for (String dia : dias) {
			ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad(dia, "09:00", null, null, requestMock);
			assertThat("Falló para el día: " + dia,
					resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		}


		verify(servicioDisponibilidadProfesorMock, times(7))
				.toggleDisponibilidadConFecha(eq("profesor@test.com"), anyString(), eq("09:00"), any(LocalDate.class));
	}

	@Test
	public void deberiaFuncionarConTodosLosHorarios() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		String[] horas = {"06:00", "07:00", "08:00", "09:00", "10:00", "11:00",
				"12:00", "13:00", "14:00", "15:00", "16:00", "17:00",
				"18:00", "19:00", "20:00", "21:00", "22:00", "23:00"};

		for (String hora : horas) {
			ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad("Lunes", hora, null, null, requestMock);
			assertThat("Falló para la hora: " + hora,
					resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		}


		verify(servicioDisponibilidadProfesorMock, times(18))
				.toggleDisponibilidadConFecha(eq("profesor@test.com"), eq("Lunes"), anyString(), any(LocalDate.class));
	}

	@Test
	public void deberiaManejarMultiplesRequestsSimultaneos() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		ModelAndView resultado1 = controladorDisponibilidad.toggleDisponibilidad("Lunes", "09:00", null, null, requestMock);
		assertThat(resultado1.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));

		ModelAndView resultado2 = controladorDisponibilidad.toggleDisponibilidad("Lunes", "10:00", null, null, requestMock);
		assertThat(resultado2.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));


		ModelAndView resultado3 = controladorDisponibilidad.toggleDisponibilidad("Martes", "09:00", null, null, requestMock);
		assertThat(resultado3.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(3))
				.toggleDisponibilidadConFecha(eq("profesor@test.com"), anyString(), anyString(), any(LocalDate.class));
	}


	@Test
	public void deberiaManejorParametrosNulosEnReservar() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView modelAndView = controladorDisponibilidad.reservarHorario(null, "09:00", null, requestMock);


		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, never()).reservarHorario(anyString(), anyString(), anyString());
	}

	@Test
	public void deberiaManejorParametrosNulosEnDesagendar() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView modelAndView = controladorDisponibilidad.desagendarHorario("Lunes", null, null, requestMock);


		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, never()).desagendarHorario(anyString(), anyString(), anyString());
	}

	@Test
	public void deberiaManejorParametrosVaciosEnReservar() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView modelAndView = controladorDisponibilidad.reservarHorario("", "09:00", null, requestMock);


		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, never()).reservarHorario(anyString(), anyString(), anyString());
	}

	@Test
	public void deberiaManejorParametrosVaciosEnDesagendar() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView modelAndView = controladorDisponibilidad.desagendarHorario("Lunes", "", null, requestMock);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, never()).desagendarHorario(anyString(), anyString(), anyString());
	}

	@Test
	public void deberiaManejorParametrosConEspaciosEnReservar() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView modelAndView = controladorDisponibilidad.reservarHorario(" Miércoles ", " 14:00 ", null, requestMock);


		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.reservarHorario("profesor@test.com", "Miércoles", "14:00");
	}

	@Test
	public void deberiaManejorParametrosConEspaciosEnDesagendar() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView modelAndView = controladorDisponibilidad.desagendarHorario(" Jueves ", " 16:00 ", null, requestMock);


		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.desagendarHorario("profesor@test.com", "Jueves", "16:00");
	}

	@Test
	public void deberiaValidarDiasInvalidosEnReservar() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView modelAndView = controladorDisponibilidad.reservarHorario("Lunnes", "09:00", null, requestMock);


		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, never()).reservarHorario(anyString(), anyString(), anyString());
	}

	@Test
	public void deberiaValidarHorasInvalidasEnReservar() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView modelAndView = controladorDisponibilidad.reservarHorario("Lunes", "25:00", null, requestMock);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, never()).reservarHorario(anyString(), anyString(), anyString());
	}

	@Test
	public void deberiaValidarFormatoHoraIncorrectoEnDesagendar() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView modelAndView = controladorDisponibilidad.desagendarHorario("Lunes", "9:0", null, requestMock);


		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, never()).desagendarHorario(anyString(), anyString(), anyString());
	}

	@Test
	public void deberiaManejarExcepcionesEnReservarHorario() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		doThrow(new RuntimeException("Error en base de datos"))
				.when(servicioDisponibilidadProfesorMock)
				.reservarHorario("profesor@test.com", "Lunes", "09:00");


		ModelAndView modelAndView = controladorDisponibilidad.reservarHorario("Lunes", "09:00", null, requestMock);


		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.reservarHorario("profesor@test.com", "Lunes", "09:00");
	}

	@Test
	public void deberiaManejarExcepcionesEnDesagendarHorario() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		doThrow(new RuntimeException("Error en base de datos"))
				.when(servicioDisponibilidadProfesorMock)
				.desagendarHorario("profesor@test.com", "Lunes", "09:00");


		ModelAndView modelAndView = controladorDisponibilidad.desagendarHorario("Lunes", "09:00", null, requestMock);


		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.desagendarHorario("profesor@test.com", "Lunes", "09:00");
	}

	@Test
	public void deberiaCargarEstadosMapCorrectamenteEnCalendario() {

		LocalDate fechaInicio = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
		List<disponibilidadProfesor> disponibilidades = Arrays.asList(
				new disponibilidadProfesor("profesor@test.com", "Lunes", "09:00", EstadoDisponibilidad.DISPONIBLE),
				new disponibilidadProfesor("profesor@test.com", "Martes", "10:00", EstadoDisponibilidad.OCUPADO),
				new disponibilidadProfesor("profesor@test.com", "Miércoles", "11:00", EstadoDisponibilidad.RESERVADO)
		);

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		when(servicioDisponibilidadProfesorMock.obtenerDisponibilidadProfesorPorSemana(
				eq("profesor@test.com"), any(LocalDate.class)))
				.thenReturn(disponibilidades);
		ModelAndView modelAndView = controladorDisponibilidad.irACalendarioProfesor(null, requestMock);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("calendario-profesor"));

		@SuppressWarnings("unchecked")
		Map<String, String> estadosMap = (Map<String, String>) modelAndView.getModel().get("estadosMap");

		assertThat(estadosMap.get("Lunes-09:00"), equalToIgnoringCase("DISPONIBLE"));
		assertThat(estadosMap.get("Martes-10:00"), equalToIgnoringCase("OCUPADO"));
		assertThat(estadosMap.get("Miércoles-11:00"), equalToIgnoringCase("RESERVADO"));
	}


	/*
	@Test
	public void deberiaFuncionarConTodosLosEstados() {

		LocalDate fechaInicio = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		EstadoDisponibilidad[] estados = {EstadoDisponibilidad.DISPONIBLE, EstadoDisponibilidad.OCUPADO, EstadoDisponibilidad.RESERVADO};

		for (EstadoDisponibilidad estado : estados) {
			List<disponibilidadProfesor> disponibilidades = Arrays.asList(
					new disponibilidadProfesor("profesor@test.com", "Lunes", "09:00", estado)
			);

			when(servicioDisponibilidadProfesorMock.obtenerDisponibilidadProfesorPorSemana(
					eq("profesor@test.com"), any(LocalDate.class)))
					.thenReturn(Arrays.asList());


			ModelAndView modelAndView = controladorDisponibilidad.irACalendarioProfesor(null,requestMock);


			Map<String, String> estadosMap = (Map<String, String>) modelAndView.getModel().get("estadosMap");
			assertThat("Falló para el estado: " + estado,
					estadosMap.get("Lunes-09:00"), equalToIgnoringCase(estado.toString()));
		}
	}

	 */

	@Test
	public void deberiaManejarSesionNulaEnObtenerUsuario() {

		when(requestMock.getSession()).thenReturn(null);


		ModelAndView modelAndView = controladorDisponibilidad.irACalendarioProfesor(null,requestMock);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
		verify(servicioDisponibilidadProfesorMock, never()).obtenerDisponibilidadProfesor(anyString());
	}


	@Test
	public void deberiaManejarRequestNuloEnObtenerUsuario() {

		ModelAndView modelAndView = controladorDisponibilidad.irACalendarioProfesor(null, null);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
		verify(servicioDisponibilidadProfesorMock, never())
				.obtenerDisponibilidadProfesorPorSemana(anyString(), any(LocalDate.class));
	}



	@Test
	public void deberiaRechazarHorasInvalidasFueraDeLimites() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);

		ModelAndView modelAndView1 = controladorDisponibilidad.toggleDisponibilidad(
				"Lunes", "24:00", null, null, requestMock);
		ModelAndView modelAndView2 = controladorDisponibilidad.reservarHorario("Martes", "25:30",null, requestMock);
		ModelAndView modelAndView3 = controladorDisponibilidad.desagendarHorario("Miércoles", "12:60",null,requestMock);

		assertThat(modelAndView1.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		assertThat(modelAndView2.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		assertThat(modelAndView3.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));

		verify(servicioDisponibilidadProfesorMock, never())
				.toggleDisponibilidadConFecha(anyString(), anyString(), anyString(), any(LocalDate.class));
		verify(servicioDisponibilidadProfesorMock, never()).reservarHorario(anyString(), anyString(), anyString());
		verify(servicioDisponibilidadProfesorMock, never()).desagendarHorario(anyString(), anyString(), anyString());
	}


	@Test
	public void deberiaReservarEnLaSemanaCorrecta() {

		LocalDate fechaSemanasiguiente = LocalDate.of(2025, 6, 9);
		String semanaParam = fechaSemanasiguiente.toString();

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad(
				"Lunes", "10:00", fechaSemanasiguiente.toString(), null, requestMock);


		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.toggleDisponibilidadConFecha("profesor@test.com", "Lunes", "10:00", fechaSemanasiguiente);
	}

	@Test
	public void deberiaCalcularFechaCorrectaParaCadaDiaDeLaSemana() {

		LocalDate inicioDeSemana = LocalDate.of(2025, 6, 9);

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
		LocalDate[] fechasEsperadas = {
				LocalDate.of(2025, 6, 9),
				LocalDate.of(2025, 6, 10),
				LocalDate.of(2025, 6, 11),
				LocalDate.of(2025, 6, 12),
				LocalDate.of(2025, 6, 13),
				LocalDate.of(2025, 6, 14),
				LocalDate.of(2025, 6, 15)
		};

		for (int i = 0; i < dias.length; i++) {
			String dia = dias[i];
			LocalDate fechaEsperada = fechasEsperadas[i];
			ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad(
					dia, "10:00", fechaEsperada.toString(), null, requestMock);
			verify(servicioDisponibilidadProfesorMock)
					.toggleDisponibilidadConFecha("profesor@test.com", dia, "10:00", fechaEsperada);
		}
	}

	@Test
	public void deberiaUsarFechaEspecificaCuandoSeProvee() {

		LocalDate fechaEspecifica = LocalDate.of(2025, 7, 7);
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);

		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad(
				"Lunes", "09:00", fechaEspecifica.toString(), null, requestMock);


		verify(servicioDisponibilidadProfesorMock, times(1))
				.toggleDisponibilidadConFecha("profesor@test.com", "Lunes", "09:00", fechaEspecifica);
	}

	@Test
	public void deberiaUsarFechaActualCuandoNoSeProveeFechaEspecifica() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		LocalDate fechaActual = LocalDate.now();
		DayOfWeek dayOfWeek = DayOfWeek.MONDAY;
		LocalDate fechaEsperadaLunes = fechaActual.with(dayOfWeek);


		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad(
				"Lunes", "09:00", null, null, requestMock);
		verify(servicioDisponibilidadProfesorMock, times(1))
				.toggleDisponibilidadConFecha("profesor@test.com", "Lunes", "09:00", fechaEsperadaLunes);
	}

	@Test
	public void deberiaRedirigiralaMismaSemanaDespuesDeAgendar() {
		LocalDate semanaEspecifica = LocalDate.of(2025, 6, 9);
		String semanaParam = semanaEspecifica.toString();

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad(
				"Lunes", "10:00", null, semanaParam, requestMock);


		assertThat(resultado.getViewName(),
				equalToIgnoringCase("redirect:/calendario-profesor?semana=" + semanaParam));
	}

	@Test
	public void deberiaCalcularFechaCorrectaParaSemanaSiguiente() {

		LocalDate semanaSiguiente = LocalDate.now().with(DayOfWeek.MONDAY).plusWeeks(1);
		LocalDate miercolesSiguiente = semanaSiguiente.plusDays(2);
		LocalDate viernesSiguiente = semanaSiguiente.plusDays(4);

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView resultadoMiercoles = controladorDisponibilidad.toggleDisponibilidad(
				"Miércoles", "14:00", null, semanaSiguiente.toString(), requestMock);

		ModelAndView resultadoViernes = controladorDisponibilidad.toggleDisponibilidad(
				"Viernes", "16:00", null, semanaSiguiente.toString(), requestMock);


		verify(servicioDisponibilidadProfesorMock)
				.toggleDisponibilidadConFecha("profesor@test.com", "Miércoles", "14:00", miercolesSiguiente);

		verify(servicioDisponibilidadProfesorMock)
				.toggleDisponibilidadConFecha("profesor@test.com", "Viernes", "16:00", viernesSiguiente);
	}

	@Test
	public void deberiaFuncionarCorrectamenteConDomingo() {

		LocalDate inicioSemana = LocalDate.of(2025, 6, 9);
		LocalDate domingo = inicioSemana.plusDays(6);

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad(
				"Domingo", "20:00", null, inicioSemana.toString(), requestMock);


		verify(servicioDisponibilidadProfesorMock)
				.toggleDisponibilidadConFecha("profesor@test.com", "Domingo", "20:00", domingo);
	}

	@Test
	public void deberiaUsarSemanaActualSiNoSeProporcionaParametro() {

		LocalDate fechaActual = LocalDate.now();
		LocalDate inicioSemanaActual = fechaActual.with(DayOfWeek.MONDAY);
		LocalDate juevesActual = inicioSemanaActual.plusDays(3);

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad(
				"Jueves", "11:00", null, null, requestMock);


		verify(servicioDisponibilidadProfesorMock)
				.toggleDisponibilidadConFecha("profesor@test.com", "Jueves", "11:00", juevesActual);
	}

	@Test
	public void deberiaManejarSemanaInvalida() {

		String semanaInvalida = "fecha-invalida";
		LocalDate fechaActual = LocalDate.now();
		LocalDate inicioSemanaActual = fechaActual.with(DayOfWeek.MONDAY);
		LocalDate sabadoActual = inicioSemanaActual.plusDays(5);

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad(
				"Sábado", "15:00", null, semanaInvalida, requestMock);

		verify(servicioDisponibilidadProfesorMock)
				.toggleDisponibilidadConFecha("profesor@test.com", "Sábado", "15:00", sabadoActual);
	}

	@Test
	public void deberiaUsarFechaEspecificaSobreSemanaActual() {

		LocalDate fechaEspecifica = LocalDate.of(2025, 7, 24);
		LocalDate semanaActual = LocalDate.of(2025, 6, 9);

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad(
				"Jueves", "13:00", fechaEspecifica.toString(), semanaActual.toString(), requestMock);

		verify(servicioDisponibilidadProfesorMock)
				.toggleDisponibilidadConFecha("profesor@test.com", "Jueves", "13:00", fechaEspecifica);
	}


}