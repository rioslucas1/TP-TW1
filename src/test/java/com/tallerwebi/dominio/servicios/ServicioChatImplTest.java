package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.RepositorioMensaje;
import com.tallerwebi.dominio.entidades.Mensaje;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ServicioChatImplTest {

    private RepositorioMensaje repositorioMensaje;
    private ServicioChatImpl servicioChat;

    @BeforeEach
    public void setUp() {
        repositorioMensaje = mock(RepositorioMensaje.class);
        servicioChat = new ServicioChatImpl(repositorioMensaje);
    }

    @Test
    public void enviarMensaje_deberiaGuardarMensajeConDatosCorrectos() {
        String emisor = "AlumnoNombre";
        String receptor = "ProfesorNombre";
        String contenido = "Hola profe";

        servicioChat.enviarMensaje(emisor, receptor, contenido);

        ArgumentCaptor<Mensaje> captor = ArgumentCaptor.forClass(Mensaje.class);
        verify(repositorioMensaje).guardar(captor.capture());

        Mensaje mensajeGuardado = captor.getValue();
        assertEquals(emisor, mensajeGuardado.getEmisor());
        assertEquals(receptor, mensajeGuardado.getReceptor());
        assertEquals(contenido, mensajeGuardado.getContenido());
        assertNotNull(mensajeGuardado.getFecha());
    }

    @Test
    public void obtenerConversacion_deberiaRetornarMensajesDelRepositorio() {
        String emisor = "AlumnoNombre";
        String receptor = "ProfesorNombre";

        Mensaje m1 = new Mensaje();
        m1.setEmisor(emisor);
        m1.setReceptor(receptor);
        m1.setContenido("Hola");

        Mensaje m2 = new Mensaje();
        m2.setEmisor(receptor);
        m2.setReceptor(emisor);
        m2.setContenido("Hola, ¿cómo estás?");

        List<Mensaje> conversacionMock = Arrays.asList(m1, m2);

        when(repositorioMensaje.obtenerConversacion(emisor, receptor)).thenReturn(conversacionMock);

        List<Mensaje> resultado = servicioChat.obtenerConversacion(emisor, receptor);

        assertEquals(2, resultado.size());
        assertEquals("Hola", resultado.get(0).getContenido());
        assertEquals("Hola, ¿cómo estás?", resultado.get(1).getContenido());
    }

    @Test
    public void enviarMensaje_conContenidoVacio_noDeberiaGuardarMensaje() {
        String emisor = "AlumnoNombre";
        String receptor = "ProfesorNombre";
        String contenido = "";

        servicioChat.enviarMensaje(emisor, receptor, contenido);

        verify(repositorioMensaje, never()).guardar(any());
    }

    @Test
    public void enviarMensaje_deberiaSetearFechaCercanaAAhora() {
        String emisor = "A";
        String receptor = "B";
        String contenido = "Mensaje";

        LocalDateTime antes = LocalDateTime.now();
        servicioChat.enviarMensaje(emisor, receptor, contenido);
        LocalDateTime despues = LocalDateTime.now();

        ArgumentCaptor<Mensaje> captor = ArgumentCaptor.forClass(Mensaje.class);
        verify(repositorioMensaje).guardar(captor.capture());

        LocalDateTime fecha = captor.getValue().getFecha();
        assertTrue(!fecha.isBefore(antes) && !fecha.isAfter(despues));
    }
}