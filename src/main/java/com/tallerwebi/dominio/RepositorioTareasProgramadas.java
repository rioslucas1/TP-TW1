package com.tallerwebi.dominio;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import com.tallerwebi.dominio.servicios.ServicioNotificacion;

import java.util.Timer;
import java.util.TimerTask;

@Component
@EnableScheduling
public class RepositorioTareasProgramadas implements InitializingBean {

    private final ServicioNotificacion servicioNotificacion;

    public RepositorioTareasProgramadas(ServicioNotificacion servicioNotificacion) {
        this.servicioNotificacion = servicioNotificacion;
    }

    @Override
    public void afterPropertiesSet() {
        iniciar();
    }

    private void iniciar() {
        Timer timer = new Timer();

        long cada24Horas = 1000L * 60 * 60 * 24;


        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println(" Ejecutando notificaciones programadas...");
                servicioNotificacion.ejecutar();
            }
        }, 0, cada24Horas);

    }
}
