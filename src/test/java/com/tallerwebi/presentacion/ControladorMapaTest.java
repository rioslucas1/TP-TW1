package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.servicios.ServicioMapa;
import com.tallerwebi.dominio.entidades.Materia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class ControladorMapaTest {

    private ControladorMapa controladorMapa;
    private ServicioMapa servicioMapaMock;

    @BeforeEach
    public void init() {
        servicioMapaMock = mock(ServicioMapa.class);
        controladorMapa = new ControladorMapa(servicioMapaMock);
    }

    @Test
    public void deberiaRetornarVistaMapaConListaDeDatosMapa() {
        Profesor profesor1 = mock(Profesor.class);
        when(profesor1.getNombre()).thenReturn("Juan");
        when(profesor1.getApellido()).thenReturn("Pérez");
        when(profesor1.getMateria()).thenReturn(Materia.FISICA);
        when(profesor1.getLatitud()).thenReturn(-34.6037);
        when(profesor1.getLongitud()).thenReturn(-58.3816);

        Profesor profesor2 = mock(Profesor.class);
        when(profesor2.getNombre()).thenReturn("Ana");
        when(profesor2.getApellido()).thenReturn("Gómez");
        when(profesor2.getMateria()).thenReturn(Materia.MATEMATICAS);
        when(profesor2.getLatitud()).thenReturn(-34.6090);
        when(profesor2.getLongitud()).thenReturn(-58.3838);

        List<Profesor> profesoresMock = Arrays.asList(profesor1, profesor2);

        when(servicioMapaMock.obtenerProfesores()).thenReturn(profesoresMock);

        ModelAndView modelAndView = controladorMapa.irVerMapa();

        assertThat(modelAndView.getViewName(), equalTo("mapa"));

        List<DatosMapa> datosMapa = (List<DatosMapa>) modelAndView.getModel().get("datosProfesores");
        assertThat(datosMapa, hasSize(2));

        DatosMapa primero = datosMapa.get(0);
        assertThat(primero.getNombre(), equalTo("Juan"));
        assertThat(primero.getApellido(), equalTo("Pérez"));
        assertThat(primero.getMateria(), equalTo(Materia.FISICA.toString()));
        assertThat(primero.getLatitud(), equalTo(-34.6037));
        assertThat(primero.getLongitud(), equalTo(-58.3816));

        DatosMapa segundo = datosMapa.get(1);
        assertThat(segundo.getNombre(), equalTo("Ana"));
        assertThat(segundo.getApellido(), equalTo("Gómez"));
        assertThat(segundo.getMateria(), equalTo(Materia.MATEMATICAS.toString()));
        assertThat(segundo.getLatitud(), equalTo(-34.6090));
        assertThat(segundo.getLongitud(), equalTo(-58.3838));
    }


    @Test
    public void deberiaRetornarVistaMapaConListaVaciaCuandoNoHayProfesores() {
        when(servicioMapaMock.obtenerProfesores()).thenReturn(List.of());

        ModelAndView modelAndView = controladorMapa.irVerMapa();

        assertThat(modelAndView.getViewName(), equalTo("mapa"));
        List<DatosMapa> datosMapa = (List<DatosMapa>) modelAndView.getModel().get("datosProfesores");
        assertThat(datosMapa, is(empty()));
    }

    @Test
    public void deberiaFallarLaVistaMapaCuandoServicioDevuelveNull() {
        when(servicioMapaMock.obtenerProfesores()).thenReturn(null);

        ModelAndView modelAndView = controladorMapa.irVerMapa();

        assertThat(modelAndView.getViewName(), equalTo("mapa"));

        List<DatosMapa> datosMapa = (List<DatosMapa>) modelAndView.getModel().get("datosProfesores");
        assertThat(datosMapa, is(empty()));
        // evaluo mensaje tengo que refactorizar despues esto. Esta bien que muestr el mensaje asi?
        String mensaje = (String) modelAndView.getModel().get("mensaje");
        assertThat(mensaje, equalTo("No se encontraron profesores para mostrar en el mapa."));
    }

    @Test
    public void deberiaManejarProfesorConCoordenadasInvalidas() {
        Profesor profesor = mock(Profesor.class);
        when(profesor.getNombre()).thenReturn("Carlos");
        when(profesor.getApellido()).thenReturn("Lopez");
        when(profesor.getMateria()).thenReturn(Materia.PROGRAMACION);
        when(profesor.getLatitud()).thenReturn(0.0); // por el momento lo dejo en double deberia ser null despues(?
        when(profesor.getLongitud()).thenReturn(0.0); // lo mismo que arriba

        when(servicioMapaMock.obtenerProfesores()).thenReturn(List.of(profesor));

        ModelAndView modelAndView = controladorMapa.irVerMapa();

        List<DatosMapa> datosMapa = (List<DatosMapa>) modelAndView.getModel().get("datosProfesores");
        assertThat(datosMapa, hasSize(1)); //comparacion / validacion
        DatosMapa dato = datosMapa.get(0);
        assertThat(dato.getLatitud(), is(0.0));
        assertThat(dato.getLongitud(), is(0.0));
    }


    @Test
    public void deberiaIgnorarProfesorConNombreNulo() {
        Profesor profesor = mock(Profesor.class);
        when(profesor.getNombre()).thenReturn(null);
        when(profesor.getApellido()).thenReturn("Apellido");
        when(profesor.getMateria()).thenReturn(Materia.FISICA);
        when(profesor.getLatitud()).thenReturn(-34.5);
        when(profesor.getLongitud()).thenReturn(-58.4);

        when(servicioMapaMock.obtenerProfesores()).thenReturn(List.of(profesor));

        ModelAndView modelAndView = controladorMapa.irVerMapa();
        List<DatosMapa> datosMapa = (List<DatosMapa>) modelAndView.getModel().get("datosProfesores");

        assertThat(datosMapa, hasSize(1));
        assertThat(datosMapa.get(0).getNombre(), anyOf(isEmptyOrNullString(), nullValue()));
    }
    @Test
    public void deberiaIgnorarProfesorConApellidoNulo() {
        Profesor profesor = mock(Profesor.class);
        when(profesor.getNombre()).thenReturn("Nombre");
        when(profesor.getApellido()).thenReturn(null);
        when(profesor.getMateria()).thenReturn(Materia.FISICA);
        when(profesor.getLatitud()).thenReturn(-34.5);
        when(profesor.getLongitud()).thenReturn(-58.4);

        when(servicioMapaMock.obtenerProfesores()).thenReturn(List.of(profesor));

        ModelAndView modelAndView = controladorMapa.irVerMapa();
        List<DatosMapa> datosMapa = (List<DatosMapa>) modelAndView.getModel().get("datosProfesores");

        assertThat(datosMapa, hasSize(1));
        assertThat(datosMapa.get(0).getApellido(), anyOf(isEmptyOrNullString(), nullValue()));
    }



    @Test
    public void deberiaMostrarCorrectamenteProfesorConCaracteresEspeciales() {
        Profesor profesor = mock(Profesor.class);
        when(profesor.getNombre()).thenReturn("José María");
        when(profesor.getApellido()).thenReturn("Gómez-Álvarez");
        when(profesor.getMateria()).thenReturn(Materia.PROGRAMACION);
        when(profesor.getLatitud()).thenReturn(-34.7);
        when(profesor.getLongitud()).thenReturn(-58.5);

        when(servicioMapaMock.obtenerProfesores()).thenReturn(List.of(profesor));

        ModelAndView modelAndView = controladorMapa.irVerMapa();

        List<DatosMapa> datos = (List<DatosMapa>) modelAndView.getModel().get("datosProfesores");

        assertThat(datos, hasSize(1));
        assertThat(datos.get(0).getNombre(), equalTo("José María"));
        assertThat(datos.get(0).getApellido(), equalTo("Gómez-Álvarez"));
    }



}