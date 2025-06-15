package com.tallerwebi.dominio.entidades;

import javax.persistence.*;

@Entity
@Table(name = "profesores")
@PrimaryKeyJoinColumn(name = "id")
public class Profesor extends Usuario {

    private Double latitud;
    private Double longitud;
    private Materia materia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tema_id", nullable = false)
    private Tema tema;

    public Profesor() {
        super();
        this.setRol("PROFESOR");
    }

    public Profesor(String nombre, String apellido, String email, String password) {
        super();
        this.setNombre(nombre);
        this.setApellido(apellido);
        this.setEmail(email);
        this.setPassword(password);
        this.setRol("PROFESOR");
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

    public Materia getMateria() {
        return materia;
    }

    public void setMateria(Materia materia) {
        this.materia = materia;
    }

    public Tema getTema() {
        return tema;
    }

    public void setTema(Tema tema) {
        this.tema = tema;
    }
}


