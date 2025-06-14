package com.tallerwebi.dominio.entidades;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.servicios.ServicioLogin;
import com.tallerwebi.dominio.servicios.ServicioLoginImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AlumnoTest {

	@Test
	public void crearAlumnoYEstablecerCoordenadasDeberiaFuncionarCorrectamente() {

		Alumno alumno = new Alumno();
		double latitudEsperada = -34.6037;
		double longitudEsperada = -58.3816;
		alumno.setLatitud(latitudEsperada);
		alumno.setLongitud(longitudEsperada);
		assertEquals(latitudEsperada, alumno.getLatitud());
		assertEquals(longitudEsperada, alumno.getLongitud());
	}

	@Test
	public void alumnoDeberiaPoderTenerListaDeTemas() {
		Alumno alumno = new Alumno();
		Tema tema1 = new Tema();
		tema1.setNombre("Matematicas");
		Tema tema2 = new Tema();
		tema2.setNombre("Fisica");
		List<Tema> temasEsperados = Arrays.asList(tema1, tema2);

		alumno.setTemas(temasEsperados);
		assertEquals(temasEsperados, alumno.getTemas());
		assertEquals(2, alumno.getTemas().size());
		assertTrue(alumno.getTemas().contains(tema1));
		assertTrue(alumno.getTemas().contains(tema2));
	}

	@Test
	public void alumnoDeberiaHeredarPropiedadesDeUsuario() {

		Alumno alumno = new Alumno();
		String nombre = "Nombre";
		String apellido = "Alumno";
		String email = "nombre@unlam.com";
		String password = "123456";


		alumno.setNombre(nombre);
		alumno.setApellido(apellido);
		alumno.setEmail(email);
		alumno.setPassword(password);

		assertEquals(nombre, alumno.getNombre());
		assertEquals(apellido, alumno.getApellido());
		assertEquals(email, alumno.getEmail());
		assertEquals(password, alumno.getPassword());
	}




}