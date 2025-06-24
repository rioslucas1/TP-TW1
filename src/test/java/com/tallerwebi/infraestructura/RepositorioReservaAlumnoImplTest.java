package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioReservaAlumno;
import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.dominio.entidades.disponibilidadProfesor;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RepositorioReservaAlumnoImplTest {

    private SessionFactory sessionFactoryMock;
    private Session sessionMock;
    private Criteria criteriaMock;
    private RepositorioReservaAlumno repositorioReservaAlumno;
    private disponibilidadProfesor disponibilidadMock;
    private Profesor profesorMock;

    @BeforeEach
    public void init() {
        sessionFactoryMock = mock(SessionFactory.class);
        sessionMock = mock(Session.class);
        criteriaMock = mock(Criteria.class);
        repositorioReservaAlumno = new RepositorioReservaAlumnoImpl(sessionFactoryMock);

        when(sessionFactoryMock.getCurrentSession()).thenReturn(sessionMock);
        when(sessionMock.createCriteria(disponibilidadProfesor.class)).thenReturn(criteriaMock);
        when(criteriaMock.createAlias("profesor", "p")).thenReturn(criteriaMock);
        when(criteriaMock.add(any())).thenReturn(criteriaMock);

        profesorMock = mock(Profesor.class);
        when(profesorMock.getEmail()).thenReturn("profesor@unlam.com");

        disponibilidadMock = mock(disponibilidadProfesor.class);
        when(disponibilidadMock.getId()).thenReturn(1L);
        when(disponibilidadMock.getEmailProfesor()).thenReturn("profesor@unlam.com");
        when(disponibilidadMock.getDiaSemana()).thenReturn("LUNES");
        when(disponibilidadMock.getHora()).thenReturn("09:00");
        when(disponibilidadMock.getProfesor()).thenReturn(profesorMock);
    }

    @Test
    public void buscarPorProfesorDeberiaRetornarListaDeDisponibilidades() {
        String emailProfesor = "profesor@unlam.com";
        disponibilidadProfesor disponibilidad1 = mock(disponibilidadProfesor.class);
        disponibilidadProfesor disponibilidad2 = mock(disponibilidadProfesor.class);
        List<disponibilidadProfesor> disponibilidadesEsperadas = Arrays.asList(disponibilidad1, disponibilidad2);

        when(criteriaMock.list()).thenReturn(disponibilidadesEsperadas);

        List<disponibilidadProfesor> disponibilidadesObtenidas = repositorioReservaAlumno.buscarPorProfesor(emailProfesor);

        assertNotNull(disponibilidadesObtenidas);
        assertEquals(2, disponibilidadesObtenidas.size());
        assertThat(disponibilidadesObtenidas, containsInAnyOrder(disponibilidad1, disponibilidad2));
        verify(sessionMock, times(1)).createCriteria(disponibilidadProfesor.class);
        verify(criteriaMock, times(1)).createAlias("profesor", "p");
        verify(criteriaMock, times(1)).list();
    }

    @Test
    public void buscarPorProfesorInexistenteDeberiaRetornarListaVacia() {
        String emailProfesor = "inexistente@unlam.com";
        List<disponibilidadProfesor> listaVacia = Arrays.asList();

        when(criteriaMock.list()).thenReturn(listaVacia);

        List<disponibilidadProfesor> disponibilidadesObtenidas = repositorioReservaAlumno.buscarPorProfesor(emailProfesor);

        assertNotNull(disponibilidadesObtenidas);
        assertEquals(0, disponibilidadesObtenidas.size());
        verify(sessionMock, times(1)).createCriteria(disponibilidadProfesor.class);
        verify(criteriaMock, times(1)).createAlias("profesor", "p");
        verify(criteriaMock, times(1)).list();
    }

    @Test
    public void buscarPorProfesorDiaHoraDeberiaRetornarDisponibilidad() {
        String emailProfesor = "profesor@unlam.com";
        String diaSemana = "LUNES";
        String hora = "09:00";

        when(criteriaMock.uniqueResult()).thenReturn(disponibilidadMock);

        disponibilidadProfesor disponibilidadObtenida = repositorioReservaAlumno.buscarPorProfesorDiaHora(emailProfesor, diaSemana, hora);

        assertNotNull(disponibilidadObtenida);
        assertEquals(disponibilidadMock, disponibilidadObtenida);
        verify(sessionMock, times(1)).createCriteria(disponibilidadProfesor.class);
        verify(criteriaMock, times(1)).uniqueResult();
    }

    @Test
    public void buscarPorProfesorDiaHoraInexistenteDeberiaRetornarNull() {
        String emailProfesor = "profesor@unlam.com";
        String diaSemana = "MARTES";
        String hora = "15:00";

        when(criteriaMock.uniqueResult()).thenReturn(null);

        disponibilidadProfesor disponibilidadObtenida = repositorioReservaAlumno.buscarPorProfesorDiaHora(emailProfesor, diaSemana, hora);

        assertNull(disponibilidadObtenida);
        verify(sessionMock, times(1)).createCriteria(disponibilidadProfesor.class);
        verify(criteriaMock, times(1)).uniqueResult();
    }

    @Test
    public void buscarPorIdDeberiaRetornarDisponibilidad() {
        Long id = 1L;

        when(sessionMock.get(disponibilidadProfesor.class, id)).thenReturn(disponibilidadMock);

        disponibilidadProfesor disponibilidadObtenida = repositorioReservaAlumno.buscarPorId(id);

        assertNotNull(disponibilidadObtenida);
        assertEquals(disponibilidadMock, disponibilidadObtenida);
        verify(sessionMock, times(1)).get(disponibilidadProfesor.class, id);
    }

    @Test
    public void buscarPorIdInexistenteDeberiaRetornarNull() {
        Long id = 999L;

        when(sessionMock.get(disponibilidadProfesor.class, id)).thenReturn(null);

        disponibilidadProfesor disponibilidadObtenida = repositorioReservaAlumno.buscarPorId(id);

        assertNull(disponibilidadObtenida);
        verify(sessionMock, times(1)).get(disponibilidadProfesor.class, id);
    }

    @Test
    public void guardarDisponibilidadDeberiaLlamarSaveOrUpdateEnSession() {
        repositorioReservaAlumno.guardar(disponibilidadMock);

        verify(sessionMock, times(1)).saveOrUpdate(disponibilidadMock);
    }

    @Test
    public void buscarPorProfesorDiaFechaDeberiaRetornarListaDeDisponibilidades() {
        String emailProfesor = "profesor@unlam.com";
        String diaSemana = "LUNES";
        LocalDate fechaEspecifica = LocalDate.of(2024, 12, 16);

        disponibilidadProfesor disponibilidad1 = mock(disponibilidadProfesor.class);
        disponibilidadProfesor disponibilidad2 = mock(disponibilidadProfesor.class);
        List<disponibilidadProfesor> disponibilidadesEsperadas = Arrays.asList(disponibilidad1, disponibilidad2);

        when(criteriaMock.list()).thenReturn(disponibilidadesEsperadas);

        List<disponibilidadProfesor> disponibilidadesObtenidas = repositorioReservaAlumno.buscarPorProfesorDiaFecha(
                emailProfesor, diaSemana, fechaEspecifica);

        assertNotNull(disponibilidadesObtenidas);
        assertEquals(2, disponibilidadesObtenidas.size());
        assertThat(disponibilidadesObtenidas, containsInAnyOrder(disponibilidad1, disponibilidad2));
        verify(sessionMock, times(1)).createCriteria(disponibilidadProfesor.class);
        verify(criteriaMock, times(1)).list();
    }

    @Test
    public void buscarPorProfesorDiaFechaSinResultadosDeberiaRetornarListaVacia() {
        String emailProfesor = "profesor@unlam.com";
        String diaSemana = "DOMINGO";
        LocalDate fechaEspecifica = LocalDate.of(2024, 12, 22);
        List<disponibilidadProfesor> listaVacia = Arrays.asList();

        when(criteriaMock.list()).thenReturn(listaVacia);

        List<disponibilidadProfesor> disponibilidadesObtenidas = repositorioReservaAlumno.buscarPorProfesorDiaFecha(
                emailProfesor, diaSemana, fechaEspecifica);

        assertNotNull(disponibilidadesObtenidas);
        assertEquals(0, disponibilidadesObtenidas.size());
        verify(sessionMock, times(1)).createCriteria(disponibilidadProfesor.class);
        verify(criteriaMock, times(1)).list();
    }

    @Test
    public void buscarPorProfesorConEmailNullDeberiaFuncionar() {
        String emailProfesor = null;
        List<disponibilidadProfesor> listaVacia = Arrays.asList();

        when(criteriaMock.list()).thenReturn(listaVacia);

        List<disponibilidadProfesor> disponibilidadesObtenidas = repositorioReservaAlumno.buscarPorProfesor(emailProfesor);

        assertNotNull(disponibilidadesObtenidas);
        assertEquals(0, disponibilidadesObtenidas.size());
        verify(sessionMock, times(1)).createCriteria(disponibilidadProfesor.class);
        verify(criteriaMock, times(1)).createAlias("profesor", "p");
        verify(criteriaMock, times(1)).list();
    }

    @Test
    public void buscarPorProfesorDiaHoraConParametrosNullDeberiaRetornarNull() {
        String emailProfesor = null;
        String diaSemana = null;
        String hora = null;

        when(criteriaMock.uniqueResult()).thenReturn(null);

        disponibilidadProfesor disponibilidadObtenida = repositorioReservaAlumno.buscarPorProfesorDiaHora(emailProfesor, diaSemana, hora);

        assertNull(disponibilidadObtenida);
        verify(sessionMock, times(1)).createCriteria(disponibilidadProfesor.class);
        verify(criteriaMock, times(1)).uniqueResult();
    }

    @Test
    public void buscarPorIdConIdNullDeberiaRetornarNull() {
        Long id = null;

        when(sessionMock.get(disponibilidadProfesor.class, id)).thenReturn(null);

        disponibilidadProfesor disponibilidadObtenida = repositorioReservaAlumno.buscarPorId(id);

        assertNull(disponibilidadObtenida);
        verify(sessionMock, times(1)).get(disponibilidadProfesor.class, id);
    }

    @Test
    public void buscarPorProfesorConUnSoloResultadoDeberiaRetornarListaConUnElemento() {
        String emailProfesor = "profesor@unlam.com";
        List<disponibilidadProfesor> disponibilidadesEsperadas = Arrays.asList(disponibilidadMock);

        when(criteriaMock.list()).thenReturn(disponibilidadesEsperadas);

        List<disponibilidadProfesor> disponibilidadesObtenidas = repositorioReservaAlumno.buscarPorProfesor(emailProfesor);

        assertNotNull(disponibilidadesObtenidas);
        assertEquals(1, disponibilidadesObtenidas.size());
        assertEquals(disponibilidadMock, disponibilidadesObtenidas.get(0));
        verify(sessionMock, times(1)).createCriteria(disponibilidadProfesor.class);
        verify(criteriaMock, times(1)).createAlias("profesor", "p");
        verify(criteriaMock, times(1)).list();
    }

}