package com.tallerwebi.presentacion;

public class DatosRegistroProfesor {

    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private Long temaId;

    public DatosRegistroProfesor() {
    }

    public DatosRegistroProfesor(String nombre, String apellido, String email, String password, Long temaId) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.password = password;
        this.temaId = temaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getTemaId() {
        return temaId;
    }

    public void setTemaId(Long temaId) {
        this.temaId = temaId;
    }
    }