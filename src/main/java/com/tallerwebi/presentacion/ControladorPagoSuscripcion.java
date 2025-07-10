package com.tallerwebi.presentacion;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.dominio.servicios.ServicioMercadoPago;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Collections;

@Controller
public class ControladorPagoSuscripcion {

    private final RepositorioUsuario repositorioUsuario;
    private final ServicioMercadoPago servicioMercadoPago;

    @Autowired
    public ControladorPagoSuscripcion(RepositorioUsuario repositorioUsuario, ServicioMercadoPago servicioMercadoPago) {
        this.repositorioUsuario = repositorioUsuario;
        this.servicioMercadoPago = servicioMercadoPago;
        MercadoPagoConfig.setAccessToken("APP_USR-4613712092625736-070300-d7b9416de23198cfad9290559a13fefa-2529561427");
    }

    @GetMapping("/suscripcion/pagar")
    public ModelAndView vistaPago(HttpServletRequest request) {
        Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");
        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }
        return new ModelAndView("pago-suscripcion");
    }

    @PostMapping("/suscripcion/crear-preferencia")
    @ResponseBody
    public String crearPreferencia(HttpServletRequest request) {
        Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");
        if (usuario == null) {
            return "{\"error\": \"No logueado\"}";
        }

        try {
            String preferenceId = servicioMercadoPago.crearPreferencia(usuario);
            usuario.setSubscriptionId(preferenceId);
            repositorioUsuario.modificar(usuario);
            return "{\"preferenceId\": \"" + preferenceId + "\"}";
        } catch (Exception e) {
            return "{\"error\": \"Error al crear preferencia: " + e.getMessage() + "\"}";
        }
    }

    @GetMapping("/suscripcion/resultado")
    public ModelAndView resultadoPago(
            @RequestParam(required = false) String collection_status,
            @RequestParam(required = false) String external_reference,
            @RequestParam(required = false) String preference_id,
            HttpServletRequest request
    ) {
        if ("approved".equals(collection_status) && external_reference != null) {
            try {
                Usuario usuario = repositorioUsuario.buscarPorId(Long.parseLong(external_reference));
                if (usuario != null) {
                    usuario.setHabilitado(true);
                    repositorioUsuario.modificar(usuario);
                    request.getSession().setAttribute("USUARIO", usuario);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ModelAndView mv = new ModelAndView("resultado-suscripcion");
        mv.addObject("estado", collection_status);
        return mv;
    }
}