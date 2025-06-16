package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Tema;
import com.tallerwebi.dominio.servicios.ServicioTema;
import com.tallerwebi.dominio.servicios.ServicioLogin;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
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

		profesorMock = mock(Profesor.class);
		when(profesorMock.getEmail()).thenReturn("profesor@unlam.com");
		when(profesorMock.getNombre()).thenReturn("Profesor");
		when(profesorMock.getApellido()).thenReturn("Apellido");
		when(profesorMock.getPassword()).thenReturn("123456");


		temaMock = mock(Tema.class);
		when(temaMock.getId()).thenReturn(1L);
		when(temaMock.getNombre()).thenReturn("Matemáticas");

		requestMock = mock(HttpServletRequest.class);
		sessionMock = mock(HttpSession.class);
		servicioLoginMock = mock(ServicioLogin.class);
		servicioTemaMock = mock(ServicioTema.class);
		controladorLogin = new ControladorLogin(servicioLoginMock, servicioTemaMock);
	}

	@Test
	public void loginConUsuarioYPasswordInorrectosDeberiaLlevarALoginNuevamente(){
		// preparacion
		when(servicioLoginMock.consultarUsuario(anyString(), anyString())).thenReturn(null);

		// ejecucion
		ModelAndView modelAndView = controladorLogin.validarLogin(datosLoginMock, requestMock);

		// validacion
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
		datosRegistroMock = new DatosRegistro("email-invalido", "123456", "Juan", "Perez");
		ModelAndView modelAndView = controladorLogin.registrarme(datosRegistroMock);
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("nuevo-usuario"));
		assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("El formato del email es inválido"));
	}



	@Test
	public void verPerfilDeberiaDevolverVistaVerPerfil() {
		String vista = controladorLogin.verPerfil();
		assertThat(vista, equalToIgnoringCase("verPerfil"));
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
		assertThat(modelAndView.getModel().get("error").toString(), equalToIgnoringCase("El email y la contraseña son obligatorios"));
	}

	@Test
	public void cerrarSesionDeberiaInvalidarLaSesionYRedirigirAHome() {
		when(requestMock.getSession()).thenReturn(sessionMock);

		ModelAndView modelAndView = controladorLogin.cerrarSesion(requestMock);

		verify(sessionMock, times(1)).invalidate();
		assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
	}
















}