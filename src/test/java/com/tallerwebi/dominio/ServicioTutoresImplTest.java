package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.RepositorioProfesor;
import com.tallerwebi.dominio.entidades.Profesor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ServicioTutoresImplTest {

    @Mock
    private RepositorioProfesor repositorioProfesor;

    @InjectMocks
    private ServicioTutoresImpl servicioTutores;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void deberiaObtenerTutoresFiltradosDesdeElRepositorio() {

        String categoria = "Programacion";
        String modalidad = "Online";
        String duracion = "1 hora";
        String busqueda = "Angel";

        Profesor profe1 = new Profesor(); // Podés completar sus campos si lo necesitás
        Profesor profe2 = new Profesor();
        List<Profesor> profesoresEsperados = Arrays.asList(profe1, profe2);

        when(repositorioProfesor.buscarTutoresFiltrados(categoria, modalidad, duracion, busqueda))
                .thenReturn(profesoresEsperados);


        List<Profesor> resultado = servicioTutores.obtenerTutoresFiltrados(categoria, modalidad, duracion, busqueda);


        assertEquals(profesoresEsperados, resultado);
        verify(repositorioProfesor).buscarTutoresFiltrados(categoria, modalidad, duracion, busqueda);
    }
}