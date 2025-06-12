package com.tallerwebi.dominio;
import java.util.List;

import javax.persistence.*;

@Entity
public class Alumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String materiaInteres;
    private double latitud;
    private double longitud;

    @OneToMany(mappedBy = "alumno")
    private List<CursoAlumno> cursosInscritos;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMateriaInteres() { return materiaInteres; }
    public void setMateriaInteres(String materiaInteres) { this.materiaInteres = materiaInteres; }

    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }

    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }

    public List<CursoAlumno> getCursosInscritos() { return cursosInscritos; }
    public void setCursosInscritos(List<CursoAlumno> cursosInscritos) { this.cursosInscritos = cursosInscritos; }
}

