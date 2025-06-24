package com.tallerwebi.dominio.servicios;
import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.Alumno;
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

public class ServicioSuscripcionTest {
    private ServicioSuscripcion servicioSuscripcion;
    private RepositorioUsuario repositorioUsuarioMock;
    private Alumno alumnoMock;
    private Profesor profesorMock;
    private Usuario usuarioGenericoMock;

    @BeforeEach
    public void init() {
        repositorioUsuarioMock = mock(RepositorioUsuario.class);
        servicioSuscripcion = new ServicioSuscripcionImpl(repositorioUsuarioMock);

        alumnoMock = mock(Alumno.class);
        when(alumnoMock.getId()).thenReturn(1L);
        when(alumnoMock.getEmail()).thenReturn("alumno@unlam.com");
        when(alumnoMock.getNombre()).thenReturn("Alumno");
        when(alumnoMock.getApellido()).thenReturn("Test");

        profesorMock = mock(Profesor.class);
        when(profesorMock.getId()).thenReturn(2L);
        when(profesorMock.getEmail()).thenReturn("profesor@unlam.com");
        when(profesorMock.getNombre()).thenReturn("Profesor");
        when(profesorMock.getApellido()).thenReturn("Test");

        usuarioGenericoMock = mock(Usuario.class);
        when(usuarioGenericoMock.getId()).thenReturn(3L);
    }

    @Test
    public void suscribirAlumnoAProfesorDeberiaRetornarTrueCuandoLaSuscripcionEsExitosa() {
        Long alumnoId = 1L;
        Long profesorId = 2L;

        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(profesorMock);
        when(alumnoMock.estaSuscritoAProfesor(profesorMock)).thenReturn(false);

        boolean resultado = servicioSuscripcion.suscribirAlumnoAProfesor(alumnoId, profesorId);

        assertTrue(resultado);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(profesorId);
        verify(alumnoMock, times(1)).estaSuscritoAProfesor(profesorMock);
        verify(alumnoMock, times(1)).agregarProfesor(profesorMock);
        verify(repositorioUsuarioMock, times(1)).modificar(alumnoMock);
    }

    @Test
    public void suscribirAlumnoAProfesorDeberiaRetornarFalseCuandoElAlumnoYaEstaSuscrito() {
        Long alumnoId = 1L;
        Long profesorId = 2L;

        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(profesorMock);
        when(alumnoMock.estaSuscritoAProfesor(profesorMock)).thenReturn(true);

        boolean resultado = servicioSuscripcion.suscribirAlumnoAProfesor(alumnoId, profesorId);

        assertFalse(resultado);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(profesorId);
        verify(alumnoMock, times(1)).estaSuscritoAProfesor(profesorMock);
        verify(alumnoMock, never()).agregarProfesor(any(Profesor.class));
        verify(repositorioUsuarioMock, never()).modificar(any(Usuario.class));
    }

    @Test
    public void suscribirAlumnoAProfesorDeberiaRetornarFalseCuandoElUsuarioAlumnoNoEsInstanciaDeAlumno() {
        Long alumnoId = 1L;
        Long profesorId = 2L;

        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(usuarioGenericoMock);
        when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(profesorMock);

        boolean resultado = servicioSuscripcion.suscribirAlumnoAProfesor(alumnoId, profesorId);

        assertFalse(resultado);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(profesorId);
        verify(repositorioUsuarioMock, never()).modificar(any(Usuario.class));
    }

    @Test
    public void suscribirAlumnoAProfesorDeberiaRetornarFalseCuandoElUsuarioProfesorNoEsInstanciaDeProfesor() {
        Long alumnoId = 1L;
        Long profesorId = 2L;

        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(usuarioGenericoMock);

        boolean resultado = servicioSuscripcion.suscribirAlumnoAProfesor(alumnoId, profesorId);

        assertFalse(resultado);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(profesorId);
        verify(repositorioUsuarioMock, never()).modificar(any(Usuario.class));
    }

