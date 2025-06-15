package com.tallerwebi.dominio.entidades;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "alumnos")
@PrimaryKeyJoinColumn(name = "id")
public class Alumno extends Usuario {

    private Double latitud;
    private Double longitud;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "alumno_tema",
            joinColumns = @JoinColumn(name = "alumno_id"),
            inverseJoinColumns = @JoinColumn(name = "tema_id")
    )
    private List<Tema> temas = new ArrayList<>();

    public Alumno() {
        super();
        this.setRol("ALUMNO");
    }

    public Alumno(String nombre, String apellido, String email, String password) {
        super();
        this.setNombre(nombre);
        this.setApellido(apellido);
        this.setEmail(email);
        this.setPassword(password);
        this.setRol("ALUMNO");
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

    public List<Tema> getTemas() {
        return temas;
    }

    public void setTemas(List<Tema> temas) {
        this.temas = temas != null ? temas : new ArrayList<>();
    }

    public void agregarTema(Tema tema) {
        if (tema != null && !this.temas.contains(tema)) {
            this.temas.add(tema);
        }
    }

    public void removerTema(Tema tema) {
        this.temas.remove(tema);
    }

    public boolean tieneTema(Tema tema) {
        return this.temas.contains(tema);
    }
}