package com.tallerwebi.dominio.servicios;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.tallerwebi.dominio.entidades.Clase;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class ServicioMeetImpl implements ServicioMeet {

    private static final String APPLICATION_NAME = "ClasesYa";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/calendar");

    private GoogleCredential credential;

    @Override
    public void intercambiarCodigoPorTokens(String code) {
        try {
            InputStream in = ServicioMeetImpl.class.getResourceAsStream("/credentials.json");
            GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

            var tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY,
                    clientSecrets.getDetails().getClientId(),
                    clientSecrets.getDetails().getClientSecret(),
                    code,
                    "http://localhost:8080/oauth2callback" // Debe coincidir con Google Console
            ).execute();

            credential = new GoogleCredential.Builder()
                    .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                    .setJsonFactory(JSON_FACTORY)
                    .setClientSecrets(clientSecrets)
                    .build()
                    .setFromTokenResponse(tokenResponse);

            System.out.println("Access Token obtenido correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Calendar obtenerServicioCalendar() throws Exception {
        if (credential == null || credential.getAccessToken() == null) {
            throw new RuntimeException("No hay credenciales disponibles, realiza la autorización OAuth.");
        }

        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Override
    public String crearReunionGoogleMeet(Clase clase) {
        try {
            Calendar service = obtenerServicioCalendar();

            Event event = new Event()
                    .setSummary("Clase con " + (clase.getAlumno() != null ? clase.getAlumno().getNombre() : "Alumno"))
                    .setDescription("Clase generada automáticamente desde ClasesYa con Meet");

            LocalDateTime fechaHoraInicio = clase.getFechaEspecifica().atTime(
                    Integer.parseInt(clase.getHora().split(":")[0]),
                    Integer.parseInt(clase.getHora().split(":")[1])
            );

            Date startDate = Date.from(fechaHoraInicio.atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(fechaHoraInicio.plusHours(1).atZone(ZoneId.systemDefault()).toInstant());

            EventDateTime start = new EventDateTime()
                    .setDateTime(new com.google.api.client.util.DateTime(startDate))
                    .setTimeZone("America/Argentina/Buenos_Aires");
            event.setStart(start);

            EventDateTime end = new EventDateTime()
                    .setDateTime(new com.google.api.client.util.DateTime(endDate))
                    .setTimeZone("America/Argentina/Buenos_Aires");
            event.setEnd(end);

            ConferenceData conferenceData = new ConferenceData();
            CreateConferenceRequest createConferenceRequest = new CreateConferenceRequest();
            ConferenceSolutionKey conferenceSolutionKey = new ConferenceSolutionKey();
            conferenceSolutionKey.setType("hangoutsMeet");
            createConferenceRequest.setConferenceSolutionKey(conferenceSolutionKey);
            createConferenceRequest.setRequestId("clase-ya-" + clase.getId());
            conferenceData.setCreateRequest(createConferenceRequest);
            event.setConferenceData(conferenceData);

            Event createdEvent = service.events().insert("primary", event)
                    .setConferenceDataVersion(1)
                    .execute();

            return createdEvent.getHangoutLink();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}