package com.tallerwebi.dominio.entidades;

import com.tallerwebi.dominio.Materia;

import javax.persistence.*;

@Entity
@PrimaryKeyJoinColumn(name = "id")
public class Profesor extends Usuario{



    private double latitud;

    public double getLongitud() {
        return longitud;
    }
    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    @Override
    public Tema getTema() {
        return tema;
    }

    @Override
    public void setTema(Tema tema) {
        this.tema = tema;
    }

    private double longitud;

    public Materia getMateria() {
        return materia;
    }

    public void setMateria(Materia materia) {
        this.materia = materia;
    }

    private Materia materia;


    @OneToOne
    @JoinColumn(name = "tema_id")
    private Tema tema;
}
