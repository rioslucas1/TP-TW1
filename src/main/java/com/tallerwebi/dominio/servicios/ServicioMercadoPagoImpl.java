package com.tallerwebi.dominio.servicios;

import com.mercadopago.client.preference.*;
import com.mercadopago.resources.preference.Preference;
import com.tallerwebi.dominio.entidades.Usuario;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;

@Service
public class ServicioMercadoPagoImpl implements ServicioMercadoPago{

    @Override
    public String crearPreferencia(Usuario usuario) throws Exception {
        PreferenceItemRequest item = PreferenceItemRequest.builder()
                .title("Suscripción ClasesYa Premium")
                .quantity(1)
                .currencyId("ARS")
                .unitPrice(new BigDecimal("1000.00"))
                .build();

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success("https://cd46-190-175-76-56.ngrok-free.app/spring/suscripcion/resultado")
                .failure("https://cd46-190-175-76-56.ngrok-free.app/spring/suscripcion/resultado")
                .pending("https://cd46-190-175-76-56.ngrok-free.app/spring/suscripcion/resultado")
                .build();

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(Collections.singletonList(item))
                .backUrls(backUrls)
                .autoReturn("approved")
                .externalReference(usuario.getId().toString())
                .build();

        PreferenceClient client = new PreferenceClient();
        Preference preference = client.create(preferenceRequest);
        
        return preference.getId();
    }
}