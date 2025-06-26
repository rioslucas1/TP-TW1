package com.tallerwebi.dominio.entidades;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedback_profesor")
public class FeedbackProfesor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesor_id", nullable = false)
    private Profesor profesor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @Column(nullable = false)
    private Integer calificacion;

    @Lob
    @Column(name = "comentario")
    private String comentario;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "nombre_estudiante")
    private String nombreEstudiante;

    public FeedbackProfesor() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public FeedbackProfesor(Profesor profesor, Alumno alumno, Integer calificacion, String comentario) {
        this();
        this.profesor = profesor;
        this.alumno = alumno;
        this.calificacion = calificacion;
        this.comentario = comentario;
        this.nombreEstudiante = alumno.getNombre() + " " + alumno.getApellido();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Profesor getProfesor() {
        return profesor;
    }

    public void setProfesor(Profesor profesor) {
        this.profesor = profesor;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
        if (alumno != null) {
            this.nombreEstudiante = alumno.getNombre() + " " + alumno.getApellido();
        }
    }

    public Integer getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Integer calificacion) {
        this.calificacion = calificacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getNombreEstudiante() {
        return nombreEstudiante;
    }

    public void setNombreEstudiante(String nombreEstudiante) {
        this.nombreEstudiante = nombreEstudiante;
    }

    public String getFechaFormateada() {
        if (fechaCreacion != null) {
            return fechaCreacion.toString();
        }
        return "";
    }
}