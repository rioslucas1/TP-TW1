package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioTema;
import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Tema;
import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

 @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = {HibernateInfraestructuraTestConfig.class})
    @Transactional
    public class RepositorioTemaImplTest {

        @Autowired
        private SessionFactory sessionFactory;
        private RepositorioTema repositorioTema;

        @BeforeEach
        public void init() {
            this.repositorioTema = new RepositorioTemaImpl(this.sessionFactory);
        }

        @Test
        @Rollback
        public void cuandoBuscoPorIdExistenteEntoncesRetornaElTema() {

            Tema tema = new Tema();
            tema.setNombre("Matemáticas");
            sessionFactory.getCurrentSession().save(tema);

            sessionFactory.getCurrentSession().flush();
            Long idGuardado = tema.getId();

            Tema temaEncontrado = repositorioTema.buscarPorId(idGuardado);

            assertNotNull(temaEncontrado);
            assertEquals(idGuardado, temaEncontrado.getId());
            assertEquals("Matemáticas", temaEncontrado.getNombre());
        }

        @Test
        @Rollback
        public void cuandoBuscoPorIdInexistenteEntoncesRetornaNulo() {

            Tema temaEncontrado = repositorioTema.buscarPorId(999L);

            assertNull(temaEncontrado);
        }

        @Test
        @Rollback
        public void cuandoObtiengoTodosLosTemasEntoncesRetornaListaCompleta() {

            Tema tema1 = new Tema();
            tema1.setNombre("Física");
            sessionFactory.getCurrentSession().save(tema1);

            Tema tema2 = new Tema();
            tema2.setNombre("Química");
            sessionFactory.getCurrentSession().save(tema2);

            Tema tema3 = new Tema();
            tema3.setNombre("Biología");
            sessionFactory.getCurrentSession().save(tema3);

            List<Tema> temasObtenidos = repositorioTema.obtenerTodos();

            assertNotNull(temasObtenidos);
            assertEquals(3, temasObtenidos.size());
            assertThat(temasObtenidos, hasItems(tema1, tema2, tema3));

            List<String> nombresObtenidos = temasObtenidos.stream()
                    .map(Tema::getNombre)
                    .collect(java.util.stream.Collectors.toList());
            assertThat(nombresObtenidos, containsInAnyOrder("Física", "Química", "Biología"));
        }

        @Test
        @Rollback
        public void cuandoNoHayTemasEntoncesObtenerTodosRetornaListaVacia() {

            List<Tema> temasObtenidos = repositorioTema.obtenerTodos();


            assertNotNull(temasObtenidos);
            assertEquals(0, temasObtenidos.size());
            assertTrue(temasObtenidos.isEmpty());
        }

        @Test
        @Rollback
        public void cuandoGuardoUnTemaYLuegoBuscoPorIdEntoncesLoEncuentroCorrectamente() {

            Tema tema = new Tema();
            tema.setNombre("Historia");

            sessionFactory.getCurrentSession().save(tema);
            sessionFactory.getCurrentSession().flush();

            Tema temaEncontrado = repositorioTema.buscarPorId(tema.getId());

            assertNotNull(temaEncontrado);
            assertEquals("Historia", temaEncontrado.getNombre());
            assertEquals(tema.getId(), temaEncontrado.getId());
        }

        @Test
        @Rollback
        public void cuandoHayUnSoloTemaEntoncesObtenerTodosRetornaListaConUnElemento() {

            Tema tema = new Tema();
            tema.setNombre("Literatura");
            sessionFactory.getCurrentSession().save(tema);

            List<Tema> temasObtenidos = repositorioTema.obtenerTodos();

            assertNotNull(temasObtenidos);

            assertEquals(1, temasObtenidos.size());
            assertEquals("Literatura", temasObtenidos.get(0).getNombre());
            assertEquals(tema, temasObtenidos.get(0));

        }

        @Test
        @Rollback
        public void verificarQueLosDatosSeGuardanCorrectamenteEnLaBaseDeDatos() {

            Tema tema = new Tema();
            tema.setNombre("Programación Java");
            sessionFactory.getCurrentSession().save(tema);
            sessionFactory.getCurrentSession().flush();

            String hql = "FROM Tema WHERE nombre = :nombre";
            Query query = sessionFactory.getCurrentSession().createQuery(hql);
            query.setParameter("nombre", "Programación Java");
            Tema temaVerificado = (Tema) query.getSingleResult();

            assertNotNull(temaVerificado);
            assertEquals("Programación Java", temaVerificado.getNombre());
            assertNotNull(temaVerificado.getId());
            Tema temaDesdeRepositorio = repositorioTema.buscarPorId(temaVerificado.getId());
            assertEquals(temaVerificado, temaDesdeRepositorio);
        }
        }

