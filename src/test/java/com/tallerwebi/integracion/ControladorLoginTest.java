package com.tallerwebi.integracion;

import com.tallerwebi.dominio.servicios.ServicioLogin;
import com.tallerwebi.dominio.servicios.ServicioTema;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Tema;
import com.tallerwebi.dominio.entidades.Clase;
import com.tallerwebi.presentacion.DatosLogin;
import com.tallerwebi.presentacion.DatosRegistro;
import com.tallerwebi.presentacion.DatosRegistroProfesor;
import com.tallerwebi.presentacion.ControladorLogin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Arrays;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SpringWebTestConfig.class, HibernateTestConfig.class, ControladorLoginTest.TestConfig.class})
public class ControladorLoginTest {

	// Datos de prueba
	private Alumno alumnoReal;
	private Profesor profesorReal;
	private Tema temaReal;
	private List<Clase> clasesMock;
	private List<Tema> temasMock;

	@Autowired
	private WebApplicationContext wac;
	private MockMvc mockMvc;

	// Inyección de los servicios mockeados
	@Autowired
	private ServicioLogin servicioLogin;

	@Autowired
	private ServicioTema servicioTema;

	// Configuración de los mocks como beans de Spring
	@Configuration
	static class TestConfig {
		@Bean
		@Primary
		public ServicioLogin servicioLoginMock() {
			return mock(ServicioLogin.class);
		}

		@Bean
		@Primary
		public ServicioTema servicioTemaMock() {
			return mock(ServicioTema.class);
		}
	}

