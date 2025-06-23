package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioDisponibilidadProfesor;
import com.tallerwebi.dominio.entidades.*;
import com.tallerwebi.infraestructura.config.HibernateInfraestructuraTestConfig;
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
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateInfraestructuraTestConfig.class})
@Transactional
public class RepositorioDisponibilidadProfesorImplTest {

    @Autowired
    private SessionFactory sessionFactory;
    private RepositorioDisponibilidadProfesor repositorioDisponibilidadProfesor;

    @BeforeEach
    public void init() {
        this.repositorioDisponibilidadProfesor = new RepositorioDisponibilidadProfesorImpl(this.sessionFactory);
    }

    @Test
    @Rollback
    public void cuandoGuardoUnaDisponibilidadEntoncesSeGuardaCorrectamenteEnLaBaseDeDatos() {

        Tema tema = new Tema();
        tema.setNombre("Matemáticas");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("Carlos");
        profesor.setApellido("Rodriguez");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Clase disponibilidad = new Clase();
        disponibilidad.setProfesor(profesor);
        disponibilidad.setDiaSemana("Lunes");
        disponibilidad.setHora("10:00");
        disponibilidad.setFechaEspecifica(LocalDate.now().plusDays(1));
        disponibilidad.setEstado(EstadoDisponibilidad.DISPONIBLE);

        repositorioDisponibilidadProfesor.guardar(disponibilidad);

        String hql = "FROM Clase WHERE profesor.id = :profesorId AND diaSemana = :dia";
        Query query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("profesorId", profesor.getId());
        query.setParameter("dia", "Lunes");
        Clase disponibilidadGuardada = (Clase) query.getSingleResult();

        assertNotNull(disponibilidadGuardada);
        assertEquals("Lunes", disponibilidadGuardada.getDiaSemana());
        assertEquals("10:00", disponibilidadGuardada.getHora());
        assertEquals(profesor.getId(), disponibilidadGuardada.getProfesor().getId());
        assertEquals(EstadoDisponibilidad.DISPONIBLE, disponibilidadGuardada.getEstado());
    }

    @Test
    @Rollback
    public void cuandoBuscoDisponibilidadesPorProfesorEntoncesRetornaLaListaCorrecta() {

        Tema tema = new Tema();
        tema.setNombre("Física");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("Ana");
        profesor.setApellido("Lopez");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);


        Clase disponibilidad1 = new Clase();
        disponibilidad1.setProfesor(profesor);
        disponibilidad1.setDiaSemana("Lunes");
        disponibilidad1.setHora("09:00");
        disponibilidad1.setEstado(EstadoDisponibilidad.DISPONIBLE);
        sessionFactory.getCurrentSession().save(disponibilidad1);

        Clase disponibilidad2 = new Clase();
        disponibilidad2.setProfesor(profesor);
        disponibilidad2.setDiaSemana("Martes");
        disponibilidad2.setHora("14:00");
        disponibilidad2.setEstado(EstadoDisponibilidad.DISPONIBLE);
        sessionFactory.getCurrentSession().save(disponibilidad2);

        List<Clase> disponibilidades = repositorioDisponibilidadProfesor.buscarPorProfesor(profesor);

