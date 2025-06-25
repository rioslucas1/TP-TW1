package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.dominio.servicios.ServicioTema;
import com.tallerwebi.dominio.servicios.ServicioLogin;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

public class ControladorLoginTest {

	private ControladorLogin controladorLogin;
	private Alumno alumnoMock;
	private Profesor profesorMock;
	private Tema temaMock;
	private DatosLogin datosLoginMock;
	private DatosRegistro datosRegistroMock;
	private HttpServletRequest requestMock;
	private HttpSession sessionMock;
	private ServicioLogin servicioLoginMock;
	private ServicioTema servicioTemaMock;


	@BeforeEach
	public void init(){


		datosLoginMock = new DatosLogin("alumno@unlam.com", "123456");
		datosRegistroMock = new DatosRegistro("nuevoalumno@unlam.com", "123456", "Nombre", "Apellido");

		alumnoMock = mock(Alumno.class);
		when(alumnoMock.getEmail()).thenReturn("alumno@unlam.com");
		when(alumnoMock.getNombre()).thenReturn("Nombre");
		when(alumnoMock.getApellido()).thenReturn("Apellido");
		when(alumnoMock.getPassword()).thenReturn("123456");
		when(alumnoMock.getId()).thenReturn(1L);

		profesorMock = mock(Profesor.class);
		when(profesorMock.getEmail()).thenReturn("profesor@unlam.com");
		when(profesorMock.getNombre()).thenReturn("Profesor");
		when(profesorMock.getApellido()).thenReturn("Apellido");
		when(profesorMock.getPassword()).thenReturn("123456");
		when(profesorMock.getId()).thenReturn(2L);


		temaMock = mock(Tema.class);
		when(temaMock.getId()).thenReturn(1L);
		when(temaMock.getNombre()).thenReturn("Matemáticas");

		when(profesorMock.getTema()).thenReturn(temaMock);

		requestMock = mock(HttpServletRequest.class);
		sessionMock = mock(HttpSession.class);
		servicioLoginMock = mock(ServicioLogin.class);
		servicioTemaMock = mock(ServicioTema.class);
		controladorLogin = new ControladorLogin(servicioLoginMock, servicioTemaMock);
	}

