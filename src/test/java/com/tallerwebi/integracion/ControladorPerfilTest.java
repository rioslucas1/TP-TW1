package com.tallerwebi.integracion;

import com.tallerwebi.dominio.servicios.ServicioTema;
import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.presentacion.ControladorPerfil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SpringWebTestConfig.class, HibernateTestConfig.class, ControladorPerfilTest.TestConfig.class})
public class ControladorPerfilTest {


    private Alumno alumnoReal;
    private Profesor profesorReal;
    private Tema temaReal;
    private List<Tema> temasMock;

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    @Autowired
    private ServicioTema servicioTema;

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    @Configuration
    static class TestConfig {
        @Bean
        @Primary
        public ServicioTema servicioTemaMock() {
            return mock(ServicioTema.class);
        }

        @Bean
        @Primary
        public RepositorioUsuario repositorioUsuarioMock() {
            return mock(RepositorioUsuario.class);
        }
    }

    @BeforeEach
    public void init() {

        temaReal = new Tema();
        temaReal.setId(1L);
        temaReal.setNombre("Matemáticas");

        alumnoReal = new Alumno();
        alumnoReal.setId(1L);
        alumnoReal.setNombre("Ana");
        alumnoReal.setApellido("García");
        alumnoReal.setEmail("alumno@unlam.com");
        alumnoReal.setPassword("password123");
        alumnoReal.setDescripcion("Estudiante de tecnicatura");
        alumnoReal.setModalidadPreferida(ModalidadPreferida.PRESENCIAL);
        alumnoReal.setTemas(new ArrayList<>());


        profesorReal = new Profesor();
        profesorReal.setId(2L);
        profesorReal.setNombre("Juan");
        profesorReal.setApellido("Pérez");
        profesorReal.setEmail("profesor@unlam.com");
        profesorReal.setPassword("password123");

        temasMock = Arrays.asList(temaReal);

        reset(servicioTema, repositorioUsuario);

        when(repositorioUsuario.buscarPorId(1L)).thenReturn(alumnoReal);
        when(servicioTema.obtenerTodos()).thenReturn(temasMock);
        when(servicioTema.obtenerPorId(1L)).thenReturn(temaReal);

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void queAlVerPerfilAlumnoMuestraLaInformacionCompleta() throws Exception {
        MvcResult result = mockMvc.perform(get("/perfil")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("verPerfil"));
        assertThat(modelAndView.getModel().get("nombreUsuario"), is("Ana"));
        assertThat(modelAndView.getModel().get("rol"), is("alumno"));
        assertThat(modelAndView.getModel().get("usuario"), is(alumnoReal));
        assertThat(modelAndView.getModel().get("alumno"), is(alumnoReal));
        assertThat(modelAndView.getModel().get("todosLosTemas"), is(temasMock));
        assertThat(modelAndView.getModel().get("esEdicion"), is(false));

        verify(repositorioUsuario).buscarPorId(1L);
        verify(servicioTema).obtenerTodos();
    }

    @Test
    public void queAlVerPerfilSinUsuarioEnSesionRedirigeALogin() throws Exception {
        mockMvc.perform(get("/perfil"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void queAlVerPerfilConProfesorEnSesionRedirigeAPerfilProfesor() throws Exception {
        mockMvc.perform(get("/perfil")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profesor/perfil"));
    }

    @Test
    public void queAlEditarPerfilAlumnoMuestraFormularioDeEdicion() throws Exception {
        MvcResult result = mockMvc.perform(get("/perfil/editar")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assertThat(modelAndView.getViewName(), is("verPerfil"));
        assertThat(modelAndView.getModel().get("alumno"), is(alumnoReal));
        assertThat(modelAndView.getModel().get("todosLosTemas"), is(temasMock));
        assertThat(modelAndView.getModel().get("esEdicion"), is(true));

        verify(repositorioUsuario).buscarPorId(1L);
        verify(servicioTema).obtenerTodos();
    }

    @Test
    public void queAlEditarPerfilSinUsuarioEnSesionRedirigeALogin() throws Exception {
        mockMvc.perform(get("/perfil/editar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void queAlActualizarPerfilConDatosValidosActualizaCorrectamente() throws Exception {
        doNothing().when(repositorioUsuario).modificar(any(Alumno.class));

        mockMvc.perform(post("/perfil/actualizar")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("nombre", "Carlos")
                        .param("apellido", "López")
                        .param("descripcion", "Nueva descripción")
                        .param("modalidadPreferida", "VIRTUAL"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"));

        verify(repositorioUsuario).buscarPorId(1L);
        verify(repositorioUsuario).modificar(any(Alumno.class));
    }

    @Test
    public void queAlActualizarPerfilSinNombreDevuelveError() throws Exception {
        mockMvc.perform(post("/perfil/actualizar")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("nombre", "")
                        .param("apellido", "López")
                        .param("descripcion", "Nueva descripción"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil/editar"));
    }

    @Test
    public void queAlActualizarPerfilSinApellidoDevuelveError() throws Exception {
        mockMvc.perform(post("/perfil/actualizar")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("nombre", "Carlos")
                        .param("apellido", "")
                        .param("descripcion", "Nueva descripción"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil/editar"));
    }

    @Test
    public void queAlActualizarPerfilConNombreVacioDevuelveError() throws Exception {
        mockMvc.perform(post("/perfil/actualizar")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("nombre", "   ")
                        .param("apellido", "López")
                        .param("descripcion", "Nueva descripción"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil/editar"));
    }

    @Test
    public void queAlActualizarPerfilConApellidoVacioDevuelveError() throws Exception {
        mockMvc.perform(post("/perfil/actualizar")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("nombre", "Carlos")
                        .param("apellido", "   ")
                        .param("descripcion", "Nueva descripción"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil/editar"));
    }

    @Test
    public void queAlActualizarPerfilSinUsuarioEnSesionRedirigeALogin() throws Exception {
        mockMvc.perform(post("/perfil/actualizar")
                        .param("nombre", "Carlos")
                        .param("apellido", "López"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void queAlActualizarPerfilConProfesorEnSesionRedirigeAHome() throws Exception {
        mockMvc.perform(post("/perfil/actualizar")
                        .sessionAttr("USUARIO", profesorReal)
                        .param("nombre", "Carlos")
                        .param("apellido", "López"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    public void queAlActualizarPerfilConModalidadInvalidaDejaModalidadEnNull() throws Exception {
        doNothing().when(repositorioUsuario).modificar(any(Alumno.class));

        mockMvc.perform(post("/perfil/actualizar")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("nombre", "Carlos")
                        .param("apellido", "López")
                        .param("modalidadPreferida", "MODALIDAD_INEXISTENTE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"));

        verify(repositorioUsuario).modificar(any(Alumno.class));
    }

    @Test
    public void queAlActualizarPerfilConDescripcionVaciaDejaDescripcionEnNull() throws Exception {
        doNothing().when(repositorioUsuario).modificar(any(Alumno.class));

        mockMvc.perform(post("/perfil/actualizar")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("nombre", "Carlos")
                        .param("apellido", "López")
                        .param("descripcion", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"));

        verify(repositorioUsuario).modificar(any(Alumno.class));
    }

    @Test
    public void queAlActualizarPerfilConFotoBase64ValidaActualizaCorrectamente() throws Exception {
        String fotoBase64Valida = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAoKCgoKCgsMDAsPEA==";
        doNothing().when(repositorioUsuario).modificar(any(Alumno.class));

        mockMvc.perform(post("/perfil/actualizar")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("nombre", "Carlos")
                        .param("apellido", "López")
                        .param("fotoBase64", fotoBase64Valida))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"));

        verify(repositorioUsuario).modificar(any(Alumno.class));
    }

    @Test
    public void queAlActualizarPerfilConFotoBase64InvalidaDevuelveError() throws Exception {
        String fotoBase64Invalida = "imagen-invalida";

        mockMvc.perform(post("/perfil/actualizar")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("nombre", "Carlos")
                        .param("apellido", "López")
                        .param("fotoBase64", fotoBase64Invalida))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil/editar"));
    }

    @Test
    public void queAlEliminarFotoPerfilEliminaCorrectamente() throws Exception {
        doNothing().when(repositorioUsuario).modificar(any(Alumno.class));

        mockMvc.perform(post("/perfil/eliminar-foto")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"));

        verify(repositorioUsuario).buscarPorId(1L);
        verify(repositorioUsuario).modificar(any(Alumno.class));
    }

    @Test
    public void queAlEliminarFotoSinUsuarioEnSesionRedirigeALogin() throws Exception {
        mockMvc.perform(post("/perfil/eliminar-foto"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void queAlEliminarFotoConProfesorEnSesionRedirigeAHome() throws Exception {
        mockMvc.perform(post("/perfil/eliminar-foto")
                        .sessionAttr("USUARIO", profesorReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    public void queAlAgregarTemaConDatosValidosAgregaCorrectamente() throws Exception {
        doNothing().when(repositorioUsuario).modificar(any(Alumno.class));

        mockMvc.perform(post("/perfil/agregar-tema")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("temaId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"));

        verify(repositorioUsuario).buscarPorId(1L);
        verify(servicioTema).obtenerPorId(1L);
        verify(repositorioUsuario).modificar(any(Alumno.class));
    }

    @Test
    public void queAlAgregarTemaInexistenteDevuelveError() throws Exception {
        when(servicioTema.obtenerPorId(999L)).thenReturn(null);

        mockMvc.perform(post("/perfil/agregar-tema")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("temaId", "999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"));

        verify(servicioTema).obtenerPorId(999L);
    }

    @Test
    public void queAlAgregarTemaYaExistenteDevuelveError() throws Exception {
        alumnoReal.getTemas().add(temaReal);

        mockMvc.perform(post("/perfil/agregar-tema")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("temaId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"));

        verify(servicioTema).obtenerPorId(1L);
    }

    @Test
    public void queAlAgregarTemaSinUsuarioEnSesionRedirigeALogin() throws Exception {
        mockMvc.perform(post("/perfil/agregar-tema")
                        .param("temaId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void queAlAgregarTemaConProfesorEnSesionRedirigeAHome() throws Exception {
        mockMvc.perform(post("/perfil/agregar-tema")
                        .sessionAttr("USUARIO", profesorReal)
                        .param("temaId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    public void queAlEliminarTemaConDatosValidosEliminaCorrectamente() throws Exception {
        alumnoReal.getTemas().add(temaReal);
        doNothing().when(repositorioUsuario).modificar(any(Alumno.class));

        mockMvc.perform(post("/perfil/eliminar-tema")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("temaId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"));

        verify(repositorioUsuario).buscarPorId(1L);
        verify(servicioTema).obtenerPorId(1L);
        verify(repositorioUsuario).modificar(any(Alumno.class));
    }

    @Test
    public void queAlEliminarTemaInexistenteDevuelveError() throws Exception {
        when(servicioTema.obtenerPorId(999L)).thenReturn(null);

        mockMvc.perform(post("/perfil/eliminar-tema")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("temaId", "999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"));

        verify(servicioTema).obtenerPorId(999L);
    }

    @Test
    public void queAlEliminarTemaSinUsuarioEnSesionRedirigeALogin() throws Exception {
        mockMvc.perform(post("/perfil/eliminar-tema")
                        .param("temaId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void queAlEliminarTemaConProfesorEnSesionRedirigeAHome() throws Exception {
        mockMvc.perform(post("/perfil/eliminar-tema")
                        .sessionAttr("USUARIO", profesorReal)
                        .param("temaId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    public void queAlActualizarPerfilConModalidadVaciaDejaModalidadEnNull() throws Exception {
        doNothing().when(repositorioUsuario).modificar(any(Alumno.class));

        mockMvc.perform(post("/perfil/actualizar")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("nombre", "Carlos")
                        .param("apellido", "López")
                        .param("modalidadPreferida", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"));

        verify(repositorioUsuario).modificar(any(Alumno.class));
    }

    @Test
    public void queAlActualizarPerfilConExcepcionDevuelveError() throws Exception {
        when(repositorioUsuario.buscarPorId(1L)).thenThrow(new RuntimeException("Error de base de datos"));

        mockMvc.perform(post("/perfil/actualizar")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("nombre", "Carlos")
                        .param("apellido", "López"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"));

        verify(repositorioUsuario).buscarPorId(1L);
    }

    @Test
    public void queAlEliminarFotoConExcepcionDevuelveError() throws Exception {
        when(repositorioUsuario.buscarPorId(1L)).thenThrow(new RuntimeException("Error de base de datos"));

        mockMvc.perform(post("/perfil/eliminar-foto")
                        .sessionAttr("USUARIO", alumnoReal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"));

        verify(repositorioUsuario).buscarPorId(1L);
    }

    @Test
    public void queAlAgregarTemaConExcepcionDevuelveError() throws Exception {
        when(repositorioUsuario.buscarPorId(1L)).thenThrow(new RuntimeException("Error de base de datos"));

        mockMvc.perform(post("/perfil/agregar-tema")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("temaId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"));

        verify(repositorioUsuario).buscarPorId(1L);
    }

    @Test
    public void queAlEliminarTemaConExcepcionDevuelveError() throws Exception {
        when(repositorioUsuario.buscarPorId(1L)).thenThrow(new RuntimeException("Error de base de datos"));

        mockMvc.perform(post("/perfil/eliminar-tema")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("temaId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"));

        verify(repositorioUsuario).buscarPorId(1L);
    }

    @Test
    public void queAlActualizarPerfilConParametrosNulosNoFallaValores() throws Exception {
        doNothing().when(repositorioUsuario).modificar(any(Alumno.class));

        mockMvc.perform(post("/perfil/actualizar")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("nombre", "Carlos")
                        .param("apellido", "López"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"));

        verify(repositorioUsuario).modificar(any(Alumno.class));
    }

    @Test
    public void queAlActualizarPerfilConFotoBase64VaciaNoActualizaFoto() throws Exception {
        doNothing().when(repositorioUsuario).modificar(any(Alumno.class));

        mockMvc.perform(post("/perfil/actualizar")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("nombre", "Carlos")
                        .param("apellido", "López")
                        .param("fotoBase64", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"));

        verify(repositorioUsuario).modificar(any(Alumno.class));
    }

    @Test
    public void queAlActualizarPerfilConFotoBase64SinDataImagenDevuelveError() throws Exception {
        String fotoBase64Invalida = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChAI=";

        mockMvc.perform(post("/perfil/actualizar")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("nombre", "Carlos")
                        .param("apellido", "López")
                        .param("fotoBase64", fotoBase64Invalida))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil/editar"));
    }

    @Test
    public void queAlActualizarPerfilConFotoBase64SinBase64DevuelveError() throws Exception {
        String fotoBase64Invalida = "data:image/jpeg;texto-sin-base64";

        mockMvc.perform(post("/perfil/actualizar")
                        .sessionAttr("USUARIO", alumnoReal)
                        .param("nombre", "Carlos")
                        .param("apellido", "López")
                        .param("fotoBase64", fotoBase64Invalida))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil/editar"));
    }
}