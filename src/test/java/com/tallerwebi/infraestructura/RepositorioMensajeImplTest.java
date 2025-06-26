package com.tallerwebi.infraestructura;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Mensaje;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.infraestructura.RepositorioMensajeImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class RepositorioMensajeImplTest {


        private EntityManager entityManager;
        private RepositorioMensajeImpl repositorioMensaje;

        @BeforeEach
        public void init() {
            entityManager = mock(EntityManager.class);
            repositorioMensaje = new RepositorioMensajeImpl();
            // Inyectar manualmente el EntityManager
            var entityManagerField = RepositorioMensajeImpl.class.getDeclaredFields()[0];
            entityManagerField.setAccessible(true);
            try {
                entityManagerField.set(repositorioMensaje, entityManager);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        public void deberiaGuardarMensajeCorrectamente() {
            Mensaje mensaje = new Mensaje();
            mensaje.setContenido("Hola");
            mensaje.setFecha(LocalDateTime.now());

            repositorioMensaje.guardar(mensaje);

            verify(entityManager).persist(mensaje);
        }

        @Test
        public void deberiaObtenerConversacionEntreAlumnoYProfesor() {
            String alumno = "Angel";
            String profesor = "ProfesorX";

            Mensaje m1 = new Mensaje();
            m1.setContenido("Hola profe");
            m1.setEmisor(alumno);

            Mensaje m2 = new Mensaje();
            m2.setContenido("Hola alumno");
            m2.setEmisor(profesor);

            List<Mensaje> mensajesEsperados = Arrays.asList(m1, m2);

            TypedQuery<Mensaje> queryMock = mock(TypedQuery.class);
            when(entityManager.createQuery(anyString(), eq(Mensaje.class))).thenReturn(queryMock);
            when(queryMock.setParameter(anyString(), any())).thenReturn(queryMock);
            when(queryMock.getResultList()).thenReturn(mensajesEsperados);

            List<Mensaje> resultado = repositorioMensaje.obtenerConversacion(alumno, profesor);

            assertEquals(2, resultado.size());
            assertTrue(resultado.contains(m1));
            assertTrue(resultado.contains(m2));
        }

        

}
