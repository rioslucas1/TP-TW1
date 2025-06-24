package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Clase;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServicioLoginImplTest {

	private ServicioLogin servicioLogin;
	private RepositorioUsuario repositorioUsuarioMock;
	private Alumno alumnoMock;
	private Profesor profesorMock;
	private Usuario usuarioMock;



	@BeforeEach
	public void init(){


		repositorioUsuarioMock = mock(RepositorioUsuario.class);
		servicioLogin = new ServicioLoginImpl(repositorioUsuarioMock);

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

		usuarioMock = mock(Usuario.class);

		when(usuarioMock.getEmail()).thenReturn("usuario@unlam.com");
		when(usuarioMock.getPassword()).thenReturn("contra123");

	}

	@Test
	public void consultarUsuarioConEmailYPasswordCorrectosDeberiaRetornarUsuario() {
		String email = "alumno@unlam.com";
		String password = "123456";
		when(repositorioUsuarioMock.buscarUsuario(email, password)).thenReturn(alumnoMock);
		Usuario usuarioEncontrado = servicioLogin.consultarUsuario(email, password);
		assertNotNull(usuarioEncontrado);
		assertEquals(alumnoMock, usuarioEncontrado);
		verify(repositorioUsuarioMock, times(1)).buscarUsuario(email, password);
	}

	@Test
	public void consultarUsuarioConEmailYPasswordIncorrectosDeberiaRetornarNull() {
		String email = "usuario@inexistente.com";
		String password = "passwordIncorrecto";
		when(repositorioUsuarioMock.buscarUsuario(email, password)).thenReturn(null);
		Usuario usuarioEncontrado = servicioLogin.consultarUsuario(email, password);
		assertNull(usuarioEncontrado);
		verify(repositorioUsuarioMock, times(1)).buscarUsuario(email, password);
			}

	@Test
	public void registrarUsuarioNuevoDeberiaGuardarUsuarioExitosamente() throws UsuarioExistente {

		String email = "nuevousuario@unlam.com";
		when(usuarioMock.getEmail()).thenReturn(email);
		when(repositorioUsuarioMock.buscar(email)).thenReturn(null);
		servicioLogin.registrar(usuarioMock);
		verify(repositorioUsuarioMock, times(1)).buscar(email);
		verify(repositorioUsuarioMock, times(1)).guardar(usuarioMock);
	}

	@Test
	public void registrarUsuarioExistenteDeberiaLanzarExcepcionUsuarioExistente() {
		String email = "usuarioexistente@unlam.com";
		when(usuarioMock.getEmail()).thenReturn(email);
		when(repositorioUsuarioMock.buscar(email)).thenReturn(usuarioMock);
		assertThrows(UsuarioExistente.class, () -> {
			servicioLogin.registrar(usuarioMock);
		});
		verify(repositorioUsuarioMock, times(1)).buscar(email);
		verify(repositorioUsuarioMock, never()).guardar(any(Usuario.class));
	}


	@Test
	public void obtenerProfesoresDeberiaRetornarListaDeProfesores() {
		Profesor profesor1 = mock(Profesor.class);
		Profesor profesor2 = mock(Profesor.class);

		List<Usuario> profesoresEsperados = Arrays.asList(profesor1, profesor2);
		when(repositorioUsuarioMock.buscarPorTipo(Profesor.class)).thenReturn(profesoresEsperados);
		List<Usuario> profesoresObtenidos = servicioLogin.obtenerProfesores();
		assertNotNull(profesoresObtenidos);
		assertEquals(profesoresObtenidos.size(), 2);
		assertThat(profesoresObtenidos, containsInAnyOrder(profesor1, profesor2));
		verify(repositorioUsuarioMock, times(1)).buscarPorTipo(Profesor.class);

		}

	@Test
	public void obtenerProfesoresSinProfesoresRegistradosDeberiaRetornarListaVacia() {

		List<Usuario> listaVacia = Arrays.asList();
		when(repositorioUsuarioMock.buscarPorTipo(Profesor.class)).thenReturn(listaVacia);
		List<Usuario> profesoresObtenidos = servicioLogin.obtenerProfesores();
		assertNotNull(profesoresObtenidos);
		assertEquals(profesoresObtenidos.size(), 0);
		assertEquals(profesoresObtenidos, listaVacia);
		verify(repositorioUsuarioMock, times(1)).buscarPorTipo(Profesor.class);
	}

	@Test
	public void consultarUsuarioConEmailNullDeberiaLlamarRepositorio() {

		String email = null;
		String password = "123456";
		when(repositorioUsuarioMock.buscarUsuario(email, password)).thenReturn(null);
		Usuario usuarioEncontrado = servicioLogin.consultarUsuario(email, password);
		assertNull(usuarioEncontrado);
		verify(repositorioUsuarioMock, times(1)).buscarUsuario(email, password);
	}

	@Test
	public void consultarUsuarioConPasswordNullDeberiaLlamarRepositorio() {

		String email = "usuario@unlam.com";
		String password = null;
		when(repositorioUsuarioMock.buscarUsuario(email, password)).thenReturn(null);
		Usuario usuarioEncontrado = servicioLogin.consultarUsuario(email, password);
		assertNull(usuarioEncontrado);
		verify(repositorioUsuarioMock, times(1)).buscarUsuario(email, password);
	}

	@Test
	public void consultarUsuarioConEmailVacioDeberiaRetornarNull() {
		String email = "";
		String password = "123456";
		when(repositorioUsuarioMock.buscarUsuario(email, password)).thenReturn(null);

		Usuario usuarioEncontrado = servicioLogin.consultarUsuario(email, password);

		assertNull(usuarioEncontrado);
		verify(repositorioUsuarioMock, times(1)).buscarUsuario(email, password);
	}

	@Test
	public void consultarUsuarioConPasswordVaciaDeberiaRetornarNull() {
		String email = "usuario@unlam.com";
		String password = "";
		when(repositorioUsuarioMock.buscarUsuario(email, password)).thenReturn(null);

		Usuario usuarioEncontrado = servicioLogin.consultarUsuario(email, password);

		assertNull(usuarioEncontrado);
		verify(repositorioUsuarioMock, times(1)).buscarUsuario(email, password);
	}

	@Test
	public void consultarUsuarioConEspaciosEnBlancoDeberiaRetornarNull() {
		String email = "   ";
		String password = "   ";
		when(repositorioUsuarioMock.buscarUsuario(email, password)).thenReturn(null);

		Usuario usuarioEncontrado = servicioLogin.consultarUsuario(email, password);

		assertNull(usuarioEncontrado);
		verify(repositorioUsuarioMock, times(1)).buscarUsuario(email, password);
	}

	@Test
	public void registrarUsuarioConEmailVacioDeberiaLanzarExcepcionUsuarioExistente() {
		when(usuarioMock.getEmail()).thenReturn("");
		when(repositorioUsuarioMock.buscar("")).thenReturn(usuarioMock);

		assertThrows(UsuarioExistente.class, () -> servicioLogin.registrar(usuarioMock));

		verify(repositorioUsuarioMock, times(1)).buscar("");
		verify(repositorioUsuarioMock, never()).guardar(any(Usuario.class));
	}

	@Test
	public void registrarUsuarioConEmailNullDeberiaLanzarExcepcionUsuarioExistente() {
		when(usuarioMock.getEmail()).thenReturn(null);
		when(repositorioUsuarioMock.buscar(null)).thenReturn(usuarioMock);

		assertThrows(UsuarioExistente.class, () -> servicioLogin.registrar(usuarioMock));

		verify(repositorioUsuarioMock, times(1)).buscar(null);
		verify(repositorioUsuarioMock, never()).guardar(any(Usuario.class));
	}

	@Test
	public void consultarProfesorConCredencialesCorrectasDeberiaRetornarProfesor() {
		String email = "profesor@unlam.com";
		String password = "123456";
		when(repositorioUsuarioMock.buscarUsuario(email, password)).thenReturn(profesorMock);

		Usuario usuarioEncontrado = servicioLogin.consultarUsuario(email, password);

		assertNotNull(usuarioEncontrado);
		assertEquals(profesorMock, usuarioEncontrado);
		verify(repositorioUsuarioMock, times(1)).buscarUsuario(email, password);
	}

	@Test
	public void registrarAlumnoNuevoDeberiaGuardarlo() throws UsuarioExistente {
		String email = "nuevoalumno@unlam.com";
		when(alumnoMock.getEmail()).thenReturn(email);
		when(repositorioUsuarioMock.buscar(email)).thenReturn(null);

		servicioLogin.registrar(alumnoMock);

		verify(repositorioUsuarioMock, times(1)).buscar(email);
		verify(repositorioUsuarioMock, times(1)).guardar(alumnoMock);
	}

	@Test
	public void registrarProfesorNuevoDeberiaGuardarlo() throws UsuarioExistente {
		String email = "nuevoprofesor@unlam.com";
		when(profesorMock.getEmail()).thenReturn(email);
		when(repositorioUsuarioMock.buscar(email)).thenReturn(null);

		servicioLogin.registrar(profesorMock);

		verify(repositorioUsuarioMock, times(1)).buscar(email);
		verify(repositorioUsuarioMock, times(1)).guardar(profesorMock);
	}

	@Test
	public void obtenerProfesoresConUnSoloProfesorDeberiaRetornarListaConUnElemento() {
		List<Usuario> profesoresEsperados = Arrays.asList(profesorMock);
		when(repositorioUsuarioMock.buscarPorTipo(Profesor.class)).thenReturn(profesoresEsperados);

		List<Usuario> profesoresObtenidos = servicioLogin.obtenerProfesores();

		assertNotNull(profesoresObtenidos);
		assertEquals(1, profesoresObtenidos.size());
		assertEquals(profesorMock, profesoresObtenidos.get(0));
		verify(repositorioUsuarioMock, times(1)).buscarPorTipo(Profesor.class);
	}

	@Test
	public void obtenerProfesoresDeAlumnoConAlumnoValidoDeberiaRetornarListaDeProfesores() {
		Long alumnoId = 1L;
		Profesor profesor1 = mock(Profesor.class);
		Profesor profesor2 = mock(Profesor.class);
		List<Profesor> profesoresEsperados = Arrays.asList(profesor1, profesor2);

		when(repositorioUsuarioMock.obtenerProfesoresDeAlumno(alumnoId)).thenReturn(profesoresEsperados);

		List<Profesor> profesoresObtenidos = servicioLogin.obtenerProfesoresDeAlumno(alumnoId);

		assertNotNull(profesoresObtenidos);
		assertEquals(2, profesoresObtenidos.size());
		assertThat(profesoresObtenidos, containsInAnyOrder(profesor1, profesor2));
		verify(repositorioUsuarioMock, times(1)).obtenerProfesoresDeAlumno(alumnoId);
	}

	@Test
	public void obtenerProfesoresDeAlumnoConAlumnoSinProfesoresDeberiaRetornarListaVacia() {
		Long alumnoId = 1L;
		List<Profesor> listaVacia = Arrays.asList();

		when(repositorioUsuarioMock.obtenerProfesoresDeAlumno(alumnoId)).thenReturn(listaVacia);

		List<Profesor> profesoresObtenidos = servicioLogin.obtenerProfesoresDeAlumno(alumnoId);

		assertNotNull(profesoresObtenidos);
		assertEquals(0, profesoresObtenidos.size());
		verify(repositorioUsuarioMock, times(1)).obtenerProfesoresDeAlumno(alumnoId);
	}

	@Test
	public void obtenerProfesoresDeAlumnoConIdNuloDeberiaLlamarRepositorio() {
		Long alumnoId = null;

		when(repositorioUsuarioMock.obtenerProfesoresDeAlumno(alumnoId)).thenReturn(Arrays.asList());

		List<Profesor> profesoresObtenidos = servicioLogin.obtenerProfesoresDeAlumno(alumnoId);

		assertNotNull(profesoresObtenidos);
		verify(repositorioUsuarioMock, times(1)).obtenerProfesoresDeAlumno(alumnoId);
	}

	@Test
	public void obtenerClasesProfesorConProfesorValidoDeberiaRetornarListaDeClases() {
		Long profesorId = 1L;
		Clase clase1 = mock(Clase.class);
		Clase clase2 = mock(Clase.class);
		List<Clase> clasesEsperadas = Arrays.asList(clase1, clase2);

		when(repositorioUsuarioMock.obtenerClasesProfesor(profesorId)).thenReturn(clasesEsperadas);

		List<Clase> clasesObtenidas = servicioLogin.obtenerClasesProfesor(profesorId);

		assertNotNull(clasesObtenidas);
		assertEquals(2, clasesObtenidas.size());
		assertThat(clasesObtenidas, containsInAnyOrder(clase1, clase2));
		verify(repositorioUsuarioMock, times(1)).obtenerClasesProfesor(profesorId);
	}

	@Test
	public void obtenerClasesProfesorConProfesorSinClasesDeberiaRetornarListaVacia() {
		Long profesorId = 1L;
		List<Clase> listaVacia = Arrays.asList();

		when(repositorioUsuarioMock.obtenerClasesProfesor(profesorId)).thenReturn(listaVacia);

		List<Clase> clasesObtenidas = servicioLogin.obtenerClasesProfesor(profesorId);

		assertNotNull(clasesObtenidas);
		assertEquals(0, clasesObtenidas.size());
		verify(repositorioUsuarioMock, times(1)).obtenerClasesProfesor(profesorId);
	}

	@Test
	public void obtenerClasesProfesorConIdNuloDeberiaLlamarRepositorio() {
		Long profesorId = null;

		when(repositorioUsuarioMock.obtenerClasesProfesor(profesorId)).thenReturn(Arrays.asList());

		List<Clase> clasesObtenidas = servicioLogin.obtenerClasesProfesor(profesorId);

		assertNotNull(clasesObtenidas);
		verify(repositorioUsuarioMock, times(1)).obtenerClasesProfesor(profesorId);
	}

	@Test
	public void obtenerClasesAlumnoConAlumnoValidoDeberiaRetornarListaDeClases() {
		Long alumnoId = 1L;
		Clase clase1 = mock(Clase.class);
		Clase clase2 = mock(Clase.class);
		List<Clase> clasesEsperadas = Arrays.asList(clase1, clase2);

		when(repositorioUsuarioMock.obtenerClasesAlumno(alumnoId)).thenReturn(clasesEsperadas);

		List<Clase> clasesObtenidas = servicioLogin.obtenerClasesAlumno(alumnoId);

		assertNotNull(clasesObtenidas);
		assertEquals(2, clasesObtenidas.size());
		assertThat(clasesObtenidas, containsInAnyOrder(clase1, clase2));
		verify(repositorioUsuarioMock, times(1)).obtenerClasesAlumno(alumnoId);
	}

	@Test
	public void obtenerClasesAlumnoConAlumnoSinClasesDeberiaRetornarListaVacia() {
		Long alumnoId = 1L;
		List<Clase> listaVacia = Arrays.asList();

		when(repositorioUsuarioMock.obtenerClasesAlumno(alumnoId)).thenReturn(listaVacia);

		List<Clase> clasesObtenidas = servicioLogin.obtenerClasesAlumno(alumnoId);

		assertNotNull(clasesObtenidas);
		assertEquals(0, clasesObtenidas.size());
		verify(repositorioUsuarioMock, times(1)).obtenerClasesAlumno(alumnoId);
	}

	@Test
	public void obtenerClasesAlumnoConIdNuloDeberiaLlamarRepositorio() {
		Long alumnoId = null;

		when(repositorioUsuarioMock.obtenerClasesAlumno(alumnoId)).thenReturn(Arrays.asList());

		List<Clase> clasesObtenidas = servicioLogin.obtenerClasesAlumno(alumnoId);

		assertNotNull(clasesObtenidas);
		verify(repositorioUsuarioMock, times(1)).obtenerClasesAlumno(alumnoId);
	}
}