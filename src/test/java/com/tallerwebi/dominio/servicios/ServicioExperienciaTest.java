package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.RepositorioExperiencia;
import com.tallerwebi.dominio.entidades.ExperienciaEstudiantil;
import com.tallerwebi.dominio.entidades.Profesor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
public class ServicioExperienciaTest {

    private ServicioExperiencia servicioExperiencia;
    private RepositorioExperiencia repositorioExperienciaMock;
    private ExperienciaEstudiantil experienciaMock;
    private Profesor profesorMock;

    @BeforeEach
    public void init() {
        repositorioExperienciaMock = mock(RepositorioExperiencia.class);
        servicioExperiencia = new ServicioExperienciaImpl(repositorioExperienciaMock);

        profesorMock = mock(Profesor.class);
        when(profesorMock.getId()).thenReturn(1L);

        experienciaMock = mock(ExperienciaEstudiantil.class);
        when(experienciaMock.getId()).thenReturn(1L);
        when(experienciaMock.getProfesor()).thenReturn(profesorMock);
        when(experienciaMock.getInstitucion()).thenReturn("Universidad Nacional");
        when(experienciaMock.getDescripcion()).thenReturn("Experiencia en docencia");
        when(experienciaMock.getFecha()).thenReturn("2023-01-01");
        when(experienciaMock.getTipoExperiencia()).thenReturn("Docencia");
    }

    @Test
    public void obtenerExperienciasPorProfesorConIdValidoDeberiaRetornarListaDeExperiencias() {
        Long profesorId = 1L;
        ExperienciaEstudiantil experiencia1 = mock(ExperienciaEstudiantil.class);
        ExperienciaEstudiantil experiencia2 = mock(ExperienciaEstudiantil.class);
        List<ExperienciaEstudiantil> experienciasEsperadas = Arrays.asList(experiencia1, experiencia2);

        when(repositorioExperienciaMock.obtenerPorProfesorId(profesorId)).thenReturn(experienciasEsperadas);

        List<ExperienciaEstudiantil> experienciasObtenidas = servicioExperiencia.obtenerExperienciasPorProfesor(profesorId);

        assertNotNull(experienciasObtenidas);
        assertEquals(2, experienciasObtenidas.size());
        assertThat(experienciasObtenidas, containsInAnyOrder(experiencia1, experiencia2));
        verify(repositorioExperienciaMock, times(1)).obtenerPorProfesorId(profesorId);
    }

    @Test
    public void obtenerExperienciasPorProfesorSinExperienciasDeberiaRetornarListaVacia() {
        Long profesorId = 1L;
        List<ExperienciaEstudiantil> listaVacia = Arrays.asList();

        when(repositorioExperienciaMock.obtenerPorProfesorId(profesorId)).thenReturn(listaVacia);

        List<ExperienciaEstudiantil> experienciasObtenidas = servicioExperiencia.obtenerExperienciasPorProfesor(profesorId);

        assertNotNull(experienciasObtenidas);
        assertEquals(0, experienciasObtenidas.size());
        assertEquals(listaVacia, experienciasObtenidas);
        verify(repositorioExperienciaMock, times(1)).obtenerPorProfesorId(profesorId);
    }

    @Test
    public void obtenerExperienciasPorProfesorConIdNullDeberiaLlamarRepositorio() {
        Long profesorId = null;
        when(repositorioExperienciaMock.obtenerPorProfesorId(profesorId)).thenReturn(Arrays.asList());

        List<ExperienciaEstudiantil> experienciasObtenidas = servicioExperiencia.obtenerExperienciasPorProfesor(profesorId);

        assertNotNull(experienciasObtenidas);
        verify(repositorioExperienciaMock, times(1)).obtenerPorProfesorId(profesorId);
    }

    @Test
    public void guardarExperienciaValidaDeberiaRetornarExperienciaGuardada() {
        when(repositorioExperienciaMock.guardar(experienciaMock)).thenReturn(experienciaMock);

        ExperienciaEstudiantil experienciaGuardada = servicioExperiencia.guardar(experienciaMock);

        assertNotNull(experienciaGuardada);
        assertEquals(experienciaMock, experienciaGuardada);
        verify(repositorioExperienciaMock, times(1)).guardar(experienciaMock);
    }

    @Test
    public void guardarExperienciaNullDeberiaLlamarRepositorio() {
        when(repositorioExperienciaMock.guardar(null)).thenReturn(null);

        ExperienciaEstudiantil experienciaGuardada = servicioExperiencia.guardar(null);

        assertNull(experienciaGuardada);
        verify(repositorioExperienciaMock, times(1)).guardar(null);
    }

