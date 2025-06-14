package com.tallerwebi.dominio.entidades;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TemaTest {

	@Test
	public void crearTemaConNombreDeberiaEstablecerNombreCorrectamente() {
		Tema tema = new Tema();
		String nombreEsperado = "Matemáticas";
		tema.setNombre(nombreEsperado);
		assertEquals(nombreEsperado, tema.getNombre());
	}

	@Test
	public void temaRecienCreadoDeberiaTenerIdNull() {

		Tema tema = new Tema();
		assertNull(tema.getId());
	}

	@Test
	public void establecerIdEnTemaDeberiaFuncionarCorrectamente() {
		Tema tema = new Tema();
		Long idEsperado = 1L;
		tema.setId(idEsperado);
		assertEquals(idEsperado, tema.getId());
	}
}