        assertNotNull(disponibilidades);
        assertEquals(2, disponibilidades.size());
        assertThat(disponibilidades, hasItems(disponibilidad1, disponibilidad2));
    }

    @Test
    @Rollback
    public void cuandoBuscoDisponibilidadPorProfesorDiaYHoraEntoncesRetornaLaDisponibilidadCorrecta() {

        Tema tema = new Tema();
        tema.setNombre("Química");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("Pedro");
        profesor.setApellido("Martinez");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Clase disponibilidad = new Clase();
        disponibilidad.setProfesor(profesor);
        disponibilidad.setDiaSemana("Miércoles");
        disponibilidad.setHora("16:00");
        disponibilidad.setEstado(EstadoDisponibilidad.DISPONIBLE);
        sessionFactory.getCurrentSession().save(disponibilidad);

        Clase encontrada = repositorioDisponibilidadProfesor.buscarPorProfesorDiaHora(
                profesor, "Miércoles", "16:00");

        assertNotNull(encontrada);
        assertEquals("Miércoles", encontrada.getDiaSemana());
        assertEquals("16:00", encontrada.getHora());
        assertEquals(profesor.getId(), encontrada.getProfesor().getId());
    }

    @Test
    @Rollback
    public void cuandoBuscoDisponibilidadPorProfesorDiaYHoraInexistenteEntoncesRetornaNulo() {

        Tema tema = new Tema();
        tema.setNombre("Historia");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("Laura");
        profesor.setApellido("Gonzalez");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Clase encontrada = repositorioDisponibilidadProfesor.buscarPorProfesorDiaHora(
                profesor, "Viernes", "20:00");

        assertNull(encontrada);
    }

    @Test
    @Rollback
    public void cuandoBuscoDisponibilidadesPorProfesorDiaYFechaEntoncesRetornaLaListaCorrecta() {
        Tema tema = new Tema();
        tema.setNombre("Literatura");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("Miguel");
        profesor.setApellido("Herrera");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        LocalDate fechaEspecifica = LocalDate.now().plusDays(3);
        Clase disponibilidad1 = new Clase();
        disponibilidad1.setProfesor(profesor);
        disponibilidad1.setDiaSemana("Jueves");
        disponibilidad1.setHora("10:00");
        disponibilidad1.setFechaEspecifica(fechaEspecifica);
        disponibilidad1.setEstado(EstadoDisponibilidad.DISPONIBLE);
        sessionFactory.getCurrentSession().save(disponibilidad1);

        Clase disponibilidad2 = new Clase();
        disponibilidad2.setProfesor(profesor);
        disponibilidad2.setDiaSemana("Jueves");
        disponibilidad2.setHora("15:00");
        disponibilidad2.setFechaEspecifica(fechaEspecifica);
        disponibilidad2.setEstado(EstadoDisponibilidad.DISPONIBLE);
        sessionFactory.getCurrentSession().save(disponibilidad2);
        List<Clase> disponibilidades = repositorioDisponibilidadProfesor.buscarPorProfesorDiaFecha(
                profesor, "Jueves", fechaEspecifica);
        assertNotNull(disponibilidades);
        assertEquals(2, disponibilidades.size());
        assertThat(disponibilidades, hasItems(disponibilidad1, disponibilidad2));
    }

    @Test
    @Rollback
    public void cuandoBuscoDisponibilidadPorProfesorDiaHoraYFechaEntoncesRetornaLaDisponibilidadCorrecta() {
        Tema tema = new Tema();
        tema.setNombre("Biología");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("Sofia");
        profesor.setApellido("Ruiz");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        LocalDate fechaEspecifica = LocalDate.now().plusDays(5);
        Clase disponibilidad = new Clase();
        disponibilidad.setProfesor(profesor);
        disponibilidad.setDiaSemana("Sábado");
        disponibilidad.setHora("11:00");
        disponibilidad.setFechaEspecifica(fechaEspecifica);
        disponibilidad.setEstado(EstadoDisponibilidad.DISPONIBLE);
        sessionFactory.getCurrentSession().save(disponibilidad);

        Clase encontrada = repositorioDisponibilidadProfesor.buscarPorProfesorDiaHoraFecha(
                profesor, "Sábado", "11:00", fechaEspecifica);
        assertNotNull(encontrada);

        assertEquals("Sábado", encontrada.getDiaSemana());
        assertEquals("11:00", encontrada.getHora());
        assertEquals(fechaEspecifica, encontrada.getFechaEspecifica());
        assertEquals(profesor.getId(), encontrada.getProfesor().getId());
    }

    @Test
    @Rollback
    public void cuandoEliminoUnaDisponibilidadEntoncesSeEliminaCorrectamenteDeLaBaseDeDatos() {

        Tema tema = new Tema();
        tema.setNombre("Geografía");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("Ricardo");
        profesor.setApellido("Morales");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Clase disponibilidad = new Clase();
        disponibilidad.setProfesor(profesor);
        disponibilidad.setDiaSemana("Domingo");
        disponibilidad.setHora("13:00");
        disponibilidad.setEstado(EstadoDisponibilidad.DISPONIBLE);
        sessionFactory.getCurrentSession().save(disponibilidad);
        Clase disponibilidadGuardada = repositorioDisponibilidadProfesor.buscarPorProfesorDiaHora(
                profesor, "Domingo", "13:00");
        assertNotNull(disponibilidadGuardada);
        repositorioDisponibilidadProfesor.eliminar(disponibilidadGuardada);
        Clase disponibilidadEliminada = repositorioDisponibilidadProfesor.buscarPorProfesorDiaHora(
                profesor, "Domingo", "13:00");
        assertNull(disponibilidadEliminada);
    }

    @Test
    @Rollback
    public void cuandoModificoUnaDisponibilidadEntoncesSeActualizaCorrectamente() {

        Tema tema = new Tema();
        tema.setNombre("Arte");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("Elena");
        profesor.setApellido("Vega");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);
        Clase disponibilidad = new Clase();
        disponibilidad.setProfesor(profesor);
        disponibilidad.setDiaSemana("Lunes");
        disponibilidad.setHora("08:00");
        disponibilidad.setEstado(EstadoDisponibilidad.DISPONIBLE);
        sessionFactory.getCurrentSession().save(disponibilidad);
        disponibilidad.setHora("09:00");
        disponibilidad.setEstado(EstadoDisponibilidad.RESERVADO);
        repositorioDisponibilidadProfesor.guardar(disponibilidad); // saveOrUpdate
        String hql = "FROM Clase WHERE profesor.id = :profesorId AND diaSemana = :dia";
        Query query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("profesorId", profesor.getId());
        query.setParameter("dia", "Lunes");
        Clase disponibilidadModificada = (Clase) query.getSingleResult();

        assertEquals("09:00", disponibilidadModificada.getHora());
        assertEquals(EstadoDisponibilidad.RESERVADO, disponibilidadModificada.getEstado());
    }
}
