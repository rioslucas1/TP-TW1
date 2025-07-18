package com.tallerwebi.presentacion;

public class DatosMapa {

    private String nombre;
    private String apellido;
    private String tema;
    private double latitud;
    private double longitud;

    public DatosMapa(String nombre, String apellido, String tema, double latitud, double longitud) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.tema = tema;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getTema() {
        return tema;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

//    public void setApellido(String apellido) {
//        this.apellido = apellido;
//    }
//
//    public void setMateria(String materia) {
//        this.materia = materia;
//    }
//
//    public void setNombre(String nombre) {
//        this.nombre = nombre;
//    }
//
//    public void setLatitud(double latitud) {
//        this.latitud = latitud;
//    }
//
//    public void setLongitud(double longitud) {
//        this.longitud = longitud;
//    }
}
