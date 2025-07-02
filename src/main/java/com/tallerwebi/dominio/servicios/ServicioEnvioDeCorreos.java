package com.tallerwebi.dominio.servicios;


import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Map;
import java.util.Properties;


public class ServicioEnvioDeCorreos {

    private final String remitente = "eze.mendoza100@gmail.com";
    private final String password = "wymi fefi wwgz jvbt";

    public enum TipoCorreo {
        BIENVENIDA, RECUPERAR_PASSWORD, NOTIFICACION
    }

    public void enviarCorreo(String destinatario, String asunto, TipoCorreo tipo, Map<String, String> datos) {
        String cuerpoHtml = generarHtmlSegunTipo(tipo, datos);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(remitente, password);
            }
        });

        try {
            Message mensaje = new MimeMessage(session);

            mensaje.setFrom(new InternetAddress("noreply@clasesya"));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            mensaje.setSubject(asunto);
            mensaje.setContent(cuerpoHtml, "text/html; charset=utf-8");

            Transport.send(mensaje);
            System.out.println("Correo enviado correctamente.");
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar correo", e);
        }
    }

    private String generarHtmlSegunTipo(TipoCorreo tipo, Map<String, String> datos) {
        switch (tipo) {
            case BIENVENIDA:
             return String.format(
                "<div style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 40px;'>"
                    + "<div style='max-width: 600px; margin: auto; background-color: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);'>"
                    + "  <div style='text-align: center;'>"
                    + "    <a href='https://clasesya.com' style='text-decoration: none; font-size: 28px; font-weight: bold; color: #195755;'>Clases<span style='color: #f4b400;'>Ya</span></a>"
                    + "    <h2 style='color: #195755; margin-top: 20px;'>¡Bienvenido, %s!</h2>"
                    + "  </div>"
                    + "  <p style='font-size: 16px; color: #333333; line-height: 1.6; margin-top: 20px;'>"
                    + "    Nos alegra que te hayas unido a <strong>ClasesYa</strong>. A partir de ahora vas a poder reservar clases, conocer a nuestros profesores y organizar tu aprendizaje de forma personalizada."
                    + "  </p>"
                    + "  <p style='font-size: 16px; color: #333333; line-height: 1.6;'>"
                    + "    Para comenzar, ingresá a tu cuenta desde el siguiente botón:"
                    + "  </p>"
                    + "  <div style='text-align: center; margin: 30px 0;'>"
                    + "    <a href='https://clasesya.com/login' style='background-color: #195755; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-size: 16px;'>Ingresar a mi cuenta</a>"
                    + "  </div>"
                    + "  <p style='font-size: 14px; color: #777777;'>"
                    + "    Si tenés alguna pregunta o necesitás ayuda, no dudes en escribirnos a <a href='mailto:soporte@clasesya.com'>soporte@clasesya.com</a>."
                    + "  </p>"
                    + "  <hr style='margin: 40px 0; border: none; border-top: 1px solid #dddddd;'>"
                    + "  <p style='font-size: 12px; color: #aaaaaa; text-align: center;'>"
                    + "    © 2025 ClasesYa. Todos los derechos reservados."
                    + "  </p>"
                    + "</div>"
                + "</div>",
                datos.getOrDefault("nombre", "usuario")
            );



            case RECUPERAR_PASSWORD:
                return String.format(
                        "<div style='font-family: Arial;'>" +
                                "<h2>Hola %s,</h2>" +
                                "<p>Hacé clic en el siguiente enlace para recuperar tu contraseña:</p>" +
                                "<a href='%s'>Recuperar contraseña</a>" +
                                "</div>",
                        datos.getOrDefault("nombre", "usuario"),
                        datos.getOrDefault("link", "#"));

                    case NOTIFICACION:
                        return String.format(
                            "<div style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 40px;'>"
                                + "<div style='max-width: 600px; margin: auto; background-color: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);'>"
                                + "  <div style='text-align: center;'>"
                                + "    <a href='https://clasesya.com' style='text-decoration: none; font-size: 28px; font-weight: bold; color: #195755;'>Clases<span style='color: #f4b400;'>Ya</span></a>"
                                + "    <h2 style='color: #195755; margin-top: 20px;'>Hola %s,</h2>"
                                + "  </div>"
                                + "  <p style='font-size: 16px; color: #333333; line-height: 1.6; margin-top: 20px;'>"
                                + "    %s"
                                + "  </p>"
                                + "  <div style='text-align: center; margin: 30px 0;'>"
                                + "    <a href='%s' style='background-color: #195755; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-size: 16px;'>Ver notificación</a>"
                                + "  </div>"
                                + "  <p style='font-size: 14px; color: #777777;'>"
                                + "    Si tenés alguna pregunta o necesitás ayuda, podés contactarnos en <a href='mailto:soporte@clasesya.com'>soporte@clasesya.com</a>."
                                + "  </p>"
                                + "  <hr style='margin: 40px 0; border: none; border-top: 1px solid #dddddd;'>"
                                + "  <p style='font-size: 12px; color: #aaaaaa; text-align: center;'>"
                                + "    © 2025 ClasesYa. Todos los derechos reservados."
                                + "  </p>"
                                + "</div>"
                            + "</div>",
                            datos.getOrDefault("nombre", "usuario"),
                            datos.getOrDefault("mensaje", "Tenés una nueva notificación."),
                            datos.getOrDefault("url", "https://clasesya.com")
                        );


            default:
                return "<div><p>Mensaje genérico</p></div>";
        }
    }
}