	@Test
	public void loginConUsuarioYPasswordInorrectosDeberiaLlevarALoginNuevamente(){

		when(servicioLoginMock.consultarUsuario(anyString(), anyString())).thenReturn(null);
		ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
		assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("Usuario o clave incorrecta"));
		verify(servicioLoginMock, times(1)).consultarUsuario("alumno@unlam.com", "123456");
	}

	@Test
	public void loginDeAlumnoConUsuarioYPasswordCorrectosDeberiaLLevarAHomeConRolAlumno(){
		// preparacion
		when(requestMock.getSession()).thenReturn(sessionMock);
		when(servicioLoginMock.consultarUsuario(anyString(), anyString())).thenReturn(alumnoMock);

		// ejecucion
		ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock);

		// validacion
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
		verify(sessionMock, times(1)).setAttribute("ROL", "alumno");
		verify(sessionMock, times(1)).setAttribute("USUARIO", alumnoMock);
	}

	@Test
	public void loginConProfesorCorrectosDeberiaLlevarAHomeConRolProfesor() {
		when(requestMock.getSession()).thenReturn(sessionMock);
		when(servicioLoginMock.consultarUsuario(anyString(), anyString())).thenReturn(profesorMock);
		ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
		verify(sessionMock, times(1)).setAttribute("ROL", "profesor");
		verify(sessionMock, times(1)).setAttribute("USUARIO", profesorMock);
	}


	@Test
	public void registrameSiUsuarioNoExisteDeberiaCrearUsuarioYVolverAlLogin() throws UsuarioExistente {

		ModelAndView modelAndView = controladorLogin.registrarme(datosRegistroMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
		verify(servicioLoginMock, times(1)).registrar(any(Alumno.class));
	}

	@Test
	public void registrarmeSiUsuarioExisteDeberiaVolverAFormularioYMostrarError() throws UsuarioExistente {

		doThrow(UsuarioExistente.class).when(servicioLoginMock).registrar(any(Alumno.class));
		ModelAndView modelAndView = controladorLogin.registrarme(datosRegistroMock);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
		assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("El usuario ya existe"));
	}

	@Test
	public void errorEnRegistrarmeDeberiaVolverAFormularioYMostrarError() throws UsuarioExistente {

		doThrow(RuntimeException.class).when(servicioLoginMock).registrar(any(Alumno.class));
		ModelAndView modelAndView = controladorLogin.registrarme(datosRegistroMock);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
		assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("Error al registrar el nuevo usuario"));
	}

	@Test
	public void loginConEmailVacioDeberiaMostrarError() {
		datosLoginMock = new DatosLogin("", "123");

		ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
		assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("El email es obligatorio"));
	}

	@Test
	public void loginConExpresionesComunesInvalidasDebeDarError() {
		datosLoginMock = new DatosLogin("correo-invalido", "123");

		ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
		assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("El formato del email es inválido"));
	}

	@Test
	public void registrarAlumnoConCamposVaciosDeberiaVolverAFormularioConError() {
		datosRegistroMock = new DatosRegistro("", "", "", "");
		ModelAndView modelAndView = controladorLogin.registrarme(datosRegistroMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
		assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("Todos los campos son obligatorios"));
	}

	@Test
	public void registrarAlumnoConEmailInvalidoDeberiaVolverAFormularioConError() {
		datosRegistroMock = new DatosRegistro("emailerror", "123456", "nombre", "apellido");
		ModelAndView modelAndView = controladorLogin.registrarme(datosRegistroMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
		assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("El formato del email es inválido"));
	}

	@Test
	public void nuevoUsuarioDeberiaRetornarFormularioConModelo() {
		ModelAndView modelAndView = controladorLogin.nuevoUsuario();

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
		assertThat(modelAndView.getModel().get("datosRegistro"), notNullValue());
	}

	@Test
	public void loginConPasswordVaciaDeberiaMostrarError() {
		datosLoginMock = new DatosLogin("correo@valido.com", "");

		ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
		assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("La contraseña es obligatoria"));
	}

	@Test
	public void cerrarSesionDeberiaInvalidarLaSesionYRedirigirAHome() {
		when(requestMock.getSession()).thenReturn(sessionMock);

		ModelAndView modelAndView = controladorLogin.cerrarSesion(requestMock);

		verify(sessionMock, times(1)).invalidate();
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
	}

	@Test
	public void loginConEmailYPasswordNullDeberiaMostrarError() {
		datosLoginMock = new DatosLogin(null, null);

		ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
		assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("El email y la contraseña son obligatorios"));
	}

	@Test
	public void loginConCamposEnBlancoDeberiaMostrarError() {
		datosLoginMock = new DatosLogin("   ", "   ");

		ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
		assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("El email y la contraseña son obligatorios"));
	}

	@Test
	public void loginExitosoSinRolDeberiaRedirigirIgualAPorDefecto() {
		Usuario usuarioSinRol = mock(Usuario.class);
		when(usuarioSinRol.getRol()).thenReturn(null);
		when(requestMock.getSession()).thenReturn(sessionMock);
		when(servicioLoginMock.consultarUsuario(anyString(), anyString())).thenReturn(usuarioSinRol);
	}
	@Test
	public void irAHomeConProfesorDeberiaMostrarTemaYClasesDelProfesor() {

		Long profesorId = 1L;
		when(profesorMock.getId()).thenReturn(profesorId);
		when(profesorMock.getTema()).thenReturn(temaMock);
		when(requestMock.getSession()).thenReturn(sessionMock);
		when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);

		List<Clase> clasesProfesor = Arrays.asList(
				new Clase(profesorMock, "Lunes", "09:00"),
				new Clase(profesorMock, "Miércoles", "14:00")
		);
		when(servicioLoginMock.obtenerClasesProfesor(profesorId)).thenReturn(clasesProfesor);
		ModelAndView modelAndView = controladorLogin.irAHome(requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("home"));
		assertThat(modelAndView.getModel().get("rol").toString(), equalToIgnoringCase("profesor"));
		assertEquals(modelAndView.getModel().get("temaProfesor"), temaMock);
		assertEquals(modelAndView.getModel().get("clasesProfesor"), clasesProfesor);

		verify(servicioLoginMock, times(1)).obtenerClasesProfesor(profesorId);
		verify(servicioLoginMock, never()).obtenerProfesores();
		verify(servicioLoginMock, never()).obtenerClasesAlumno(anyLong());
	}

	@Test
	public void irAHomeConAlumnoDeberiaMostrarProfesoresYClasesReservadas() {

		Long alumnoId = 2L;
		when(alumnoMock.getId()).thenReturn(alumnoId);
		when(requestMock.getSession()).thenReturn(sessionMock);
		when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
		List<Profesor> profesoresSuscritos = Arrays.asList(profesorMock);

		when(servicioLoginMock.obtenerProfesoresDeAlumno(alumnoId)).thenReturn(profesoresSuscritos);

		List<Clase> clasesReservadas = Arrays.asList(
				new Clase(profesorMock, "Martes", "10:00", EstadoDisponibilidad.RESERVADO)
		);
		when(servicioLoginMock.obtenerClasesAlumno(alumnoId)).thenReturn(clasesReservadas);

		ModelAndView modelAndView = controladorLogin.irAHome(requestMock);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("home"));
		assertThat(modelAndView.getModel().get("rol").toString(), equalToIgnoringCase("alumno"));
		assertEquals(modelAndView.getModel().get("listaProfesores"), profesoresSuscritos);
		assertEquals(modelAndView.getModel().get("clasesReservadas"), clasesReservadas);

		verify(servicioLoginMock, times(1)).obtenerProfesoresDeAlumno(alumnoId);
		verify(servicioLoginMock, times(1)).obtenerClasesAlumno(alumnoId);
		verify(servicioLoginMock, never()).obtenerClasesProfesor(anyLong());
	}

	@Test
	public void irAHomeConUsuarioNuloDeberiaRetornarHomeVacio() {

		when(requestMock.getSession()).thenReturn(sessionMock);
		when(sessionMock.getAttribute("USUARIO")).thenReturn(null);
		ModelAndView modelAndView = controladorLogin.irAHome(requestMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("home"));
		assertNull(modelAndView.getModel().get("nombreUsuario"));
		assertNull(modelAndView.getModel().get("rol"));

		verify(servicioLoginMock, never()).obtenerClasesProfesor(anyLong());
		verify(servicioLoginMock, never()).obtenerClasesAlumno(anyLong());
		verify(servicioLoginMock, never()).obtenerProfesores();
	}


	@Test
	public void definirRolConAlumnoDeberiaRetornarAlumno() {
		when(requestMock.getSession()).thenReturn(sessionMock);
		when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
		ModelAndView modelAndView = controladorLogin.irAHome(requestMock);
		assertThat(modelAndView.getModel().get("rol").toString(), equalToIgnoringCase("alumno"));
	}

	@Test
	public void definirRolConUsuarioDesconocidoDeberiaRetornarUsuario() {
		// Given
		Usuario usuarioGenerico = mock(Usuario.class);
		when(usuarioGenerico.getNombre()).thenReturn("Usuario");
		when(requestMock.getSession()).thenReturn(sessionMock);
		when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioGenerico);
		ModelAndView modelAndView = controladorLogin.irAHome(requestMock);
		assertThat(modelAndView.getModel().get("rol").toString(), equalToIgnoringCase("usuario"));
	}

	@Test
	public void obtenerClasesProfesorDeberiaLlamarAlServicioConIdCorrecto() {

		Long profesorId = 5L;
		when(profesorMock.getId()).thenReturn(profesorId);
		when(requestMock.getSession()).thenReturn(sessionMock);
		when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
		when(profesorMock.getTema()).thenReturn(temaMock);

		List<Clase> clasesEsperadas = Arrays.asList();
		when(servicioLoginMock.obtenerClasesProfesor(profesorId)).thenReturn(clasesEsperadas);

		controladorLogin.irAHome(requestMock);

		verify(servicioLoginMock, times(1)).obtenerClasesProfesor(profesorId);
	}

	@Test
	public void obtenerClasesAlumnoDeberiaLlamarAlServicioConIdCorrecto() {

		Long alumnoId = 3L;
		when(alumnoMock.getId()).thenReturn(alumnoId);
		when(requestMock.getSession()).thenReturn(sessionMock);
		when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);

		List<Clase> clasesEsperadas = Arrays.asList();
		when(servicioLoginMock.obtenerClasesAlumno(alumnoId)).thenReturn(clasesEsperadas);
		when(servicioLoginMock.obtenerProfesores()).thenReturn(Arrays.asList());


		controladorLogin.irAHome(requestMock);

		verify(servicioLoginMock, times(1)).obtenerClasesAlumno(alumnoId);
	}

	@Test
	public void irAHomeConProfesorSinClasesDeberiaRetornarListaVacia() {

		Long profesorId = 1L;
		when(profesorMock.getId()).thenReturn(profesorId);
		when(profesorMock.getTema()).thenReturn(temaMock);
		when(requestMock.getSession()).thenReturn(sessionMock);
		when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
		List<Clase> clasesVacias = Arrays.asList();
		when(servicioLoginMock.obtenerClasesProfesor(profesorId)).thenReturn(clasesVacias);
		ModelAndView modelAndView = controladorLogin.irAHome(requestMock);
		assertEquals(modelAndView.getModel().get("clasesProfesor"), clasesVacias);
		assertEquals(0, ((List<?>) modelAndView.getModel().get("clasesProfesor")).size());
	}

	@Test
	public void irAHomeConAlumnoSinClasesReservadasDeberiaRetornarListaVacia() {

		Long alumnoId = 2L;
		when(alumnoMock.getId()).thenReturn(alumnoId);
		when(requestMock.getSession()).thenReturn(sessionMock);
		when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);

		when(servicioLoginMock.obtenerProfesores()).thenReturn(Arrays.asList(profesorMock));

		List<Clase> clasesVacias = Arrays.asList();
		when(servicioLoginMock.obtenerClasesAlumno(alumnoId)).thenReturn(clasesVacias);
		ModelAndView modelAndView = controladorLogin.irAHome(requestMock);
		assertEquals(modelAndView.getModel().get("clasesReservadas"), clasesVacias);
		assertEquals(0, ((List<?>) modelAndView.getModel().get("clasesReservadas")).size());
	}

	@Test
	public void irAHomeConAlumnoSinProfesoresSuscritosDeberiaRetornarListaVacia() {
		Long alumnoId = 2L;
		when(alumnoMock.getId()).thenReturn(alumnoId);
		when(requestMock.getSession()).thenReturn(sessionMock);
		when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);

		List<Profesor> profesoresVacios = Arrays.asList();
		when(servicioLoginMock.obtenerProfesoresDeAlumno(alumnoId)).thenReturn(profesoresVacios);

		List<Clase> clasesVacias = Arrays.asList();
		when(servicioLoginMock.obtenerClasesAlumno(alumnoId)).thenReturn(clasesVacias);

		ModelAndView modelAndView = controladorLogin.irAHome(requestMock);

		assertEquals(modelAndView.getModel().get("listaProfesores"), profesoresVacios);
		assertEquals(0, ((List<?>) modelAndView.getModel().get("listaProfesores")).size());
		assertEquals(modelAndView.getModel().get("clasesReservadas"), clasesVacias);
		assertEquals(0, ((List<?>) modelAndView.getModel().get("clasesReservadas")).size());
	}

	@Test
	public void irALoginDeberiaRetornarVistaLoginConModelo() {
		ModelAndView modelAndView = controladorLogin.irALogin();

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("login"));
		assertThat(modelAndView.getModel().get("datosLogin"), notNullValue());
	}

	@Test
	public void inicioDeberiaRedirigirALogin() {
		ModelAndView modelAndView = controladorLogin.inicio();

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
	}

	@Test
	public void mostrarFormularioProfesorDeberiaRetornarVistaConTemasYModelo() {
		List<Tema> temasMock = Arrays.asList(temaMock);
		when(servicioTemaMock.obtenerTodos()).thenReturn(temasMock);

		ModelAndView modelAndView = controladorLogin.mostrarFormularioProfesor();

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("registrar-profesor"));
		assertThat(modelAndView.getModel().get("datosRegistro"), notNullValue());
		assertThat(modelAndView.getModel().get("temas"), notNullValue());
		assertEquals(modelAndView.getModel().get("temas"), temasMock);
	}

	@Test
	public void procesarRegistroProfesorConDatosValidosDeberiaRegistrarYRedirigirALogin() throws UsuarioExistente {
		DatosRegistroProfesor datosProfesor = new DatosRegistroProfesor("Profesor", "apellido", "profesor@test.com", "12345", 1L);
		Long temaId = 1L;

		when(servicioTemaMock.obtenerPorId(temaId)).thenReturn(temaMock);

		ModelAndView modelAndView = controladorLogin.procesarRegistroProfesor(datosProfesor, temaId);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
		verify(servicioLoginMock, times(1)).registrar(any(Profesor.class));
		verify(servicioTemaMock, times(1)).obtenerPorId(temaId);
	}

	@Test
	public void procesarRegistroProfesorConCamposVaciosDeberiaVolverAFormularioConError() {
		DatosRegistroProfesor datosProfesor = new DatosRegistroProfesor("", "", "", "", null);
		List<Tema> temasMock = Arrays.asList(temaMock);
		when(servicioTemaMock.obtenerTodos()).thenReturn(temasMock);

		ModelAndView modelAndView = controladorLogin.procesarRegistroProfesor(datosProfesor, null);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("registrar-profesor"));
		assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("Todos los campos son obligatorios"));
		assertThat(modelAndView.getModel().get("temas"), notNullValue());
	}

	@Test
	public void procesarRegistroProfesorConEmailInvalidoDeberiaVolverAFormularioConError() {
		DatosRegistroProfesor datosProfesor = new DatosRegistroProfesor("Profesor", "apellido", "profetest.com", "12345", 1L);
		datosProfesor.setTemaId(1L);
		List<Tema> temasMock = Arrays.asList(temaMock);
		when(servicioTemaMock.obtenerTodos()).thenReturn(temasMock);

		ModelAndView modelAndView = controladorLogin.procesarRegistroProfesor(datosProfesor, 1L);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("registrar-profesor"));
		assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("El formato del email es inválido"));
		assertThat(modelAndView.getModel().get("temas"), notNullValue());
	}

	@Test
	public void procesarRegistroProfesorConUsuarioExistenteDeberiaVolverAFormularioConError() throws UsuarioExistente {
		DatosRegistroProfesor datosProfesor = new DatosRegistroProfesor("Profesor", "apellido", "profesor@test.com", "12345", 1L);
		datosProfesor.setTemaId(1L);
		List<Tema> temasMock = Arrays.asList(temaMock);

		when(servicioTemaMock.obtenerTodos()).thenReturn(temasMock);
		when(servicioTemaMock.obtenerPorId(1L)).thenReturn(temaMock);
		doThrow(UsuarioExistente.class).when(servicioLoginMock).registrar(any(Profesor.class));

		ModelAndView modelAndView = controladorLogin.procesarRegistroProfesor(datosProfesor, 1L);

		assertThat(modelAndView.getViewName(), equalToIgnoringCase("registrar-profesor"));
		assertThat(modelAndView.getModel().get("temas"), notNullValue());
	}


	@Test
	public void irAHomeConProfesorDeberiaLimitarClasesACinco() {
		Long profesorId = 1L;
		when(profesorMock.getId()).thenReturn(profesorId);
		when(profesorMock.getTema()).thenReturn(temaMock);
		when(requestMock.getSession()).thenReturn(sessionMock);
		when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);
		List<Clase> muchasClases = Arrays.asList(
				new Clase(profesorMock, "Lunes", "09:00"),
				new Clase(profesorMock, "Martes", "10:00"),
				new Clase(profesorMock, "Miércoles", "11:00"),
				new Clase(profesorMock, "Jueves", "12:00"),
				new Clase(profesorMock, "Viernes", "13:00"),
				new Clase(profesorMock, "Sábado", "14:00"),
				new Clase(profesorMock, "Domingo", "15:00")
		);
		when(servicioLoginMock.obtenerClasesProfesor(profesorId)).thenReturn(muchasClases);

		ModelAndView modelAndView = controladorLogin.irAHome(requestMock);

		List<Clase> clasesEnModelo = (List<Clase>) modelAndView.getModel().get("clasesProfesor");
		assertEquals(5, clasesEnModelo.size());
	}

	@Test
	public void irAHomeConAlumnoDeberiaLimitarClasesACinco() {
		Long alumnoId = 2L;
		when(alumnoMock.getId()).thenReturn(alumnoId);
		when(requestMock.getSession()).thenReturn(sessionMock);
		when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);

		when(servicioLoginMock.obtenerProfesoresDeAlumno(alumnoId)).thenReturn(Arrays.asList(profesorMock));

		List<Clase> muchasClases = Arrays.asList(
				new Clase(profesorMock, "Lunes", "09:00"),
				new Clase(profesorMock, "Martes", "10:00"),
				new Clase(profesorMock, "Miércoles", "11:00"),
				new Clase(profesorMock, "Jueves", "12:00"),
				new Clase(profesorMock, "Viernes", "13:00"),
				new Clase(profesorMock, "Sábado", "14:00"),
				new Clase(profesorMock, "Domingo", "15:00")
		);
		when(servicioLoginMock.obtenerClasesAlumno(alumnoId)).thenReturn(muchasClases);

		ModelAndView modelAndView = controladorLogin.irAHome(requestMock);

		List<Clase> clasesEnModelo = (List<Clase>) modelAndView.getModel().get("clasesReservadas");
		assertEquals(5, clasesEnModelo.size());
	}


}