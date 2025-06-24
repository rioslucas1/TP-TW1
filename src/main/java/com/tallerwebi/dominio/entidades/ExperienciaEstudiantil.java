package com.tallerwebi.dominio.entidades;

import javax.persistence.*;

@Entity
@Table(name = "experiencia_estudiantil")
public class ExperienciaEstudiantil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesor_id", nullable = false)
    private Profesor profesor;

    @Column(nullable = false)
    private String institucion;

    @Column(nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private String fecha;

    @Column(name = "tipo_experiencia")
    private String tipoExperiencia;

    public ExperienciaEstudiantil() {
    }

    public ExperienciaEstudiantil(Profesor profesor, String institucion, String descripcion, String fecha) {
        this.profesor = profesor;
        this.institucion = institucion;
        this.descripcion = descripcion;
        this.fecha = fecha;
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

    public String getInstitucion() {
        return institucion;
    }

    public void setInstitucion(String institucion) {
        this.institucion = institucion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getTipoExperiencia() {
        return tipoExperiencia;
    }

    public void setTipoExperiencia(String tipoExperiencia) {
        this.tipoExperiencia = tipoExperiencia;
    }
}