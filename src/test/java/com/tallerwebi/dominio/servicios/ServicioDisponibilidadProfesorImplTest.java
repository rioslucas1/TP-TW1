package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Usuario;
import org.junit.jupiter.api.BeforeEach;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.*;

public class ServicioDisponibilidadProfesorImplTest {

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
	}