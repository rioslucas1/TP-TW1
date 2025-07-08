package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.servicios.ServicioMercadoPago;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ControladorPagoSuscripcionTest {

    private ControladorPagoSuscripcion controlador;
    
    private RepositorioUsuario repositorioUsuarioMock;
    private ServicioMercadoPago servicioMercadoPagoMock;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;
    private Usuario usuarioMock;

    @BeforeEach
    public void init() {
        repositorioUsuarioMock = mock(RepositorioUsuario.class);
        servicioMercadoPagoMock = mock(ServicioMercadoPago.class);
        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);
        
        controlador = new ControladorPagoSuscripcion(repositorioUsuarioMock, servicioMercadoPagoMock);
        
        usuarioMock = mock(Alumno.class);
        when(usuarioMock.getId()).thenReturn(1L);
        when(usuarioMock.getEmail()).thenReturn("test@test.com");
        when(usuarioMock.getHabilitado()).thenReturn(false);
        
        when(requestMock.getSession()).thenReturn(sessionMock);
    }

    
    @Test
    public void vistaPagoConUsuarioLogueadoDeberiaRetornarVistaPagoSuscripcion() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
        
        ModelAndView result = controlador.vistaPago(requestMock);
        
        assertThat(result.getViewName(), equalToIgnoringCase("pago-suscripcion"));
    }
    
    @Test
    public void vistaPagoSinUsuarioLogueadoDeberiaRedirigirALogin() {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(null);
        
        ModelAndView result = controlador.vistaPago(requestMock);
        
        assertThat(result.getViewName(), equalToIgnoringCase("redirect:/login"));
    }

    
    @Test
    public void crearPreferenciaConUsuarioLogueadoDeberiaRetornarPreferenceId() throws Exception {
        String expectedPreferenceId = "MP-12345678";
        when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
        when(servicioMercadoPagoMock.crearPreferencia(usuarioMock)).thenReturn(expectedPreferenceId);
        
        String result = controlador.crearPreferencia(requestMock);
        
        assertThat(result, containsString("preferenceId"));
        assertThat(result, containsString(expectedPreferenceId));
        verify(usuarioMock).setSubscriptionId(expectedPreferenceId);
        verify(repositorioUsuarioMock).modificar(usuarioMock);
    }
    
    @Test
    public void crearPreferenciaExitosaDeberiaGuardarSubscriptionIdEnUsuario() throws Exception {
        String expectedPreferenceId = "MP-87654321";
        when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
        when(servicioMercadoPagoMock.crearPreferencia(usuarioMock)).thenReturn(expectedPreferenceId);
        
        controlador.crearPreferencia(requestMock);
        
        verify(usuarioMock).setSubscriptionId(expectedPreferenceId);
        verify(repositorioUsuarioMock).modificar(usuarioMock);
    }

    
    @Test
    public void crearPreferenciaSinUsuarioLogueadoDeberiaRetornarErrorNoLogueado() throws Exception {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(null);
        
        String result = controlador.crearPreferencia(requestMock);
        
        assertThat(result, containsString("error"));
        assertThat(result, containsString("No logueado"));
        verify(repositorioUsuarioMock, never()).modificar(any());
        verify(servicioMercadoPagoMock, never()).crearPreferencia(any());
    }
    
    @Test
    public void crearPreferenciaCuandoMercadoPagoFallaDeberiaRetornarError() throws Exception {
        when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
        when(servicioMercadoPagoMock.crearPreferencia(usuarioMock))
            .thenThrow(new RuntimeException("Error de MercadoPago"));
        
        String result = controlador.crearPreferencia(requestMock);
        
        assertThat(result, containsString("error"));
        assertThat(result, containsString("Error al crear preferencia"));
        verify(repositorioUsuarioMock, never()).modificar(any());
        verify(usuarioMock, never()).setSubscriptionId(any());
    }
    
    @Test
    public void crearPreferenciaCuandoRepositorioFallaDeberiaRetornarError() throws Exception {
        String preferenceId = "MP-123456";
        when(sessionMock.getAttribute("USUARIO")).thenReturn(usuarioMock);
        when(servicioMercadoPagoMock.crearPreferencia(usuarioMock)).thenReturn(preferenceId);
        doThrow(new RuntimeException("Error de BD")).when(repositorioUsuarioMock).modificar(usuarioMock);
        
        String result = controlador.crearPreferencia(requestMock);
        
        assertThat(result, containsString("error"));
        assertThat(result, containsString("Error al crear preferencia"));
        verify(usuarioMock).setSubscriptionId(preferenceId);
    }

    
    @Test
    public void resultadoPagoConEstadoApprovedDeberiaHabilitarUsuario() {
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(usuarioMock);
        
        ModelAndView result = controlador.resultadoPago("approved", "1", "pref_123", requestMock);
        
        assertThat(result.getViewName(), equalToIgnoringCase("resultado-suscripcion"));
        assertThat(result.getModel().get("estado"), equalTo("approved"));
        verify(usuarioMock).setHabilitado(true);
        verify(repositorioUsuarioMock).modificar(usuarioMock);
        verify(sessionMock).setAttribute("USUARIO", usuarioMock);
    }
    
    @Test
    public void resultadoPagoConEstadoRejectedNoDeberiaHabilitarUsuario() {
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(usuarioMock);
        
        ModelAndView result = controlador.resultadoPago("rejected", "1", "pref_123", requestMock);
        
        assertThat(result.getViewName(), equalToIgnoringCase("resultado-suscripcion"));
        assertThat(result.getModel().get("estado"), equalTo("rejected"));
        verify(usuarioMock, never()).setHabilitado(true);
        verify(repositorioUsuarioMock, never()).modificar(usuarioMock);
    }
    
    @Test
    public void resultadoPagoConEstadoPendingNoDeberiaHabilitarUsuario() {
        when(repositorioUsuarioMock.buscarPorId(1L)).thenReturn(usuarioMock);
        
        ModelAndView result = controlador.resultadoPago("pending", "1", "pref_123", requestMock);
        
        assertThat(result.getViewName(), equalToIgnoringCase("resultado-suscripcion"));
        assertThat(result.getModel().get("estado"), equalTo("pending"));
        verify(usuarioMock, never()).setHabilitado(true);
        verify(repositorioUsuarioMock, never()).modificar(usuarioMock);
    }
    
    @Test
    public void resultadoPagoConUsuarioInexistenteNoDeberiaFallar() {
        
        when(repositorioUsuarioMock.buscarPorId(999L)).thenReturn(null);
        
        
        ModelAndView result = controlador.resultadoPago("approved", "999", "pref_123", requestMock);
        
        
        assertThat(result.getViewName(), equalToIgnoringCase("resultado-suscripcion"));
        assertThat(result.getModel().get("estado"), equalTo("approved"));
        verify(repositorioUsuarioMock, never()).modificar(any());
    }
    
    @Test
    public void resultadoPagoSinExternalReferenceNoDeberiaHabilitarUsuario() {
        
        ModelAndView result = controlador.resultadoPago("approved", null, "pref_123", requestMock);
        
        
        assertThat(result.getViewName(), equalToIgnoringCase("resultado-suscripcion"));
        assertThat(result.getModel().get("estado"), equalTo("approved"));
        verify(repositorioUsuarioMock, never()).buscarPorId(any());
        verify(repositorioUsuarioMock, never()).modificar(any());
    }
    
    @Test
    public void resultadoPagoConExternalReferenceInvalidoNoDeberiaFallar() {
        
        ModelAndView result = controlador.resultadoPago("approved", "invalid-id", "pref_123", requestMock);
        
        
        assertThat(result.getViewName(), equalToIgnoringCase("resultado-suscripcion"));
        assertThat(result.getModel().get("estado"), equalTo("approved"));
        verify(repositorioUsuarioMock, never()).buscarPorId(any());
        verify(repositorioUsuarioMock, never()).modificar(any());
    }

    
    
    @Test
    public void resultadoPagoConParametrosNulosDeberiaRetornarVistaConEstadoNull() {
        
        ModelAndView result = controlador.resultadoPago(null, null, null, requestMock);
        
        
        assertThat(result.getViewName(), equalToIgnoringCase("resultado-suscripcion"));
        assertNull(result.getModel().get("estado"));
        verify(repositorioUsuarioMock, never()).buscarPorId(any());
    }
    
    @Test
    public void resultadoPagoConErrorEnRepositorioNoDeberiaFallar() {
        
        when(repositorioUsuarioMock.buscarPorId(1L)).thenThrow(new RuntimeException("Error de BD"));
        
        
        ModelAndView result = controlador.resultadoPago("approved", "1", "pref_123", requestMock);
        
        
        assertThat(result.getViewName(), equalToIgnoringCase("resultado-suscripcion"));
        assertThat(result.getModel().get("estado"), equalTo("approved"));
        
    }

    
    
    @Test
    public void crearPreferenciaConProfesorDeberiaFuncionarIgual() throws Exception {
        
        Usuario profesor = mock(Usuario.class);
        when(profesor.getId()).thenReturn(2L);
        String expectedPreferenceId = "MP-PROF-123";
        when(sessionMock.getAttribute("USUARIO")).thenReturn(profesor);
        when(servicioMercadoPagoMock.crearPreferencia(profesor)).thenReturn(expectedPreferenceId);
        
        
        String result = controlador.crearPreferencia(requestMock);
        
        
        assertThat(result, containsString("preferenceId"));
        assertThat(result, containsString(expectedPreferenceId));
        verify(profesor).setSubscriptionId(expectedPreferenceId);
        verify(repositorioUsuarioMock).modificar(profesor);
    }
}
