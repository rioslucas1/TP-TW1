package com.tallerwebi.dominio.servicios;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.ConferenceSolutionKey;
import com.google.api.services.calendar.model.CreateConferenceRequest;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.tallerwebi.dominio.entidades.Clase;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Service
@Transactional
public class ServicioCrearReunionImpl implements ServicioCrearReunion {
    private static final String APPLICATION_NAME = "Sistema de Reservas";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";


    private String tokensDirectory;
    private int oauthPort;

    public ServicioCrearReunionImpl() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("google.properties")) {
            if (input != null) {
                props.load(input);
                this.tokensDirectory = props.getProperty("google.calendar.tokens.directory");
                this.oauthPort = Integer.parseInt(props.getProperty("google.calendar.oauth.port"));
            } else {
                throw new RuntimeException("No se encontró google.properties");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al leer google.properties", e);
        }
    }


    @Override
    public String crearReunionMeet(Clase disponibilidad) {
        try {
            Calendar service = getCalendarService();
            Event evento = crearEvento(disponibilidad);

            Event eventoCreado = service.events()
                    .insert("primary", evento)
                    .setConferenceDataVersion(1)
                    .execute();
            ConferenceData conferenceData = eventoCreado.getConferenceData();
            if (conferenceData != null && conferenceData.getEntryPoints() != null
                    && !conferenceData.getEntryPoints().isEmpty()) {

                String enlaceMeet = conferenceData.getEntryPoints().get(0).getUri();
                disponibilidad.setEnlace_meet(enlaceMeet);

                return enlaceMeet;
            }
            throw new RuntimeException("No se pudo generar el enlace de Google Meet");

        } catch (Exception e) {
            throw new RuntimeException("Error al crear reunión de Google Meet: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean eliminarReunionMeet(String enlaceMeet) {
        try {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Event crearEvento(Clase disponibilidad) {
        Event evento = new Event();

        String titulo = String.format("Clase con %s",
                disponibilidad.getProfesor().getNombre() + " " + disponibilidad.getProfesor().getApellido());
        evento.setSummary(titulo);

        String descripcion = String.format("Clase reservada por %s %s",
                disponibilidad.getAlumno().getNombre(),
                disponibilidad.getAlumno().getApellido());
        evento.setDescription(descripcion);

        LocalDateTime fechaHoraInicio = calcularFechaHoraInicio(disponibilidad);
        LocalDateTime fechaHoraFin = fechaHoraInicio.plusHours(1);

        EventDateTime inicio = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(
                        Date.from(fechaHoraInicio.atZone(ZoneId.systemDefault()).toInstant())))
                .setTimeZone("America/Argentina/Buenos_Aires");
        evento.setStart(inicio);

        EventDateTime fin = new EventDateTime()
                .setDateTime(new com.google.api.client.util.DateTime(
                        Date.from(fechaHoraFin.atZone(ZoneId.systemDefault()).toInstant())))
                .setTimeZone("America/Argentina/Buenos_Aires");
        evento.setEnd(fin);

        EventAttendee[] asistentes = new EventAttendee[] {
                new EventAttendee().setEmail(disponibilidad.getEmailProfesor()),
                new EventAttendee().setEmail(disponibilidad.getMailAlumno())
        };
        evento.setAttendees(List.of(asistentes));

        ConferenceSolutionKey conferenceSolutionKey = new ConferenceSolutionKey();
        conferenceSolutionKey.setType("hangoutsMeet");

        CreateConferenceRequest createConferenceRequest = new CreateConferenceRequest();
        createConferenceRequest.setRequestId(UUID.randomUUID().toString());
        createConferenceRequest.setConferenceSolutionKey(conferenceSolutionKey);

        ConferenceData conferenceData = new ConferenceData();
        conferenceData.setCreateRequest(createConferenceRequest);

        evento.setConferenceData(conferenceData);

        return evento;
    }

    private LocalDateTime calcularFechaHoraInicio(Clase disponibilidad) {
        LocalDate fecha = disponibilidad.getFechaEspecifica();
        if (fecha == null) {

            fecha = LocalDate.now();
        }

        LocalTime hora = LocalTime.parse(disponibilidad.getHora());
        return LocalDateTime.of(fecha, hora);
    }

    private Calendar getCalendarService() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = ServicioCrearReunionImpl.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Archivo de credenciales no encontrado: " + CREDENTIALS_FILE_PATH);
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
