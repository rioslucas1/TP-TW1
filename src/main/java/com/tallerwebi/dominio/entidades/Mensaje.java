package com.tallerwebi.dominio.entidades;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "alumno_id")
    private Alumno alumno;

    @ManyToOne
    @JoinColumn(name = "profesor_id")
    private Profesor profesor;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    private String emisor; // "ALUMNO" o "PROFESOR"

    private LocalDateTime fecha;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public Profesor getProfesor() {
        return profesor;
    }

    public void setProfesor(Profesor profesor) {
        this.profesor = profesor;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getEmisor() {
        return emisor;
    }

    public void setEmisor(String emisor) {
        this.emisor = emisor;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getFechaFormateada() {
        if (fecha == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return fecha.format(formatter);
    }

    public void setReceptor(String receptor) {
    }

    // Método nuevo para mostrar el nombre real del emisor
    public String getNombreEmisor() {
        if ("ALUMNO".equalsIgnoreCase(emisor) && alumno != null) {
            return alumno.getNombre();
        } else if ("PROFESOR".equalsIgnoreCase(emisor) && profesor != null) {
            return profesor.getNombre();
        }
        return emisor; // fallback
    }
}