    @Test
    public void suscribirAlumnoAProfesorDeberiaRetornarFalseCuandoElAlumnoNoExiste() {
        Long alumnoId = 1L;
        Long profesorId = 2L;

        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(null);
        when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(profesorMock);

        boolean resultado = servicioSuscripcion.suscribirAlumnoAProfesor(alumnoId, profesorId);

        assertFalse(resultado);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(profesorId);
        verify(repositorioUsuarioMock, never()).modificar(any(Usuario.class));
    }

    @Test
    public void suscribirAlumnoAProfesorDeberiaRetornarFalseCuandoElProfesorNoExiste() {
        Long alumnoId = 1L;
        Long profesorId = 2L;

        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(null);

        boolean resultado = servicioSuscripcion.suscribirAlumnoAProfesor(alumnoId, profesorId);

        assertFalse(resultado);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(profesorId);
        verify(repositorioUsuarioMock, never()).modificar(any(Usuario.class));
    }

    @Test
    public void desuscribirAlumnoDeProfesorDeberiaRetornarTrueCuandoLaDesuscripcionEsExitosa() {
        Long alumnoId = 1L;
        Long profesorId = 2L;

        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(profesorMock);
        when(alumnoMock.estaSuscritoAProfesor(profesorMock)).thenReturn(true);

        boolean resultado = servicioSuscripcion.desuscribirAlumnoDeProfesor(alumnoId, profesorId);

        assertTrue(resultado);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(profesorId);
        verify(alumnoMock, times(1)).estaSuscritoAProfesor(profesorMock);
        verify(alumnoMock, times(1)).eliminarProfesor(profesorMock);
        verify(repositorioUsuarioMock, times(1)).modificar(alumnoMock);
    }

    @Test
    public void desuscribirAlumnoDeProfesorDeberiaRetornarFalseCuandoElAlumnoNoEstaSuscrito() {
        Long alumnoId = 1L;
        Long profesorId = 2L;

        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(profesorMock);
        when(alumnoMock.estaSuscritoAProfesor(profesorMock)).thenReturn(false);

        boolean resultado = servicioSuscripcion.desuscribirAlumnoDeProfesor(alumnoId, profesorId);

        assertFalse(resultado);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(profesorId);
        verify(alumnoMock, times(1)).estaSuscritoAProfesor(profesorMock);
        verify(alumnoMock, never()).eliminarProfesor(any(Profesor.class));
        verify(repositorioUsuarioMock, never()).modificar(any(Usuario.class));
    }

    @Test
    public void desuscribirAlumnoDeProfesorDeberiaRetornarFalseCuandoElUsuarioAlumnoNoEsInstanciaDeAlumno() {
        Long alumnoId = 1L;
        Long profesorId = 2L;

        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(usuarioGenericoMock);
        when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(profesorMock);

        boolean resultado = servicioSuscripcion.desuscribirAlumnoDeProfesor(alumnoId, profesorId);

        assertFalse(resultado);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(profesorId);
        verify(repositorioUsuarioMock, never()).modificar(any(Usuario.class));
    }

    @Test
    public void desuscribirAlumnoDeProfesorDeberiaRetornarFalseCuandoElUsuarioProfesorNoEsInstanciaDeProfesor() {
        Long alumnoId = 1L;
        Long profesorId = 2L;

        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(usuarioGenericoMock);

        boolean resultado = servicioSuscripcion.desuscribirAlumnoDeProfesor(alumnoId, profesorId);

        assertFalse(resultado);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(profesorId);
        verify(repositorioUsuarioMock, never()).modificar(any(Usuario.class));
    }

    @Test
    public void estaAlumnoSuscritoAProfesorDeberiaRetornarTrueCuandoElAlumnoEstaSuscrito() {
        Long alumnoId = 1L;
        Long profesorId = 2L;

        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(profesorMock);
        when(alumnoMock.estaSuscritoAProfesor(profesorMock)).thenReturn(true);

        boolean resultado = servicioSuscripcion.estaAlumnoSuscritoAProfesor(alumnoId, profesorId);

        assertTrue(resultado);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(profesorId);
        verify(alumnoMock, times(1)).estaSuscritoAProfesor(profesorMock);
    }

