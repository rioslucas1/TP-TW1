package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Usuario;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RepositorioTemaImplTest {


    private SessionFactory sessionFactoryMock;
    private Session sessionMock;
    private Criteria criteriaMock;
    private RepositorioUsuario repositorioUsuarioMock;
    private Alumno alumnoMock;
    private Profesor profesorMock;
    private Usuario usuarioMock;


    @BeforeEach
    public void init(){

        sessionFactoryMock = mock(SessionFactory.class);
        sessionMock = mock(Session.class);
        criteriaMock = mock(Criteria.class);
        repositorioUsuarioMock = new RepositorioUsuarioImpl(sessionFactoryMock);
        when(sessionFactoryMock.getCurrentSession()).thenReturn(sessionMock);
        when(sessionMock.createCriteria(any(Class.class))).thenReturn(criteriaMock);
        when(criteriaMock.add(any())).thenReturn(criteriaMock);


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
    public void buscarUsuarioConEmailYPasswordCorrectosDeberiaRetornarUsuario() {
        repositorioUsuarioMock.guardar(alumnoMock);
        when(criteriaMock.uniqueResult()).thenReturn(alumnoMock);
        Usuario usuarioEncontrado = repositorioUsuarioMock.buscarUsuario("alumno@unlam.com", "123456");

        assertNotNull(usuarioEncontrado);
        assertEquals("alumno@unlam.com", usuarioEncontrado.getEmail());
        assertEquals("123456", usuarioEncontrado.getPassword());
        assertEquals("Nombre", usuarioEncontrado.getNombre());
        assertEquals("Apellido", usuarioEncontrado.getApellido());
        verify(sessionMock, times(1)).createCriteria(Usuario.class);
    }

    @Test
    public void buscarUsuarioConEmailYPasswordIncorrectosDeberiaRetornarNull() {
        String email = "usuario@inexistente.com";
        String password = "passwordIncorrecto";
        when(criteriaMock.uniqueResult()).thenReturn(null);

        Usuario usuarioEncontrado = repositorioUsuarioMock.buscarUsuario(email, password);

        assertNull(usuarioEncontrado);
        verify(sessionMock, times(1)).createCriteria(Usuario.class);
        verify(criteriaMock, times(1)).uniqueResult();
    }

    @Test
    public void guardarUsuarioDeberiaLlamarSaveEnSession() {
        repositorioUsuarioMock.guardar(usuarioMock);

        verify(sessionMock, times(1)).save(usuarioMock);
    }

    @Test
    public void guardarAlumnoDeberiaLlamarSaveEnSession() {
        repositorioUsuarioMock.guardar(alumnoMock);

        verify(sessionMock, times(1)).save(alumnoMock);
    }

    @Test
    public void guardarProfesorDeberiaLlamarSaveEnSession() {
        repositorioUsuarioMock.guardar(profesorMock);

        verify(sessionMock, times(1)).save(profesorMock);
    }

    @Test
    public void buscarUsuarioPorEmailDeberiaRetornarUsuario() {
        String email = "usuario@unlam.com";
        when(criteriaMock.uniqueResult()).thenReturn(usuarioMock);

        Usuario usuarioEncontrado = repositorioUsuarioMock.buscar(email);

        assertNotNull(usuarioEncontrado);
        assertEquals(usuarioMock, usuarioEncontrado);
        verify(sessionMock, times(1)).createCriteria(Usuario.class);
        verify(criteriaMock, times(1)).uniqueResult();
    }

    @Test
    public void buscarUsuarioPorEmailInexistenteDeberiaRetornarNull() {
        String email = "inexistente@unlam.com";
        when(criteriaMock.uniqueResult()).thenReturn(null);

        Usuario usuarioEncontrado = repositorioUsuarioMock.buscar(email);

        assertNull(usuarioEncontrado);
        verify(sessionMock, times(1)).createCriteria(Usuario.class);
        verify(criteriaMock, times(1)).uniqueResult();
    }

    @Test
    public void modificarUsuarioDeberiaLlamarUpdateEnSession() {
        repositorioUsuarioMock.modificar(usuarioMock);

        verify(sessionMock, times(1)).update(usuarioMock);
    }

    @Test
    public void modificarAlumnoDeberiaLlamarUpdateEnSession() {
        repositorioUsuarioMock.modificar(alumnoMock);

        verify(sessionMock, times(1)).update(alumnoMock);
    }

    @Test
    public void modificarProfesorDeberiaLlamarUpdateEnSession() {
        repositorioUsuarioMock.modificar(profesorMock);

        verify(sessionMock, times(1)).update(profesorMock);
    }

    @Test
    public void buscarPorTipoProfesoresDeberiaRetornarListaDeProfesores() {
        Profesor profesor1 = mock(Profesor.class);
        Profesor profesor2 = mock(Profesor.class);
        List<Usuario> profesoresEsperados = Arrays.asList(profesor1, profesor2);

        when(criteriaMock.list()).thenReturn(profesoresEsperados);
        List<Usuario> profesoresObtenidos = repositorioUsuarioMock.buscarPorTipo(Profesor.class);
        assertNotNull(profesoresObtenidos);
        assertEquals(2, profesoresObtenidos.size());
        assertThat(profesoresObtenidos, containsInAnyOrder(profesor1, profesor2));
        verify(sessionMock, times(1)).createCriteria(Profesor.class);
        verify(criteriaMock, times(1)).list();
    }

    @Test
    public void buscarPorTipoAlumnosDeberiaRetornarListaDeAlumnos() {
        Alumno alumno1 = mock(Alumno.class);
        Alumno alumno2 = mock(Alumno.class);
        List<Usuario> alumnosEsperados = Arrays.asList(alumno1, alumno2);
        when(criteriaMock.list()).thenReturn(alumnosEsperados);
        List<Usuario> alumnosObtenidos = repositorioUsuarioMock.buscarPorTipo(Alumno.class);

        assertNotNull(alumnosObtenidos);
        assertEquals(2, alumnosObtenidos.size());
        assertThat(alumnosObtenidos, containsInAnyOrder(alumno1, alumno2));
        verify(sessionMock, times(1)).createCriteria(Alumno.class);
        verify(criteriaMock, times(1)).list();
    }

    @Test
    public void buscarPorTipoSinResultadosDeberiaRetornarListaVacia() {
        List<Usuario> listaVacia = Arrays.asList();
        when(criteriaMock.list()).thenReturn(listaVacia);
        List<Usuario> profesoresObtenidos = repositorioUsuarioMock.buscarPorTipo(Profesor.class);

        assertNotNull(profesoresObtenidos);
        assertEquals(0, profesoresObtenidos.size());
        assertEquals(listaVacia, profesoresObtenidos);
        verify(sessionMock, times(1)).createCriteria(Profesor.class);
        verify(criteriaMock, times(1)).list();
    }

    @Test
    public void buscarUsuarioConEmailNullDeberiaRetornarNull() {
        String email = null;
        String password = "123456";
        when(criteriaMock.uniqueResult()).thenReturn(null);
        Usuario usuarioEncontrado = repositorioUsuarioMock.buscarUsuario(email, password);

        assertNull(usuarioEncontrado);
        verify(sessionMock, times(1)).createCriteria(Usuario.class);
        verify(criteriaMock, times(1)).uniqueResult();
    }

    @Test
    public void buscarUsuarioConPasswordNullDeberiaRetornarNull() {
        String email = "usuario@unlam.com";
        String password = null;
        when(criteriaMock.uniqueResult()).thenReturn(null);

        Usuario usuarioEncontrado = repositorioUsuarioMock.buscarUsuario(email, password);

        assertNull(usuarioEncontrado);
        verify(sessionMock, times(1)).createCriteria(Usuario.class);
        verify(criteriaMock, times(1)).uniqueResult();
    }

    @Test
    public void buscarUsuarioConEmailVacioDeberiaRetornarNull() {
        String email = "";
        String password = "123456";
        when(criteriaMock.uniqueResult()).thenReturn(null);

        Usuario usuarioEncontrado = repositorioUsuarioMock.buscarUsuario(email, password);

        assertNull(usuarioEncontrado);
        verify(sessionMock, times(1)).createCriteria(Usuario.class);
        verify(criteriaMock, times(1)).uniqueResult();
    }

    @Test
    public void buscarUsuarioConPasswordVaciaDeberiaRetornarNull() {
        String email = "usuario@unlam.com";
        String password = "";
        when(criteriaMock.uniqueResult()).thenReturn(null);

        Usuario usuarioEncontrado = repositorioUsuarioMock.buscarUsuario(email, password);

        assertNull(usuarioEncontrado);
        verify(sessionMock, times(1)).createCriteria(Usuario.class);
        verify(criteriaMock, times(1)).uniqueResult();
    }

    @Test
    public void buscarPorEmailNullDeberiaRetornarNull() {
        String email = null;
        when(criteriaMock.uniqueResult()).thenReturn(null);

        Usuario usuarioEncontrado = repositorioUsuarioMock.buscar(email);

        assertNull(usuarioEncontrado);
        verify(sessionMock, times(1)).createCriteria(Usuario.class);
        verify(criteriaMock, times(1)).uniqueResult();
    }

    @Test
    public void buscarPorEmailVacioDeberiaRetornarNull() {
        String email = "";
        when(criteriaMock.uniqueResult()).thenReturn(null);

        Usuario usuarioEncontrado = repositorioUsuarioMock.buscar(email);

        assertNull(usuarioEncontrado);
        verify(sessionMock, times(1)).createCriteria(Usuario.class);
        verify(criteriaMock, times(1)).uniqueResult();
    }

    @Test
    public void buscarProfesorConCredencialesCorrectasDeberiaRetornarProfesor() {
        String email = "profesor@unlam.com";
        String password = "123456";
        when(criteriaMock.uniqueResult()).thenReturn(profesorMock);

        Usuario usuarioEncontrado = repositorioUsuarioMock.buscarUsuario(email, password);

        assertNotNull(usuarioEncontrado);
        assertEquals(profesorMock, usuarioEncontrado);
        verify(sessionMock, times(1)).createCriteria(Usuario.class);
        verify(criteriaMock, times(1)).uniqueResult();
    }

    @Test
    public void buscarPorTipoConUnSoloProfesorDeberiaRetornarListaConUnElemento() {
        List<Usuario> profesoresEsperados = Arrays.asList(profesorMock);
        when(criteriaMock.list()).thenReturn(profesoresEsperados);

        List<Usuario> profesoresObtenidos = repositorioUsuarioMock.buscarPorTipo(Profesor.class);

        assertNotNull(profesoresObtenidos);
        assertEquals(1, profesoresObtenidos.size());
        assertEquals(profesorMock, profesoresObtenidos.get(0));
        verify(sessionMock, times(1)).createCriteria(Profesor.class);
        verify(criteriaMock, times(1)).list();
    }


}
