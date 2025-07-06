package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.entidades.Clase;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ServicioMeetImpl implements ServicioMeet {

    @Override
    public String crearReunionGoogleMeet(Clase clase) {
        // Si no usas integración real con Google API, devolver un link de prueba:
        return "https://meet.google.com/" + UUID.randomUUID().toString().substring(0, 10);
    }
}
