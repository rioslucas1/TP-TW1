package com.tallerwebi.dominio.entidades;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class MensajeTest {

    @Test
    public void deberiaCrearMensajeYEstablecerContenidoFechaYEmisor() {
        Mensaje mensaje = new Mensaje();

        String contenido = "Hola, ¿cómo estás?";
        String emisor = "Juan";
        LocalDateTime fecha = LocalDateTime.now();

        mensaje.setContenido(contenido);
        mensaje.setEmisor(emisor);
        mensaje.setFecha(fecha);

        assertEquals(contenido, mensaje.getContenido());
        assertEquals(emisor, mensaje.getEmisor());
        assertEquals(fecha, mensaje.getFecha());
    }

    @Test
    public void deberiaFormatearFechaCorrectamente() {
        Mensaje mensaje = new Mensaje();
        LocalDateTime fecha = LocalDateTime.of(2024, 6, 26, 14, 30);
        mensaje.setFecha(fecha);

        String fechaEsperada = "26/06/2024 14:30";
        assertEquals(fechaEsperada, mensaje.getFechaFormateada());
    }

    @Test
    public void deberiaPoderAsignarAlumnoYProfesor() {
        Mensaje mensaje = new Mensaje();
        Alumno alumno = new Alumno();
        Profesor profesor = new Profesor();

        mensaje.setAlumno(alumno);
        mensaje.setProfesor(profesor);

        assertEquals(alumno, mensaje.getAlumno());
        assertEquals(profesor, mensaje.getProfesor());
    }

    @Test
    public void deberiaCrearMensajeYEstablecerCamposCorrectamente() {
        Mensaje mensaje = new Mensaje();
        Alumno alumno = new Alumno();
        alumno.setNombre("Juan");

        Profesor profesor = new Profesor();
        profesor.setNombre("Ana");

        String contenido = "Hola, ¿cómo estás?";
        String emisor = "Juan";
        LocalDateTime fecha = LocalDateTime.now();

        mensaje.setAlumno(alumno);
        mensaje.setProfesor(profesor);
        mensaje.setContenido(contenido);
        mensaje.setEmisor(emisor);
        mensaje.setFecha(fecha);

        assertEquals(alumno, mensaje.getAlumno());
        assertEquals(profesor, mensaje.getProfesor());
        assertEquals(contenido, mensaje.getContenido());
        assertEquals(emisor, mensaje.getEmisor());
        assertEquals(fecha, mensaje.getFecha());
    }


    @Test
    public void siFechaEsNullFormateoDebeDevolverVacio() {
        Mensaje mensaje = new Mensaje();
        mensaje.setFecha(null);

        assertEquals("", mensaje.getFechaFormateada());
    }
}