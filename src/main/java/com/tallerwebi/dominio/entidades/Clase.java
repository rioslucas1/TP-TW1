package com.tallerwebi.dominio.entidades;
import javax.persistence.*;
import java.time.LocalDate;

@Entity
public class Clase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String diaSemana;
    private String hora;
    private String fechaFormateada;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesor_id")
    private Profesor profesor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "alumno_id")
    private Alumno alumno;

    @Column(name = "fecha_especifica")
    private LocalDate fechaEspecifica;


    @Column(name = "enlace_meet")
    private String enlace_meet;

    @Enumerated(EnumType.STRING)
    private EstadoAsistencia estadoAsistencia = EstadoAsistencia.PENDIENTE;

    @Enumerated(EnumType.STRING)
    private EstadoDisponibilidad estado = EstadoDisponibilidad.DISPONIBLE;

    public Clase(Profesor profesor, String diaSemana, String hora) {
        this.profesor = profesor;
        this.diaSemana = diaSemana;
        this.hora = hora;
        this.estado = EstadoDisponibilidad.DISPONIBLE;
        this.estadoAsistencia = EstadoAsistencia.PENDIENTE;
    }

    public Clase(Profesor profesor, String diaSemana, String hora, EstadoDisponibilidad estado) {
        this.profesor = profesor;
        this.diaSemana = diaSemana;
        this.hora = hora;
        this.estado = estado;
    }

    public Clase(Profesor profesor, String diaSemana, String hora, LocalDate fechaEspecifica, EstadoDisponibilidad estado) {
        this.profesor = profesor;
        this.diaSemana = diaSemana;
        this.hora = hora;
        this.fechaEspecifica = fechaEspecifica;
        this.estado = estado;
    }

    public Clase() {

    }

    public String getFechaFormateada() {
        return fechaFormateada;
    }

    public void setFechaFormateada(String fechaFormateada) {
        this.fechaFormateada = fechaFormateada;
    }


    public String getEnlace_meet() {
        return enlace_meet;
    }

    public void setEnlace_meet(String enlace_meet) {
        this.enlace_meet = enlace_meet;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public EstadoDisponibilidad getEstado() {
        return estado;
    }

    public void setEstado(EstadoDisponibilidad estadoDisponibilidad) {
        this.estado = estadoDisponibilidad;
    }

    public boolean isDisponible() {
        return EstadoDisponibilidad.DISPONIBLE.equals(this.estado);
    }

    public boolean isOcupado() {
        return EstadoDisponibilidad.OCUPADO.equals(this.estado);
    }

    public boolean isReservado() {
        return EstadoDisponibilidad.RESERVADO.equals(this.estado);
    }

    public void marcarComoDisponible() {
        this.estado = EstadoDisponibilidad.DISPONIBLE;
    }

    public void marcarComoOcupado() {
        this.estado = EstadoDisponibilidad.OCUPADO;
    }

    public void marcarComoReservado() {
        this.estado = EstadoDisponibilidad.RESERVADO;
    }

    public LocalDate getFechaEspecifica() {
        return fechaEspecifica;
    }

    public void setFechaEspecifica(LocalDate fechaEspecifica) {
        this.fechaEspecifica = fechaEspecifica;
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

    public String getEmailProfesor() {
        return profesor != null ? profesor.getEmail() : null;
    }

    public String getMailAlumno() {
        return alumno != null ? alumno.getEmail() : null;
    }

    public EstadoAsistencia getEstadoAsistencia() {
        return estadoAsistencia;
    }

    public void setEstadoAsistencia(EstadoAsistencia estadoAsistencia) {
        this.estadoAsistencia = estadoAsistencia;
    }

}