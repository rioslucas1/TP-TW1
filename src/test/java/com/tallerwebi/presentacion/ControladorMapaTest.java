package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Profesor;
import com.tallerwebi.dominio.ServicioMapa;
import com.tallerwebi.dominio.Materia;
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
}