	@BeforeEach
	public void init() {
		// Crear instancias reales para las pruebas
		alumnoReal = new Alumno();
		alumnoReal.setId(1L);
		alumnoReal.setEmail("alumno@unlam.com");
		alumnoReal.setNombre("Alumno Test");
		alumnoReal.setPassword("password123");

		profesorReal = new Profesor();
		profesorReal.setId(2L);
		profesorReal.setEmail("profesor@unlam.com");
		profesorReal.setNombre("Profesor Test");
		profesorReal.setPassword("password123");

		temaReal = new Tema();
		temaReal.setId(1L);
		temaReal.setNombre("Matemáticas");

		profesorReal.setTema(temaReal);

		// Crear mocks para las clases
		Clase clase1 = new Clase();
		clase1.setId(1L);
		clase1.setDiaSemana("Lunes");
		clase1.setHora("10:00");

		Clase clase2 = new Clase();
		clase2.setId(2L);
		clase2.setDiaSemana("Miércoles");
		clase2.setHora("14:00");

		clasesMock = Arrays.asList(clase1, clase2);
		temasMock = Arrays.asList(temaReal);


		Mockito.reset(servicioLogin, servicioTema);

		// Configurar comportamiento básico de los mocks
		when(servicioTema.obtenerTodos()).thenReturn(temasMock);
		when(servicioTema.obtenerPorId(1L)).thenReturn(temaReal);
		when(servicioLogin.obtenerProfesoresDeAlumno(1L)).thenReturn(Arrays.asList(profesorReal));
		when(servicioLogin.obtenerClasesAlumno(1L)).thenReturn(clasesMock);
		when(servicioLogin.obtenerClasesProfesor(2L)).thenReturn(clasesMock);

		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	@Test
	public void queAlIrALoginMuestraLaPaginaDeLogin() throws Exception {
		MvcResult result = mockMvc.perform(get("/login"))
				.andExpect(status().isOk())
				.andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertThat(modelAndView.getViewName(), is("login"));
		assertThat(modelAndView.getModel().get("datosLogin"), is(instanceOf(DatosLogin.class)));
	}

	@Test
	public void queAlAccederALaRaizRedirigeALogin() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	public void queAlValidarLoginConDatosVaciosDevuelveError() throws Exception {
		MvcResult result = mockMvc.perform(post("/validar-login")
						.param("email", "")
						.param("password", ""))
				.andExpect(status().isOk())
				.andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertThat(modelAndView.getViewName(), is("login"));
		assertThat(modelAndView.getModel().get("error"), is("El email y la contraseña son obligatorios"));
	}

	@Test
	public void queAlValidarLoginConEmailVacioDevuelveError() throws Exception {
		MvcResult result = mockMvc.perform(post("/validar-login")
						.param("email", "")
						.param("password", "password123"))
				.andExpect(status().isOk())
				.andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertThat(modelAndView.getViewName(), is("login"));
		assertThat(modelAndView.getModel().get("error"), is("El email es obligatorio"));
	}

	@Test
	public void queAlValidarLoginConPasswordVacioDevuelveError() throws Exception {
		MvcResult result = mockMvc.perform(post("/validar-login")
						.param("email", "test@unlam.com")
						.param("password", ""))
				.andExpect(status().isOk())
				.andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertThat(modelAndView.getViewName(), is("login"));
		assertThat(modelAndView.getModel().get("error"), is("La contraseña es obligatoria"));
	}

	@Test
	public void queAlValidarLoginConEmailInvalidoDevuelveError() throws Exception {
		MvcResult result = mockMvc.perform(post("/validar-login")
						.param("email", "email-invalido")
						.param("password", "password123"))
				.andExpect(status().isOk())
				.andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertThat(modelAndView.getViewName(), is("login"));
		assertThat(modelAndView.getModel().get("error"), is("El formato del email es inválido"));
	}

	@Test
	public void queAlValidarLoginConCredencialesIncorrectasDevuelveError() throws Exception {
		when(servicioLogin.consultarUsuario("test@unlam.com", "password123")).thenReturn(null);

		MvcResult result = mockMvc.perform(post("/validar-login")
						.param("email", "test@unlam.com")
						.param("password", "password123"))
				.andExpect(status().isOk())
				.andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertThat(modelAndView.getViewName(), is("login"));
		assertThat(modelAndView.getModel().get("error"), is("Usuario o clave incorrecta"));
	}

	@Test
	public void queAlValidarLoginConAlumnoValidoRedirigeAHome() throws Exception {
		when(servicioLogin.consultarUsuario("alumno@unlam.com", "password123")).thenReturn(alumnoReal);
		doNothing().when(servicioLogin).guardarUltimaConexion(alumnoReal);

		mockMvc.perform(post("/validar-login")
						.param("email", "alumno@unlam.com")
						.param("password", "password123"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/home"));

		verify(servicioLogin).guardarUltimaConexion(alumnoReal);
	}

	@Test
	public void queAlValidarLoginConProfesorValidoRedirigeAHome() throws Exception {
		when(servicioLogin.consultarUsuario("profesor@unlam.com", "password123")).thenReturn(profesorReal);
		doNothing().when(servicioLogin).guardarUltimaConexion(profesorReal);

		mockMvc.perform(post("/validar-login")
						.param("email", "profesor@unlam.com")
						.param("password", "password123"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/home"));

		verify(servicioLogin).guardarUltimaConexion(profesorReal);
	}

	@Test
	public void queAlIrANuevoUsuarioMuestraElFormularioDeRegistro() throws Exception {
		MvcResult result = mockMvc.perform(get("/nuevo-usuario"))
				.andExpect(status().isOk())
				.andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertThat(modelAndView.getViewName(), is("nuevo-usuario"));
		assertThat(modelAndView.getModel().get("datosRegistro"), is(instanceOf(DatosRegistro.class)));
	}

	@Test
	public void queAlRegistrarAlumnoConDatosVaciosDevuelveError() throws Exception {
		MvcResult result = mockMvc.perform(post("/registrarme")
						.param("nombre", "")
						.param("apellido", "")
						.param("email", "")
						.param("password", ""))
				.andExpect(status().isOk())
				.andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertThat(modelAndView.getViewName(), is("nuevo-usuario"));
		assertThat(modelAndView.getModel().get("error"), is("Todos los campos son obligatorios"));
	}

	@Test
	public void queAlRegistrarAlumnoConEmailInvalidoDevuelveError() throws Exception {
		MvcResult result = mockMvc.perform(post("/registrarme")
						.param("nombre", "Juan")
						.param("apellido", "Pérez")
						.param("email", "email-invalido")
						.param("password", "password123"))
				.andExpect(status().isOk())
				.andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertThat(modelAndView.getViewName(), is("nuevo-usuario"));
		assertThat(modelAndView.getModel().get("error"), is("El formato del email es inválido"));
	}

	@Test
	public void queAlRegistrarAlumnoValidoRedirigeALogin() throws Exception {
		doNothing().when(servicioLogin).registrar(any(Alumno.class));

		mockMvc.perform(post("/registrarme")
						.param("nombre", "Juan")
						.param("apellido", "Pérez")
						.param("email", "juan@unlam.com")
						.param("password", "password123"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));

		verify(servicioLogin).registrar(any(Alumno.class));
	}

	@Test
	public void queAlIrAlRegistroProfesorMuestraElFormularioConTemas() throws Exception {
		MvcResult result = mockMvc.perform(get("/registrarprofesor"))
				.andExpect(status().isOk())
				.andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertThat(modelAndView.getViewName(), is("registrar-profesor"));
		assertThat(modelAndView.getModel().get("temas"), is(temasMock));
		assertThat(modelAndView.getModel().get("datosRegistro"), is(instanceOf(DatosRegistroProfesor.class)));

		verify(servicioTema).obtenerTodos();
	}

	@Test
	public void queAlRegistrarProfesorConDatosVaciosDevuelveError() throws Exception {
		MvcResult result = mockMvc.perform(post("/registrarprofesor")
						.param("nombre", "")
						.param("apellido", "")
						.param("email", "")
						.param("password", "")
						.param("temaId", ""))
				.andExpect(status().isOk())
				.andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertThat(modelAndView.getViewName(), is("registrar-profesor"));
		assertThat(modelAndView.getModel().get("error"), is("Todos los campos son obligatorios"));
	}

	@Test
	public void queAlRegistrarProfesorSinUbicacionDevuelveError() throws Exception {
		MvcResult result = mockMvc.perform(post("/registrarprofesor")
						.param("nombre", "Carlos")
						.param("apellido", "López")
						.param("email", "carlos@unlam.com")
						.param("password", "password123")
						.param("temaId", "1"))
				.andExpect(status().isOk())
				.andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertThat(modelAndView.getViewName(), is("registrar-profesor"));
		assertThat(modelAndView.getModel().get("error"), is("Debe seleccionar una ubicación en el mapa"));
	}

	@Test
	public void queAlIrAHomeConAlumnoMuestraHomeConDatosDelAlumno() throws Exception {
		MvcResult result = mockMvc.perform(get("/home")
						.sessionAttr("USUARIO", alumnoReal))
				.andExpect(status().isOk())
				.andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertThat(modelAndView.getViewName(), is("home"));
		assertThat(modelAndView.getModel().get("nombreUsuario"), is("Alumno Test"));
		assertThat(modelAndView.getModel().get("rol"), is("alumno"));
		assertThat(modelAndView.getModel().get("clasesReservadas"), is(clasesMock));

		verify(servicioLogin).obtenerClasesAlumno(1L);
	}

	@Test
	public void queAlIrAHomeConProfesorMuestraHomeConDatosDelProfesor() throws Exception {
		MvcResult result = mockMvc.perform(get("/home")
						.sessionAttr("USUARIO", profesorReal))
				.andExpect(status().isOk())
				.andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertThat(modelAndView.getViewName(), is("home"));
		assertThat(modelAndView.getModel().get("nombreUsuario"), is("Profesor Test"));
		assertThat(modelAndView.getModel().get("rol"), is("profesor"));
		assertThat(modelAndView.getModel().get("temaProfesor"), is(temaReal));
		assertThat(modelAndView.getModel().get("clasesReservadas"), is(clasesMock));

		verify(servicioLogin).obtenerClasesProfesor(2L);
	}

	@Test
	public void queAlVerMisClasesConUsuarioValidoMuestraLasClases() throws Exception {
		MvcResult result = mockMvc.perform(get("/mis-clases")
						.sessionAttr("USUARIO", alumnoReal))
				.andExpect(status().isOk())
				.andReturn();

		ModelAndView modelAndView = result.getModelAndView();
		assertThat(modelAndView.getViewName(), is("todas-mis-clases"));
		assertThat(modelAndView.getModel().get("todasLasClases"), is(clasesMock));

		verify(servicioLogin).obtenerClasesAlumno(1L);
	}

	@Test
	public void queAlVerMisClasesSinUsuarioEnSesionRedirigeALogin() throws Exception {
		mockMvc.perform(get("/mis-clases"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	public void queAlCerrarSesionInvalidaLaSesionYRedirigeAHome() throws Exception {
		mockMvc.perform(get("/logout"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/home"));
	}
}