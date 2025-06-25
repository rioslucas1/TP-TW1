package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.dominio.servicios.ServicioTema;
import com.tallerwebi.dominio.RepositorioUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ControladorPerfilTest {

    private ControladorPerfil controladorPerfil;
    private Alumno alumnoMock;
    private Profesor profesorMock;
    private Tema temaMock;
    private Usuario usuarioGenericoMock;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;
    private ServicioTema servicioTemaMock;
    private RepositorioUsuario repositorioUsuarioMock;
    private RedirectAttributes redirectAttributesMock;

    @BeforeEach
    public void init() {

        alumnoMock = mock(Alumno.class);
        profesorMock = mock(Profesor.class);
        usuarioGenericoMock = mock(Usuario.class);
        temaMock = mock(Tema.class);
        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);
        servicioTemaMock = mock(ServicioTema.class);
        repositorioUsuarioMock = mock(RepositorioUsuario.class);
        redirectAttributesMock = mock(RedirectAttributes.class);

        when(alumnoMock.getId()).thenReturn(1L);
        when(alumnoMock.getNombre()).thenReturn("nombre");
        when(alumnoMock.getApellido()).thenReturn("apellido");
        when(alumnoMock.getEmail()).thenReturn("alumno@unlam.com");
        when(alumnoMock.getDescripcion()).thenReturn("descripcion");
        when(alumnoMock.getModalidadPreferida()).thenReturn(ModalidadPreferida.PRESENCIAL);
        when(alumnoMock.getTemas()).thenReturn(new ArrayList<>());

        when(profesorMock.getId()).thenReturn(2L);
        when(profesorMock.getNombre()).thenReturn("Profesor");
        when(profesorMock.getApellido()).thenReturn("apellidop");

        when(temaMock.getId()).thenReturn(1L);
        when(temaMock.getNombre()).thenReturn("Matemáticas");

        when(requestMock.getSession()).thenReturn(sessionMock);

        controladorPerfil = new ControladorPerfil(servicioTemaMock, repositorioUsuarioMock);
    }

    @Test
    public void verPerfilConAlumnoLogueadoDeberiaRetornarVistaConDatos() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);

        List<Tema> temas = Arrays.asList(temaMock);
        when(servicioTemaMock.obtenerTodos()).thenReturn(temas);

        ModelAndView modelAndView = controladorPerfil.verPerfil(requestMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("verPerfil"));
        assertEquals(modelAndView.getModel().get("alumno"), alumnoMock);
        assertEquals(modelAndView.getModel().get("todosLosTemas"), temas);
        assertFalse((Boolean) modelAndView.getModel().get("esEdicion"));
        verify(servicioTemaMock, times(1)).obtenerTodos();
    }

    @Test
    public void verPerfilSinUsuarioLogueadoDeberiaRedirigirALogin() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

        ModelAndView modelAndView = controladorPerfil.verPerfil(requestMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
        verify(servicioTemaMock, never()).obtenerTodos();
    }

    @Test
    public void verPerfilConProfesorLogueadoDeberiaRedirigirAPerfilProfesor() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);

        ModelAndView modelAndView = controladorPerfil.verPerfil(requestMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/profesor/perfil"));
        verify(servicioTemaMock, never()).obtenerTodos();
    }

    @Test
    public void verPerfilConUsuarioGenericoDeberiaRedirigirAHome() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioGenericoMock);

        ModelAndView modelAndView = controladorPerfil.verPerfil(requestMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
        verify(servicioTemaMock, never()).obtenerTodos();
    }

    @Test
    public void editarPerfilConAlumnoLogueadoDeberiaRetornarVistaEnModoEdicion() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);
        List<Tema> temas = Arrays.asList(temaMock);
        when(servicioTemaMock.obtenerTodos()).thenReturn(temas);

        ModelAndView modelAndView = controladorPerfil.editarPerfil(requestMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("verPerfil"));
        assertEquals(modelAndView.getModel().get("alumno"), alumnoMock);
        assertEquals(modelAndView.getModel().get("todosLosTemas"), temas);
        assertTrue((Boolean) modelAndView.getModel().get("esEdicion"));
        verify(servicioTemaMock, times(1)).obtenerTodos();
    }

    @Test
    public void editarPerfilSinUsuarioLogueadoDeberiaRedirigirALogin() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

        ModelAndView modelAndView = controladorPerfil.editarPerfil(requestMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
        verify(servicioTemaMock, never()).obtenerTodos();
    }

    @Test
    public void editarPerfilConProfesorDeberiaRedirigirAHome() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);

        ModelAndView modelAndView = controladorPerfil.editarPerfil(requestMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
        verify(servicioTemaMock, never()).obtenerTodos();
    }

    @Test
    public void actualizarPerfilConDatosValidosDeberiaActualizarYRedirigir() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);
        String nombre = "nombre";
        String apellido = "apellido";
        String descripcion = "descripción";
        String modalidadPreferida = "VIRTUAL";

        ModelAndView modelAndView = controladorPerfil.actualizarPerfil(
                nombre, apellido, descripcion, modalidadPreferida, null, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/perfil"));
        verify(alumnoMock, times(1)).setNombre(nombre);
        verify(alumnoMock, times(1)).setApellido(apellido);
        verify(alumnoMock, times(1)).setDescripcion(descripcion);
        verify(alumnoMock, times(1)).setModalidadPreferida(ModalidadPreferida.VIRTUAL);
        verify(repositorioUsuarioMock, times(1)).modificar(alumnoMock);
        verify(sessionMock, times(1)).setAttribute("USUARIO", alumnoMock);
    }

    @Test
    public void actualizarPerfilConFotoBase64ValidaDeberiaActualizarFoto() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);
        String fotoBase64Valida = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";

        ModelAndView modelAndView = controladorPerfil.actualizarPerfil(
                "nombre", "apellido", "descripcion", "PRESENCIAL", fotoBase64Valida, requestMock, redirectAttributesMock);

        verify(alumnoMock, times(1)).setFotoPerfil(fotoBase64Valida);
        verify(repositorioUsuarioMock, times(1)).modificar(alumnoMock);
    }

    @Test
    public void actualizarPerfilConFotoBase64InvalidaDeberiaMostrarError() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);
        String fotoBase64Invalida = "imagen_invalida";

        ModelAndView modelAndView = controladorPerfil.actualizarPerfil(
                "nombre", "apellido", "descripcion", "PRESENCIAL", fotoBase64Invalida, requestMock, redirectAttributesMock);

        verify(alumnoMock, never()).setFotoPerfil(any());
        verify(repositorioUsuarioMock, never()).modificar(any());

    assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/perfil/editar"));
    }

    @Test
    public void actualizarPerfilSinUsuarioLogueadoDeberiaRedirigirALogin() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

        ModelAndView modelAndView = controladorPerfil.actualizarPerfil(
                "Nombre", "Apellido", "Descripción", "PRESENCIAL", null, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
        verify(repositorioUsuarioMock, never()).modificar(any());
    }

    @Test
    public void actualizarPerfilConProfesorDeberiaRedirigirAHome() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);

        ModelAndView modelAndView = controladorPerfil.actualizarPerfil(
                "Nombre", "Apellido", "Descripción", "PRESENCIAL", null, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
        verify(repositorioUsuarioMock, never()).modificar(any());
    }

    @Test
    public void actualizarPerfilConModalidadVaciaDeberiaAsignarNull() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);

        ModelAndView modelAndView = controladorPerfil.actualizarPerfil(
                "Nombre", "Apellido", "Descripción", "", null, requestMock, redirectAttributesMock);

        verify(alumnoMock, times(1)).setModalidadPreferida(null);
        verify(repositorioUsuarioMock, times(1)).modificar(alumnoMock);
    }

    @Test
    public void actualizarPerfilConErrorDeberiaAgregarMensajeDeError() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);
        doThrow(new RuntimeException("Error de base de datos")).when(repositorioUsuarioMock).modificar(alumnoMock);

        ModelAndView modelAndView = controladorPerfil.actualizarPerfil(
                "Nombre", "Apellido", "Descripción", "PRESENCIAL", null, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/perfil"));
    }

    @Test
    public void eliminarFotoConAlumnoLogueadoDeberiaEliminarFotoYRedirigir() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);

        ModelAndView modelAndView = controladorPerfil.eliminarFoto(requestMock, redirectAttributesMock);

        verify(alumnoMock, times(1)).setFotoPerfil(null);
        verify(repositorioUsuarioMock, times(1)).modificar(alumnoMock);
        verify(sessionMock, times(1)).setAttribute("USUARIO", alumnoMock);
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/perfil"));
    }

    @Test
    public void eliminarFotoSinUsuarioLogueadoDeberiaRedirigirALogin() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

        ModelAndView modelAndView = controladorPerfil.eliminarFoto(requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
        verify(repositorioUsuarioMock, never()).modificar(any());
    }

    @Test
    public void eliminarFotoConProfesorDeberiaRedirigirAHome() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);

        ModelAndView modelAndView = controladorPerfil.eliminarFoto(requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
        verify(repositorioUsuarioMock, never()).modificar(any());
    }

    @Test
    public void eliminarFotoConErrorDeberiaAgregarMensajeDeError() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);
        doThrow(new RuntimeException("Error de base de datos")).when(repositorioUsuarioMock).modificar(alumnoMock);

        ModelAndView modelAndView = controladorPerfil.eliminarFoto(requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/perfil"));
    }

    @Test
    public void agregarTemaValidoDeberiaAgregarTemaYRedirigir() {
        Long temaId = 1L;
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);
        List<Tema> temasPreferidos = mock(List.class);
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(servicioTemaMock.obtenerPorId(temaId)).thenReturn(temaMock);
        when(alumnoMock.getTemas()).thenReturn(temasPreferidos);

        ModelAndView modelAndView = controladorPerfil.agregarTema(temaId, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/perfil"));
        verify(temasPreferidos, times(1)).add(temaMock);
        verify(repositorioUsuarioMock, times(1)).modificar(alumnoMock);
        verify(sessionMock, times(1)).setAttribute("USUARIO", alumnoMock);
    }

    @Test
    public void agregarTemaSinUsuarioLogueadoDeberiaRedirigirALogin() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

        ModelAndView modelAndView = controladorPerfil.agregarTema(1L, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
        verify(servicioTemaMock, never()).obtenerPorId(any());
        verify(repositorioUsuarioMock, never()).modificar(any());
    }

    @Test
    public void agregarTemaConProfesorDeberiaRedirigirAHome() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);

        ModelAndView modelAndView = controladorPerfil.agregarTema(1L, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
        verify(servicioTemaMock, never()).obtenerPorId(any());
        verify(repositorioUsuarioMock, never()).modificar(any());
    }

    @Test
    public void agregarTemaInexistenteDeberiaMostrarError() {
        Long temaId = 999L;
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);
        when(servicioTemaMock.obtenerPorId(temaId)).thenReturn(null);

        ModelAndView modelAndView = controladorPerfil.agregarTema(temaId, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/perfil"));
        verify(repositorioUsuarioMock, never()).modificar(any());
    }

    @Test
    public void agregarTemaYaExistenteDeberiaMostrarError() {
        Long temaId = 1L;
        List<Tema> temasPreferidos = Arrays.asList(temaMock);

        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);
        when(servicioTemaMock.obtenerPorId(temaId)).thenReturn(temaMock);
        when(alumnoMock.getTemas()).thenReturn(temasPreferidos);

        ModelAndView modelAndView = controladorPerfil.agregarTema(temaId, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/perfil"));
        verify(repositorioUsuarioMock, never()).modificar(any());
    }

    @Test
    public void agregarTemaConErrorDeberiaAgregarMensajeDeError() {
        Long temaId = 1L;
        List<Tema> temasPreferidos = new ArrayList<>();
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);
        when(servicioTemaMock.obtenerPorId(temaId)).thenReturn(temaMock);
        when(alumnoMock.getTemas()).thenReturn(temasPreferidos);
        doThrow(new RuntimeException("Error de base de datos")).when(repositorioUsuarioMock).modificar(alumnoMock);

        ModelAndView modelAndView = controladorPerfil.agregarTema(temaId, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/perfil"));
    }

    @Test
    public void eliminarTemaValidoDeberiaEliminarTemaYRedirigir() {
        Long temaId = 1L;
        List<Tema> temasPreferidos = spy(new ArrayList<>());
        temasPreferidos.add(temaMock);
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);
        when(servicioTemaMock.obtenerPorId(temaId)).thenReturn(temaMock);
        when(alumnoMock.getTemas()).thenReturn(temasPreferidos);

        ModelAndView modelAndView = controladorPerfil.eliminarTema(temaId, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/perfil"));
        verify(temasPreferidos, times(1)).remove(temaMock);
        verify(repositorioUsuarioMock, times(1)).modificar(alumnoMock);
        verify(sessionMock, times(1)).setAttribute("USUARIO", alumnoMock);
    }

    @Test
    public void eliminarTemaSinUsuarioLogueadoDeberiaRedirigirALogin() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

        ModelAndView modelAndView = controladorPerfil.eliminarTema(1L, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
        verify(servicioTemaMock, never()).obtenerPorId(any());
        verify(repositorioUsuarioMock, never()).modificar(any());
    }

    @Test
    public void eliminarTemaConProfesorDeberiaRedirigirAHome() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesorMock);

        ModelAndView modelAndView = controladorPerfil.eliminarTema(1L, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/home"));
        verify(servicioTemaMock, never()).obtenerPorId(any());
        verify(repositorioUsuarioMock, never()).modificar(any());
    }

    @Test
    public void eliminarTemaInexistenteDeberiaMostrarError() {
        Long temaId = 999L;
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);
        when(servicioTemaMock.obtenerPorId(temaId)).thenReturn(null);

        ModelAndView modelAndView = controladorPerfil.eliminarTema(temaId, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/perfil"));
        verify(repositorioUsuarioMock, never()).modificar(any());
    }

    @Test
    public void eliminarTemaConErrorDeberiaAgregarMensajeDeError() {
        Long temaId = 1L;
        List<Tema> temasPreferidos = new ArrayList<>();
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);
        when(servicioTemaMock.obtenerPorId(temaId)).thenReturn(temaMock);
        when(alumnoMock.getTemas()).thenReturn(temasPreferidos);
        doThrow(new RuntimeException("Error de base de datos")).when(repositorioUsuarioMock).modificar(alumnoMock);

        ModelAndView modelAndView = controladorPerfil.eliminarTema(temaId, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/perfil"));
    }

    @Test
    public void actualizarPerfilConModalidadInvalidaDeberiaAsignarNull() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);

        ModelAndView modelAndView = controladorPerfil.actualizarPerfil(
                "Nombre", "Apellido", "Descripción", "INVALIDO", null, requestMock, redirectAttributesMock
        );

        verify(alumnoMock, times(1)).setModalidadPreferida(null);
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/perfil"));
    }
    @Test
    public void verPerfilConUsuarioNullDeberiaRedirigirALogin() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(null);

        ModelAndView modelAndView = controladorPerfil.verPerfil(requestMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/login"));
    }


    @Test
    public void actualizarPerfilConDescripcionNulaDeberiaAsignarNull() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);

        ModelAndView modelAndView = controladorPerfil.actualizarPerfil(
                "Nombre", "Apellido", null, "PRESENCIAL", null, requestMock, redirectAttributesMock
        );

        verify(alumnoMock).setDescripcion(null);
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/perfil"));
    }


    @Test
    public void eliminarTemaConTemasNulosNoDeberiaRomper() {
        Long temaId = 1L;
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);
        when(servicioTemaMock.obtenerPorId(temaId)).thenReturn(temaMock);
        when(alumnoMock.getTemas()).thenReturn(null);

        ModelAndView modelAndView = controladorPerfil.eliminarTema(temaId, requestMock, redirectAttributesMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/perfil"));
        verify(repositorioUsuarioMock, never()).modificar(any());
    }
    @Test
    public void actualizarPerfilConNombreYApellidoVaciosDeberiaMostrarError() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);

        ModelAndView modelAndView = controladorPerfil.actualizarPerfil(
                "", "", "Descripción", "PRESENCIAL", null, requestMock, redirectAttributesMock
        );

        verify(alumnoMock, never()).setNombre(any());
        verify(alumnoMock, never()).setApellido(any());
        verify(repositorioUsuarioMock, never()).modificar(any());

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/perfil/editar"));
    }


    @Test
    public void eliminarFotoSinFotoNoDeberiaRomper() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);
        when(alumnoMock.getFotoPerfil()).thenReturn(null);

        ModelAndView modelAndView = controladorPerfil.eliminarFoto(requestMock, redirectAttributesMock);

        verify(alumnoMock).setFotoPerfil(null);
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/perfil"));
    }

    @Test
    public void actualizarPerfilConCamposConTildesYCaracteresEspecialesDeberiaActualizarCorrectamente() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);

        String nombre = "José Ángel";
        String apellido = "Muñoz-García";
        String descripcion = "Estudiante de ingeniería & música 🎶";
        String modalidad = "PRESENCIAL";

        ModelAndView modelAndView = controladorPerfil.actualizarPerfil(
                nombre, apellido, descripcion, modalidad, null, requestMock, redirectAttributesMock
        );

        verify(alumnoMock, times(1)).setNombre(nombre);
        verify(alumnoMock, times(1)).setApellido(apellido);
        verify(alumnoMock, times(1)).setDescripcion(descripcion);
        verify(alumnoMock, times(1)).setModalidadPreferida(ModalidadPreferida.PRESENCIAL);
        verify(repositorioUsuarioMock, times(1)).modificar(alumnoMock);
        verify(sessionMock, times(1)).setAttribute("USUARIO", alumnoMock);

        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/perfil"));
    }
    @Test
    public void actualizarPerfilConEmojisYSignosDeberiaActualizarSinErrores() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);

        String nombre = "Ana 😊";
        String apellido = "López ♥️";
        String descripcion = "Me encanta la biología y los libros 📚";

        ModelAndView modelAndView = controladorPerfil.actualizarPerfil(
                nombre, apellido, descripcion, "VIRTUAL", null, requestMock, redirectAttributesMock
        );

        verify(alumnoMock).setNombre(nombre);
        verify(alumnoMock).setApellido(apellido);
        verify(alumnoMock).setDescripcion(descripcion);
        verify(alumnoMock).setModalidadPreferida(ModalidadPreferida.VIRTUAL);
        verify(repositorioUsuarioMock).modificar(alumnoMock);
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/perfil"));
    }
    @Test
    public void actualizarPerfilConHtmlDeberiaGuardarComoTextoPlano() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(alumnoMock);
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(alumnoMock);

        String nombre = "<script>alert('hola')</script>";
        String apellido = "<b>Apellido</b>";
        String descripcion = "Texto con <i>HTML</i> y etiquetas";

        ModelAndView modelAndView = controladorPerfil.actualizarPerfil(
                nombre, apellido, descripcion, "PRESENCIAL", null, requestMock, redirectAttributesMock
        );

        verify(alumnoMock).setNombre(nombre);
        verify(alumnoMock).setApellido(apellido);
        verify(alumnoMock).setDescripcion(descripcion);
        verify(repositorioUsuarioMock).modificar(alumnoMock);
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("redirect:/perfil"));
    }

}