package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.dominio.servicios.ServicioDisponibilidadProfesor;
import com.tallerwebi.dominio.servicios.ServicioLogin;
import com.tallerwebi.dominio.servicios.ServicioTema;
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
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ControladorDisponibilidadTest {


	private ControladorLogin controladorLogin;
	private Profesor usuarioProfesorMock;
	private Alumno usuarioEstudianteMock;
	private ControladorDisponibilidad controladorDisponibilidad;
	private ServicioDisponibilidadProfesor servicioDisponibilidadProfesorMock;
	private DatosLogin datosLoginMock;
	private HttpServletRequest requestMock;
	private HttpSession sessionMock;
	private ServicioLogin servicioLoginMock;
	private ServicioTema servicioTemaMock;
	private EstadoDisponibilidad estadoDisponibilidad;


	private static LocalDate diaLunes = LocalDate.now().with(DayOfWeek.MONDAY);
	private static LocalDate diaMartes = LocalDate.now().with(DayOfWeek.TUESDAY);
	private static LocalDate diaMiercoles = LocalDate.now().with(DayOfWeek.WEDNESDAY);
	private static LocalDate diaJueves = LocalDate.now().with(DayOfWeek.THURSDAY);
	private static LocalDate diaViernes = LocalDate.now().with(DayOfWeek.FRIDAY);
	private static LocalDate diaSabado = LocalDate.now().with(DayOfWeek.SATURDAY);
	private static LocalDate diaDomingo = LocalDate.now().with(DayOfWeek.SUNDAY);

	@BeforeEach
	public void init(){


		datosLoginMock = new DatosLogin("test@unlam.com", "123");
		usuarioProfesorMock = mock(Profesor.class);
		when(usuarioProfesorMock.getEmail()).thenReturn("profesor@test.com");
		when(usuarioProfesorMock.getNombre()).thenReturn("Juan");
		usuarioEstudianteMock = mock(Alumno.class);
		when(usuarioEstudianteMock.getEmail()).thenReturn("estudiante@test.com");

		requestMock = mock(HttpServletRequest.class);
		sessionMock = mock(HttpSession.class);
		when(requestMock.getSession()).thenReturn(sessionMock);
		servicioDisponibilidadProfesorMock = mock(ServicioDisponibilidadProfesor.class);
		controladorDisponibilidad = new ControladorDisponibilidad(servicioDisponibilidadProfesorMock);


	}

    @Test
    public void deberiaCargarCalendarioConDisponibilidadesDelProfesor() {

        List<Clase> disponibilidades = Arrays.asList(
                new Clase(usuarioProfesorMock, "Lunes", "09:00", diaLunes, EstadoDisponibilidad.DISPONIBLE),
                new Clase(usuarioProfesorMock, "Martes", "10:00", diaMartes, EstadoDisponibilidad.DISPONIBLE)
        );

        when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		when(servicioDisponibilidadProfesorMock.obtenerDisponibilidadProfesorPorSemana(
				eq(usuarioProfesorMock), eq(diaLunes)))
				.thenReturn(disponibilidades);


        ModelAndView modelAndView = controladorDisponibilidad.irACalendarioProfesor(diaLunes.toString(), requestMock);
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("calendario-profesor"));
        List<String> disponibilidadesKeys = (List<String>) modelAndView.getModel().get("disponibilidadesKeys");
        List<String> esperadas = Arrays.asList("Lunes-09:00", "Martes-10:00");
        assertTrue(disponibilidadesKeys.containsAll(esperadas) && esperadas.containsAll(disponibilidadesKeys));
        assertThat(modelAndView.getModel().get("emailProfesor").toString(), equalToIgnoringCase("profesor@test.com"));
        assertThat(modelAndView.getModel().get("nombreUsuario").toString(), equalToIgnoringCase("Juan"));
		assertNotNull(modelAndView.getModel().get("fechaInicioSemana"));
        assertNotNull(modelAndView.getModel().get("diasConFecha"));
        assertNotNull(modelAndView.getModel().get("fechasSemanales"));
        assertNotNull(modelAndView.getModel().get("diasConFechas"));

		verify(servicioDisponibilidadProfesorMock)
				.obtenerDisponibilidadProfesorPorSemana(eq(usuarioProfesorMock), eq(diaLunes));
    }

    @Test
    public void deberiaCargarCalendarioConParametroSemanaEspecifica() {

        when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		when(servicioDisponibilidadProfesorMock.obtenerDisponibilidadProfesorPorSemana(
				usuarioProfesorMock, diaLunes))
				.thenReturn(Arrays.asList());

        ModelAndView modelAndView = controladorDisponibilidad.irACalendarioProfesor(diaLunes.toString(), requestMock);


        assertThat(modelAndView.getViewName(), equalToIgnoringCase("calendario-profesor"));
        verify(servicioDisponibilidadProfesorMock)
                .obtenerDisponibilidadProfesorPorSemana(usuarioProfesorMock, diaLunes);
    }


	@Test
	public void deberiaDenegarAccessoACalendarioSiUsuarioNoEsProfesor() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioEstudianteMock);


		ModelAndView modelAndView = controladorDisponibilidad.irACalendarioProfesor(null, requestMock);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
		verify(servicioDisponibilidadProfesorMock, never()).obtenerDisponibilidadProfesor(usuarioProfesorMock);
	}

	@Test
	public void deberiaRedirigirALoginSiUsuarioNoEstaLogueado() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(null);
		ModelAndView modelAndView = controladorDisponibilidad.irACalendarioProfesor(null, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
		verify(servicioDisponibilidadProfesorMock, never()).obtenerDisponibilidadProfesor(usuarioProfesorMock);
	}

	@Test
	public void deberiaToggleDisponibilidadCorrectamente() {

		LocalDate fechaEsperadaLunes = LocalDate.now().with(DayOfWeek.MONDAY);
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		ModelAndView modelAndView = controladorDisponibilidad.toggleDisponibilidad("Lunes", "09:00", null, null, requestMock);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock).toggleDisponibilidadConFecha(eq(usuarioProfesorMock), eq("Lunes"),
				eq("09:00"), eq(fechaEsperadaLunes)
				);
	}

    @Test
    public void deberiaToggleDisponibilidadConFechaEspecifica() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
        ModelAndView modelAndView = controladorDisponibilidad.toggleDisponibilidad(
                "Lunes", "09:00", diaLunes.toString(), null, requestMock);
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
        verify(servicioDisponibilidadProfesorMock)
                .toggleDisponibilidadConFecha(eq(usuarioProfesorMock), eq("Lunes"),
						eq("09:00"), eq(diaLunes));
    }


	@Test
	public void deberiaRedirigirALoginEnToggleSiUsuarioNoEstaLogueado() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(null);
		ModelAndView modelAndView = controladorDisponibilidad.toggleDisponibilidad("Lunes", "09:00", null, null, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
		verify(servicioDisponibilidadProfesorMock, never()).toggleDisponibilidad(any(Profesor.class), anyString(), anyString());
	}

	@Test
	public void deberiaRedirigirAHomeEnToggleSiUsuarioNoEsProfesor() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioEstudianteMock);
		ModelAndView modelAndView = controladorDisponibilidad.toggleDisponibilidad("Lunes", "09:00", null, null, requestMock);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
		verify(servicioDisponibilidadProfesorMock, never()).toggleDisponibilidad(any(Profesor.class), anyString(), anyString());
	}

	@Test
	public void deberiaManejorParametrosNulosEnToggle() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		ModelAndView modelAndView = controladorDisponibilidad.toggleDisponibilidad(null, "09:00", null, null, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, never()).toggleDisponibilidad(any(Profesor.class), anyString(), anyString());
	}


	@Test
	public void deberiaCargarCalendarioVacioSiNoHayDisponibilidades() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		when(servicioDisponibilidadProfesorMock.obtenerDisponibilidadProfesorPorSemana(
				usuarioProfesorMock, diaLunes)).thenReturn(Arrays.asList());

		ModelAndView modelAndView = controladorDisponibilidad.irACalendarioProfesor(null, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("calendario-profesor"));

		List<String> disponibilidadesKeys = (List<String>) modelAndView.getModel().get("disponibilidadesKeys");
		assertTrue(disponibilidadesKeys.isEmpty());
	}

	@Test
	public void deberiaPermitirReservarPrimerHorario() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		when(servicioDisponibilidadProfesorMock.obtenerDisponibilidadProfesor(usuarioProfesorMock))
				.thenReturn(Arrays.asList());


		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad("Lunes", "09:00", null, null, requestMock);

		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
        verify(servicioDisponibilidadProfesorMock, times(1))
                .toggleDisponibilidadConFecha(eq(usuarioProfesorMock), eq("Lunes"), eq("09:00"), any(LocalDate.class));
	}

	@Test
	public void deberiaPermitirReservarMultiplesHorariosConsecutivos() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		List<Clase> disponibilidadesExistentes = Arrays.asList(
				new Clase(usuarioProfesorMock, "Lunes", "09:00")
		);
		when(servicioDisponibilidadProfesorMock.obtenerDisponibilidadProfesor(usuarioProfesorMock))
				.thenReturn(disponibilidadesExistentes);

		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad("Lunes", "10:00", null, null, requestMock);

		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
        verify(servicioDisponibilidadProfesorMock, times(1))
                .toggleDisponibilidadConFecha(eq(usuarioProfesorMock), eq("Lunes"), eq("10:00"), any(LocalDate.class));
	}

	@Test
	public void deberiaPermitirReservarHastaCuatroHorarios() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		List<Clase> disponibilidadesExistentes = Arrays.asList(
				new Clase(usuarioProfesorMock, "Lunes", "09:00"),
				new Clase(usuarioProfesorMock, "Lunes", "10:00"),
				new Clase(usuarioProfesorMock, "Martes", "09:00")
		);
		when(servicioDisponibilidadProfesorMock.obtenerDisponibilidadProfesor(usuarioProfesorMock))
				.thenReturn(disponibilidadesExistentes);


		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad("Martes", "10:00", null, null, requestMock);
		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.toggleDisponibilidadConFecha(eq(usuarioProfesorMock), eq("Martes"), eq("10:00"), any(LocalDate.class));
	}

	@Test
	public void deberiaPermitirDesmarcarHorarioExistente() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		List<Clase> disponibilidadesExistentes = Arrays.asList(
				new Clase(usuarioProfesorMock, "Lunes", "09:00")
		);
		when(servicioDisponibilidadProfesorMock.obtenerDisponibilidadProfesor(usuarioProfesorMock))
				.thenReturn(disponibilidadesExistentes);


		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad("Lunes", "09:00", null, null, requestMock);


		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.toggleDisponibilidadConFecha(eq(usuarioProfesorMock), eq("Lunes"), eq("09:00"), any(LocalDate.class));
	}

	@Test
	public void deberiaManejarCaracteresEspecialesEnParametros() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad("Miércoles", "09:00", null, null, requestMock);

		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.toggleDisponibilidadConFecha(eq(usuarioProfesorMock), eq("Miércoles"), eq("09:00"), any(LocalDate.class));
	}

	@Test
	public void deberiaManejarParametrosConEspacios() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad(" Lunes ", " 09:00 ", null, null, requestMock);

		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));


		verify(servicioDisponibilidadProfesorMock, times(1))
		.toggleDisponibilidadConFecha(eq(usuarioProfesorMock), eq("Lunes"), eq("09:00"), eq(diaLunes));
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
				.toggleDisponibilidadConFecha(eq(usuarioProfesorMock), anyString(), eq("09:00"), any(LocalDate.class));
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
				.toggleDisponibilidadConFecha(eq(usuarioProfesorMock), eq("Lunes"), anyString(), any(LocalDate.class));
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
				.toggleDisponibilidadConFecha(eq(usuarioProfesorMock), anyString(), anyString(), any(LocalDate.class));
	}

	@Test
	public void deberiaReservarHorarioCorrectamente() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		ModelAndView resultado = controladorDisponibilidad.reservarHorario(
				"Miércoles", "14:00", null, requestMock);
		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock)
				.reservarHorario(usuarioProfesorMock, "Miércoles", "14:00");
	}

	@Test
	public void deberiaDesagendarHorarioCorrectamente() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);

		ModelAndView resultado = controladorDisponibilidad.desagendarHorario(
				"Jueves", "16:00", null, requestMock);
		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock)
				.desagendarHorario(usuarioProfesorMock, "Jueves", "16:00");
	}


	@Test
	public void deberiaManejorParametrosNulosEnReservar() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView modelAndView = controladorDisponibilidad.reservarHorario(null, "09:00", null, requestMock);


		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, never()).reservarHorario(any(Profesor.class), anyString(), anyString());
	}

	@Test
	public void deberiaManejorParametrosNulosEnDesagendar() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView modelAndView = controladorDisponibilidad.desagendarHorario("Lunes", null, null, requestMock);


		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, never()).desagendarHorario(any(Profesor.class), anyString(), anyString());
	}

	@Test
	public void deberiaManejorParametrosVaciosEnReservar() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView modelAndView = controladorDisponibilidad.reservarHorario("", "09:00", null, requestMock);


		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, never()).reservarHorario(any(Profesor.class), anyString(), anyString());
	}

	@Test
	public void deberiaManejorParametrosVaciosEnDesagendar() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView modelAndView = controladorDisponibilidad.desagendarHorario("Lunes", "", null, requestMock);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, never()).desagendarHorario(any(Profesor.class), anyString(), anyString());
	}

	@Test
	public void deberiaManejorParametrosConEspaciosEnReservar() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView modelAndView = controladorDisponibilidad.reservarHorario(" Miércoles ", " 14:00 ", null, requestMock);


		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.reservarHorario(usuarioProfesorMock, "Miércoles", "14:00");
	}

	@Test
	public void deberiaManejorParametrosConEspaciosEnDesagendar() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView modelAndView = controladorDisponibilidad.desagendarHorario(" Jueves ", " 16:00 ", null, requestMock);


		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.desagendarHorario(usuarioProfesorMock, "Jueves", "16:00");
	}

	@Test
	public void deberiaValidarDiasInvalidosEnReservar() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView modelAndView = controladorDisponibilidad.reservarHorario("Lunnes", "09:00", null, requestMock);


		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, never()).reservarHorario(any(Profesor.class), anyString(), anyString());
	}

	@Test
	public void deberiaValidarHorasInvalidasEnReservar() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView modelAndView = controladorDisponibilidad.reservarHorario("Lunes", "25:00", null, requestMock);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, never()).reservarHorario(any(Profesor.class), anyString(), anyString());
	}

	@Test
	public void deberiaValidarFormatoHoraIncorrectoEnDesagendar() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView modelAndView = controladorDisponibilidad.desagendarHorario("Lunes", "9:0", null, requestMock);


		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, never()).desagendarHorario(any(Profesor.class), anyString(), anyString());
	}

	@Test
	public void deberiaManejarExcepcionesEnReservarHorario() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		doThrow(new RuntimeException("Error en base de datos"))
				.when(servicioDisponibilidadProfesorMock)
				.reservarHorario(usuarioProfesorMock, "Lunes", "09:00");


		ModelAndView modelAndView = controladorDisponibilidad.reservarHorario("Lunes", "09:00", null, requestMock);


		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.reservarHorario(usuarioProfesorMock, "Lunes", "09:00");
	}

	@Test
	public void deberiaManejarExcepcionesEnDesagendarHorario() {

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		doThrow(new RuntimeException("Error en base de datos"))
				.when(servicioDisponibilidadProfesorMock)
				.desagendarHorario(usuarioProfesorMock, "Lunes", "09:00");


		ModelAndView modelAndView = controladorDisponibilidad.desagendarHorario("Lunes", "09:00", null, requestMock);


		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, times(1))
				.desagendarHorario(usuarioProfesorMock, "Lunes", "09:00");
	}

	@Test
	public void deberiaCargarEstadosMapCorrectamenteEnCalendario() {

		List<Clase> disponibilidades = Arrays.asList(
				new Clase(usuarioProfesorMock, "Lunes", "09:00", EstadoDisponibilidad.DISPONIBLE),
				new Clase(usuarioProfesorMock, "Martes", "10:00", EstadoDisponibilidad.OCUPADO),
				new Clase(usuarioProfesorMock, "Miércoles", "11:00", EstadoDisponibilidad.RESERVADO)
		);

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		when(servicioDisponibilidadProfesorMock.obtenerDisponibilidadProfesorPorSemana(
				eq(usuarioProfesorMock), eq(diaLunes)))
				.thenReturn(disponibilidades);
		ModelAndView modelAndView = controladorDisponibilidad.irACalendarioProfesor(null, requestMock);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("calendario-profesor"));
		Map<String, String> estadosMap = (Map<String, String>) modelAndView.getModel().get("estadosMap");
		assertThat(estadosMap.get("Lunes-09:00"), equalToIgnoringCase("DISPONIBLE"));
		assertThat(estadosMap.get("Martes-10:00"), equalToIgnoringCase("OCUPADO"));
		assertThat(estadosMap.get("Miércoles-11:00"), equalToIgnoringCase("RESERVADO"));
	}

	@Test
	public void deberiaFuncionarConTodosLosEstados() {

		LocalDate fechaInicio = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		EstadoDisponibilidad[] estados = {EstadoDisponibilidad.DISPONIBLE, EstadoDisponibilidad.OCUPADO, EstadoDisponibilidad.RESERVADO};

		for (EstadoDisponibilidad estado : estados) {
			List<Clase> disponibilidades = Arrays.asList(
					new Clase(usuarioProfesorMock, "Lunes", "09:00", fechaInicio, estado)
			);
			when(servicioDisponibilidadProfesorMock.obtenerDisponibilidadProfesorPorSemana(
					eq(usuarioProfesorMock), any(LocalDate.class)))
					.thenReturn(disponibilidades);

			ModelAndView modelAndView = controladorDisponibilidad.irACalendarioProfesor(null,requestMock);

			Map<String, String> estadosMap = (Map<String, String>) modelAndView.getModel().get("estadosMap");
			assertThat("Falló para el estado: " + estado,
					estadosMap.get("Lunes-09:00"), equalToIgnoringCase(estado.toString()));
		}
	}

	@Test
	public void deberiaManejarSesionNulaEnObtenerUsuario() {

		when(requestMock.getSession()).thenReturn(null);
		ModelAndView modelAndView = controladorDisponibilidad.irACalendarioProfesor(null,requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
		verify(servicioDisponibilidadProfesorMock, never()).obtenerDisponibilidadProfesor(usuarioProfesorMock);
	}


	@Test
	public void deberiaManejarRequestNuloEnObtenerUsuario() {

		ModelAndView modelAndView = controladorDisponibilidad.irACalendarioProfesor(null, null);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
		verify(servicioDisponibilidadProfesorMock, never())
				.obtenerDisponibilidadProfesorPorSemana(any(Profesor.class), any(LocalDate.class));
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
				.toggleDisponibilidadConFecha(any(Profesor.class), anyString(), anyString(), any(LocalDate.class));
		verify(servicioDisponibilidadProfesorMock, never()).reservarHorario(any(Profesor.class), anyString(), anyString());
		verify(servicioDisponibilidadProfesorMock, never()).desagendarHorario(any(Profesor.class), anyString(), anyString());
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
				.toggleDisponibilidadConFecha(usuarioProfesorMock, "Lunes", "10:00", fechaSemanasiguiente);
	}

	@Test
	public void deberiaCalcularFechaCorrectaParaCadaDiaDeLaSemana() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
		LocalDate[] fechasEsperadas = {
				diaLunes, diaMartes, diaMiercoles, diaJueves, diaViernes, diaSabado, diaDomingo
		};

		for (int i = 0; i < dias.length; i++) {
			String dia = dias[i];
			LocalDate fechaEsperada = fechasEsperadas[i];
			 controladorDisponibilidad.toggleDisponibilidad(
					dia, "10:00", null, diaLunes.toString(), requestMock);
			verify(servicioDisponibilidadProfesorMock)
					.toggleDisponibilidadConFecha(usuarioProfesorMock, dia, "10:00", fechaEsperada);
		}
	}

	@Test
	public void deberiaUsarFechaEspecificaCuandoSeProvee() {


		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);

		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad(
				"Lunes", "09:00", diaLunes.toString(), null, requestMock);


		verify(servicioDisponibilidadProfesorMock, times(1))
				.toggleDisponibilidadConFecha(usuarioProfesorMock, "Lunes", "09:00", diaLunes);
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
				.toggleDisponibilidadConFecha(usuarioProfesorMock, "Lunes", "09:00", fechaEsperadaLunes);
	}

	@Test
	public void deberiaRedirigiralaMismaSemanaDespuesDeAgendar() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad(
				"Lunes", "10:00", null, diaLunes.toString(), requestMock);
		assertThat(resultado.getViewName(),
				equalToIgnoringCase("redirect:/calendario-profesor?semana=" + diaLunes.toString()));
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
				.toggleDisponibilidadConFecha(usuarioProfesorMock, "Miércoles", "14:00", miercolesSiguiente);

		verify(servicioDisponibilidadProfesorMock)
				.toggleDisponibilidadConFecha(usuarioProfesorMock, "Viernes", "16:00", viernesSiguiente);
	}

	@Test
	public void deberiaFuncionarCorrectamenteConDomingo() {

		LocalDate inicioSemana = LocalDate.of(2025, 6, 9);
		LocalDate domingo = inicioSemana.plusDays(6);

		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);


		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad(
				"Domingo", "20:00", null, inicioSemana.toString(), requestMock);


		verify(servicioDisponibilidadProfesorMock)
				.toggleDisponibilidadConFecha(usuarioProfesorMock, "Domingo", "20:00", domingo);
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
				.toggleDisponibilidadConFecha(usuarioProfesorMock, "Jueves", "11:00", juevesActual);
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
				.toggleDisponibilidadConFecha(usuarioProfesorMock, "Sábado", "15:00", sabadoActual);
	}

	@Test
	public void deberiaUsarFechaEspecificaSobreSemanaActual() {

		LocalDate fechaEspecifica = LocalDate.of(2025, 7, 24);
		LocalDate semanaActual = diaLunes;
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);
		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad(
				"Jueves", "13:00", fechaEspecifica.toString(), semanaActual.toString(), requestMock);
		verify(servicioDisponibilidadProfesorMock)
				.toggleDisponibilidadConFecha(usuarioProfesorMock, "Jueves", "13:00", fechaEspecifica);
	}
	@Test
	public void deberiaRechazarDiaYHoraInvalidosCombinados() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);

		ModelAndView resultado = controladorDisponibilidad.toggleDisponibilidad("Miercolesss", "25:99", null, null, requestMock);
		assertThat(resultado.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));

		verify(servicioDisponibilidadProfesorMock, never())
				.toggleDisponibilidadConFecha(any(), any(), any(), any());
	}

	@Test
	public void deberiaIgnorarToggleConHoraNula() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);

		ModelAndView result = controladorDisponibilidad.toggleDisponibilidad("Lunes", null, null, null, requestMock);

		assertThat(result.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, never()).toggleDisponibilidadConFecha(any(), any(), any(), any());
	}

	@Test
	public void deberiaIgnorarToggleConHoraVacia() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);

		ModelAndView result = controladorDisponibilidad.toggleDisponibilidad("Lunes", "", null, null, requestMock);

		assertThat(result.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, never()).toggleDisponibilidadConFecha(any(), any(), any(), any());
	}
	@Test
	public void deberiaIgnorarToggleConHoraEspacios() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);

		ModelAndView result = controladorDisponibilidad.toggleDisponibilidad("Lunes", "   ", null, null, requestMock);

		assertThat(result.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, never()).toggleDisponibilidadConFecha(any(), any(), any(), any());
	}
	@Test
	public void deberiaRedirigirSiUsuarioEsObjetoDesconocido() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(new Object());

		ModelAndView result = controladorDisponibilidad.irACalendarioProfesor(null, requestMock);

		assertThat(result.getViewName(), equalToIgnoringCase("redirect:/login"));
		verify(servicioDisponibilidadProfesorMock, never()).obtenerDisponibilidadProfesor(any());
	}
	@Test
	public void deberiaTrimYValidarDiaYHoraConEspacios() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);

		ModelAndView result = controladorDisponibilidad.toggleDisponibilidad("   Lunes ", " 09:00   ", null, null, requestMock);

		assertThat(result.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock).toggleDisponibilidadConFecha(eq(usuarioProfesorMock), eq("Lunes"), eq("09:00"), any(LocalDate.class));
	}
	@Test
	public void deberiaIgnorarToggleConDiaEnOtroIdioma() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);

		ModelAndView result = controladorDisponibilidad.toggleDisponibilidad("Monday", "09:00", null, null, requestMock);

		assertThat(result.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		verify(servicioDisponibilidadProfesorMock, never()).toggleDisponibilidadConFecha(any(), any(), any(), any());
	}


	@Test
	public void deberiaRedirigirSiUsuarioEsNullEnToggle() {
		when(requestMock.getSession()).thenReturn(sessionMock);
		when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

		ModelAndView result = controladorDisponibilidad.toggleDisponibilidad("Lunes", "09:00", null, null, requestMock);

		assertThat(result.getViewName(), equalToIgnoringCase("redirect:/login"));
		verify(servicioDisponibilidadProfesorMock, never()).toggleDisponibilidadConFecha(any(), any(), any(), any());
	}
	@Test
	public void deberiaPermitirHorasBordeReservar() {
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioProfesorMock);

		ModelAndView result1 = controladorDisponibilidad.reservarHorario("Lunes", "00:00", null, requestMock);
		ModelAndView result2 = controladorDisponibilidad.reservarHorario("Domingo", "23:59", null, requestMock);

		assertThat(result1.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));
		assertThat(result2.getViewName(), equalToIgnoringCase("redirect:/calendario-profesor"));

		verify(servicioDisponibilidadProfesorMock).reservarHorario(usuarioProfesorMock, "Lunes", "00:00");
		verify(servicioDisponibilidadProfesorMock).reservarHorario(usuarioProfesorMock, "Domingo", "23:59");
	}

}