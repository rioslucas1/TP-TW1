package com.tallerwebi.dominio.entidades;
import javax.persistence.*;
import java.time.LocalDate;

@Entity
public class disponibilidadProfesor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public String emailProfesor;
    public String diaSemana;
    public String hora;

    public String mailAlumno;

    @Column(name = "fecha_especifica")
    private LocalDate fechaEspecifica;



    @Enumerated(EnumType.STRING)
    private EstadoDisponibilidad estado = EstadoDisponibilidad.DISPONIBLE;
    public disponibilidadProfesor() {}

    public disponibilidadProfesor(String emailProfesor, String diaSemana, String hora) {
        this.emailProfesor = emailProfesor;
        this.diaSemana = diaSemana;
        this.hora = hora;
        this.estado = EstadoDisponibilidad.DISPONIBLE;
    }

    public disponibilidadProfesor(String emailProfesor, String diaSemana, String hora, EstadoDisponibilidad estado) {
        this.emailProfesor = emailProfesor;
        this.diaSemana = diaSemana;
        this.hora = hora;
        this.estado = estado;
    }

    public disponibilidadProfesor(String emailProfesor, String diaSemana, String hora, LocalDate fechaEspecifica, EstadoDisponibilidad estado) {
        this.emailProfesor = emailProfesor;
        this.diaSemana = diaSemana;
        this.hora = hora;
        this.fechaEspecifica = fechaEspecifica;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmailProfesor() {
        return emailProfesor;
    }

    public void setEmailProfesor(String emailProfesor) {
        this.emailProfesor = emailProfesor;
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

    public String getMailAlumno() {
        return mailAlumno;
    }

    public void setMailAlumno(String mailAlumno) {
        this.mailAlumno = mailAlumno;
    }

}