    @Test
    public void eliminarExperienciaConIdValidoDeberiaLlamarRepositorio() {
        Long experienciaId = 1L;

        servicioExperiencia.eliminar(experienciaId);

        verify(repositorioExperienciaMock, times(1)).eliminar(experienciaId);
    }

    @Test
    public void eliminarExperienciaConIdNullDeberiaLlamarRepositorio() {
        Long experienciaId = null;

        servicioExperiencia.eliminar(experienciaId);

        verify(repositorioExperienciaMock, times(1)).eliminar(experienciaId);
    }

    @Test
    public void obtenerPorIdConIdValidoDeberiaRetornarExperiencia() {
        Long experienciaId = 1L;
        when(repositorioExperienciaMock.buscarPorId(experienciaId)).thenReturn(experienciaMock);

        ExperienciaEstudiantil experienciaObtenida = servicioExperiencia.obtenerPorId(experienciaId);

        assertNotNull(experienciaObtenida);
        assertEquals(experienciaMock, experienciaObtenida);
        verify(repositorioExperienciaMock, times(1)).buscarPorId(experienciaId);
    }

    @Test
    public void obtenerPorIdConIdInexistenteDeberiaRetornarNull() {
        Long experienciaId = 999L;
        when(repositorioExperienciaMock.buscarPorId(experienciaId)).thenReturn(null);

        ExperienciaEstudiantil experienciaObtenida = servicioExperiencia.obtenerPorId(experienciaId);

        assertNull(experienciaObtenida);
        verify(repositorioExperienciaMock, times(1)).buscarPorId(experienciaId);
    }

    @Test
    public void obtenerPorIdConIdNullDeberiaLlamarRepositorio() {
        Long experienciaId = null;
        when(repositorioExperienciaMock.buscarPorId(experienciaId)).thenReturn(null);

        ExperienciaEstudiantil experienciaObtenida = servicioExperiencia.obtenerPorId(experienciaId);

        assertNull(experienciaObtenida);
        verify(repositorioExperienciaMock, times(1)).buscarPorId(experienciaId);
    }

    @Test
    public void obtenerTodasDeberiaRetornarListaCompleta() {
        ExperienciaEstudiantil experiencia1 = mock(ExperienciaEstudiantil.class);
        ExperienciaEstudiantil experiencia2 = mock(ExperienciaEstudiantil.class);
        ExperienciaEstudiantil experiencia3 = mock(ExperienciaEstudiantil.class);
        List<ExperienciaEstudiantil> experienciasEsperadas = Arrays.asList(experiencia1, experiencia2, experiencia3);

        when(repositorioExperienciaMock.obtenerTodas()).thenReturn(experienciasEsperadas);

        List<ExperienciaEstudiantil> experienciasObtenidas = servicioExperiencia.obtenerTodas();

        assertNotNull(experienciasObtenidas);
        assertEquals(3, experienciasObtenidas.size());
        assertThat(experienciasObtenidas, containsInAnyOrder(experiencia1, experiencia2, experiencia3));
        verify(repositorioExperienciaMock, times(1)).obtenerTodas();
    }

    @Test
    public void obtenerTodasSinExperienciasDeberiaRetornarListaVacia() {
        List<ExperienciaEstudiantil> listaVacia = Arrays.asList();
        when(repositorioExperienciaMock.obtenerTodas()).thenReturn(listaVacia);

        List<ExperienciaEstudiantil> experienciasObtenidas = servicioExperiencia.obtenerTodas();

        assertNotNull(experienciasObtenidas);
        assertEquals(0, experienciasObtenidas.size());
        assertEquals(listaVacia, experienciasObtenidas);
        verify(repositorioExperienciaMock, times(1)).obtenerTodas();
    }

    @Test
    public void obtenerExperienciasPorProfesorConUnSolaExperienciaDeberiaRetornarListaConUnElemento() {
        Long profesorId = 1L;
        List<ExperienciaEstudiantil> experienciasEsperadas = Arrays.asList(experienciaMock);

        when(repositorioExperienciaMock.obtenerPorProfesorId(profesorId)).thenReturn(experienciasEsperadas);

        List<ExperienciaEstudiantil> experienciasObtenidas = servicioExperiencia.obtenerExperienciasPorProfesor(profesorId);

        assertNotNull(experienciasObtenidas);
        assertEquals(1, experienciasObtenidas.size());
        assertEquals(experienciaMock, experienciasObtenidas.get(0));
        verify(repositorioExperienciaMock, times(1)).obtenerPorProfesorId(profesorId);
    }
}