package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.Usuario;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServicioNotificacion {

    private final RepositorioUsuario repositorioUsuario;
    private final ServicioEnvioDeCorreos servicioCorreo;

    public ServicioNotificacion(RepositorioUsuario repositorioUsuario, ServicioEnvioDeCorreos servicioCorreo) {
        this.repositorioUsuario = repositorioUsuario;
        this.servicioCorreo = servicioCorreo;
    }

    public void ejecutar() {
        List<Usuario> pendientes = repositorioUsuario.buscarConNotificacionesPendientes();

        for (Usuario usuario : pendientes) {
            Map<String, String> datos = new HashMap<>();
            datos.put("nombre", usuario.getNombre());
            datos.put("mensaje", "Tenés una nueva notificación.");
            datos.put("url", "https://clasesya.com/notificaciones");

            servicioCorreo.enviarCorreo(
                usuario.getEmail(),
                "Notificación",
                ServicioEnvioDeCorreos.TipoCorreo.NOTIFICACION,
                datos
            );
        }
    }
}
