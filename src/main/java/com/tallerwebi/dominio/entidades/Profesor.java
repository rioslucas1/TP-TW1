package com.tallerwebi.dominio.entidades;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "profesores")
@PrimaryKeyJoinColumn(name = "id")
public class Profesor extends Usuario {

    private Double latitud;
    private Double longitud;
    private Double calificacionPromedio;

    private Integer totalClasesDadas;
    private Integer totalAlumnos;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tema_id", nullable = false)
    private Tema tema;

    @OneToMany(mappedBy = "profesor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FeedbackProfesor> feedbacks = new ArrayList<>();

    @OneToMany(mappedBy = "profesor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExperienciaEstudiantil> experiencias = new ArrayList<>();

    @ManyToMany(mappedBy = "profesores", fetch = FetchType.LAZY)
    private List<Alumno> alumnos = new ArrayList<>();

    public Profesor() {
        super();
        this.setRol("PROFESOR");
        this.calificacionPromedio = 0.0;
        this.totalClasesDadas = 0;
        this.totalAlumnos = 0;
    }

    public Profesor(String nombre, String apellido, String email, String password) {
        super();
        this.setNombre(nombre);
        this.setApellido(apellido);
        this.setEmail(email);
        this.setPassword(password);
        this.setRol("PROFESOR");
        this.calificacionPromedio = 0.0;
        this.totalClasesDadas = 0;
        this.totalAlumnos = 0;
        this.feedbacks = new ArrayList<>();
        this.experiencias = new ArrayList<>();
    }


    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Tema getTema() {
        return tema;
    }

    public void setTema(Tema tema) {
        this.tema = tema;
    }


    public List<FeedbackProfesor> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(List<FeedbackProfesor> feedbacks) {
        this.feedbacks = feedbacks;
    }

    public void agregarFeedback(FeedbackProfesor feedback) {
        if (feedback != null && !this.feedbacks.contains(feedback)) {
            this.feedbacks.add(feedback);
            feedback.setProfesor(this);
        }
    }

    public void removerFeedback(FeedbackProfesor feedback) {
        this.feedbacks.remove(feedback);
    }

    public List<ExperienciaEstudiantil> getExperiencias() {
        return experiencias;
    }

    public void setExperiencias(List<ExperienciaEstudiantil> experiencias) {
        this.experiencias = experiencias;
    }

    public void agregarExperiencia(ExperienciaEstudiantil experiencia) {
        if (experiencia != null && !this.experiencias.contains(experiencia)) {
            this.experiencias.add(experiencia);
            experiencia.setProfesor(this);
        }
    }

    public void removerExperiencia(ExperienciaEstudiantil experiencia) {
        this.experiencias.remove(experiencia);
    }

    public Double getCalificacionPromedio() {
        return calificacionPromedio;
    }

    public void setCalificacionPromedio(Double calificacionPromedio) {
        this.calificacionPromedio = calificacionPromedio;
    }

    public Integer getTotalClasesDadas() {
        return totalClasesDadas;
    }

    public void setTotalClasesDadas(Integer totalClasesDadas) {
        this.totalClasesDadas = totalClasesDadas;
    }

    public Integer getTotalAlumnos() {
        return totalAlumnos;
    }

    public void setTotalAlumnos(Integer totalAlumnos) {
        this.totalAlumnos = totalAlumnos;
    }

    public List<Alumno> getAlumnos() {
        return alumnos;
    }

    public void setAlumnos(List<Alumno> alumnos) {
        this.alumnos = alumnos;
    }

    public void agregarAlumno(Alumno alumno) {
        if (alumno != null && !this.alumnos.contains(alumno)) {
            this.alumnos.add(alumno);

            if (!alumno.getProfesores().contains(this)) {
                alumno.agregarProfesor(this);
            }
        }
    }

    public void eliminarAlumno(Alumno alumno) {
        if (this.alumnos.remove(alumno)) {

            alumno.eliminarProfesor(this);
        }
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad")
    private ModalidadPreferida modalidad;

    public ModalidadPreferida getModalidad() {
        return modalidad;
    }

    public void setModalidad(ModalidadPreferida modalidad) {
        this.modalidad = modalidad;
    }

}


