package com.tallerwebi.dominio.entidades;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProfesorTest {
	@Test
	public void crearProfesorYEstablecerCoordenadasDeberiaFuncionarCorrectamente() {

		Profesor profesor = new Profesor();
		double latitudEsperada = -34.6037;
		double longitudEsperada = -58.3816;

		profesor.setLatitud(latitudEsperada);
		profesor.setLongitud(longitudEsperada);

		assertEquals(latitudEsperada, profesor.getLatitud());
		assertEquals(longitudEsperada, profesor.getLongitud());
	}

	@Test

	public void profesorDeberiaPoderTenerTema() {
		Profesor profesor = new Profesor();
		Tema tema = new Tema();
		tema.setNombre("Matematica");

		profesor.setTema(tema);

		assertEquals(tema, profesor.getTema());
		assertEquals("Matematica", profesor.getTema().getNombre());
	}

	@Test
	public void profesorDeberiaHeredarPropiedadesDeUsuario() {
		Profesor profesor = new Profesor();
		String nombre = "Nombre";
		String apellido = "Apellido";
		String email = "profesor@unlam.com";
		String password = "654321";

		profesor.setNombre(nombre);
		profesor.setApellido(apellido);
		profesor.setEmail(email);
		profesor.setPassword(password);

		assertEquals(nombre, profesor.getNombre());
		assertEquals(apellido, profesor.getApellido());
		assertEquals(email, profesor.getEmail());
		assertEquals(password, profesor.getPassword());
	}
}