    @Test
    public void estaAlumnoSuscritoAProfesorDeberiaRetornarFalseCuandoElAlumnoNoEstaSuscrito() {
        Long alumnoId = 1L;
        Long profesorId = 2L;

        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(profesorMock);
        when(alumnoMock.estaSuscritoAProfesor(profesorMock)).thenReturn(false);

        boolean resultado = servicioSuscripcion.estaAlumnoSuscritoAProfesor(alumnoId, profesorId);

        assertFalse(resultado);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(profesorId);
        verify(alumnoMock, times(1)).estaSuscritoAProfesor(profesorMock);
    }

    @Test
    public void estaAlumnoSuscritoAProfesorDeberiaRetornarFalseCuandoElUsuarioAlumnoNoEsInstanciaDeAlumno() {
        Long alumnoId = 1L;
        Long profesorId = 2L;

        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(usuarioGenericoMock);
        when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(profesorMock);

        boolean resultado = servicioSuscripcion.estaAlumnoSuscritoAProfesor(alumnoId, profesorId);

        assertFalse(resultado);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(profesorId);
    }

    @Test
    public void estaAlumnoSuscritoAProfesorDeberiaRetornarFalseCuandoElUsuarioProfesorNoEsInstanciaDeProfesor() {
        Long alumnoId = 1L;
        Long profesorId = 2L;

        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(usuarioGenericoMock);

        boolean resultado = servicioSuscripcion.estaAlumnoSuscritoAProfesor(alumnoId, profesorId);

        assertFalse(resultado);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(profesorId);
    }

    @Test
    public void estaAlumnoSuscritoAProfesorDeberiaRetornarFalseCuandoElAlumnoNoExiste() {
        Long alumnoId = 1L;
        Long profesorId = 2L;

        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(null);
        when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(profesorMock);

        boolean resultado = servicioSuscripcion.estaAlumnoSuscritoAProfesor(alumnoId, profesorId);

        assertFalse(resultado);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(profesorId);
    }

    @Test
    public void estaAlumnoSuscritoAProfesorDeberiaRetornarFalseCuandoElProfesorNoExiste() {
        Long alumnoId = 1L;
        Long profesorId = 2L;

        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(profesorId)).thenReturn(null);

        boolean resultado = servicioSuscripcion.estaAlumnoSuscritoAProfesor(alumnoId, profesorId);

        assertFalse(resultado);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(profesorId);
    }

    @Test
    public void suscribirAlumnoAProfesorDeberiaRetornarFalseCuandoOcurreUnaExcepcion() {
        Long alumnoId = 1L;
        Long profesorId = 2L;

        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenThrow(new RuntimeException("Error de base de datos"));

        boolean resultado = servicioSuscripcion.suscribirAlumnoAProfesor(alumnoId, profesorId);

        assertFalse(resultado);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
    }

    @Test
    public void desuscribirAlumnoDeProfesorDeberiaRetornarFalseCuandoOcurreUnaExcepcion() {
        Long alumnoId = 1L;
        Long profesorId = 2L;

        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenThrow(new RuntimeException("Error de base de datos"));

        boolean resultado = servicioSuscripcion.desuscribirAlumnoDeProfesor(alumnoId, profesorId);

        assertFalse(resultado);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
    }

    @Test
    public void estaAlumnoSuscritoAProfesorDeberiaRetornarFalseCuandoOcurreUnaExcepcion() {
        Long alumnoId = 1L;
        Long profesorId = 2L;

        when(repositorioUsuarioMock.buscarPorId(alumnoId)).thenThrow(new RuntimeException("Error de base de datos"));

        boolean resultado = servicioSuscripcion.estaAlumnoSuscritoAProfesor(alumnoId, profesorId);

        assertFalse(resultado);
        verify(repositorioUsuarioMock, times(1)).buscarPorId(alumnoId);
    }
}
