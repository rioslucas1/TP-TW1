package com.tallerwebi.dominio.entidades;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class ClaseReservada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    public String emailProfesor;
    public String emailAlumno;
    public String diaSemana;
    public String hora;



    private LocalDate fechaClase;


    private LocalTime horaClase;

    @Enumerated(EnumType.STRING)
    private EstadoDisponibilidad estado = EstadoDisponibilidad.RESERVADO;
    public ClaseReservada() {}

    public ClaseReservada(String emailProfesor, String emailAlumno, String diaSemana,
                          String hora, LocalDate fechaClase) {
        this.emailProfesor = emailProfesor;
        this.emailAlumno = emailAlumno;
        this.diaSemana = diaSemana;
        this.hora = hora;
        this.fechaClase = fechaClase;
        this.horaClase = LocalTime.parse(hora);
        this.estado = EstadoDisponibilidad.RESERVADO;
    }

    public ClaseReservada(String emailProfesor, String diaSemana, String hora, EstadoDisponibilidad estado) {
        this.emailProfesor = emailProfesor;
        this.diaSemana = diaSemana;
        this.hora = hora;
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

    public LocalDate getFechaClase() {
        return fechaClase;
    }

    public void setFechaClase(LocalDate fechaClase) {
        this.fechaClase = fechaClase;
    }

    public LocalTime getHoraClase() {
        return horaClase;
    }

    public void setHoraClase(LocalTime horaClase) {
        this.horaClase = horaClase;
    }


}