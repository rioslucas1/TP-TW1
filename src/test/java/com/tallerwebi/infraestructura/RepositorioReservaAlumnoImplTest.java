package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioReservaAlumno;
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
public class RepositorioReservaAlumnoImplTest {

    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioReservaAlumno repositorioReservaAlumno;
    private Profesor profesor;
    private Alumno alumno;
    private Tema tema;
    private static LocalDate diaLunes = LocalDate.of(2025, 6, 9);
    private static LocalDate diaDomingo = LocalDate.of(2025, 6, 15);

    @BeforeEach
    public void init() {
        this.repositorioReservaAlumno = new RepositorioReservaAlumnoImpl(this.sessionFactory);

        tema = new Tema();
        tema.setNombre("Matemáticas");
        sessionFactory.getCurrentSession().save(tema);

        profesor = new Profesor();
        profesor.setEmail("profesor@unlam.com");
        profesor.setPassword("123456");
        profesor.setNombre("Carlos");
        profesor.setApellido("Rodriguez");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        alumno = new Alumno();
        alumno.setEmail("alumno@unlam.com");
        alumno.setPassword("123456");
        alumno.setNombre("Maria");
        alumno.setApellido("Garcia");
        sessionFactory.getCurrentSession().save(alumno);
    }

    @Test
    @Rollback
    public void dadoQueExistenDisponibilidadesDeProfesorCuandoBuscoPorProfesorEntoncesRetornaListaDeDisponibilidades() {

        Clase disponibilidad1 = new Clase();
        disponibilidad1.setProfesor(profesor);
        disponibilidad1.setDiaSemana("LUNES");
        disponibilidad1.setHora("09:00");
        disponibilidad1.setEstado(EstadoDisponibilidad.DISPONIBLE);
        sessionFactory.getCurrentSession().save(disponibilidad1);

        Clase disponibilidad2 = new Clase();
        disponibilidad2.setProfesor(profesor);
        disponibilidad2.setDiaSemana("MARTES");
        disponibilidad2.setHora("14:00");
        disponibilidad2.setEstado(EstadoDisponibilidad.DISPONIBLE);
        sessionFactory.getCurrentSession().save(disponibilidad2);

        List<Clase> disponibilidadesObtenidas =
                repositorioReservaAlumno.buscarPorProfesor(profesor.getEmail());

        // Then
        assertNotNull(disponibilidadesObtenidas);
        assertEquals(2, disponibilidadesObtenidas.size());
        assertThat(disponibilidadesObtenidas, hasItems(disponibilidad1, disponibilidad2));
    }

    @Test
    @Rollback
    public void cuandoBuscoPorProfesorInexistenteEntoncesRetornaListaVacia() {

        String emailInexistente = "inexistente@unlam.com";
        List<Clase> disponibilidadesObtenidas =
                repositorioReservaAlumno.buscarPorProfesor(emailInexistente);
        assertNotNull(disponibilidadesObtenidas);
        assertEquals(0, disponibilidadesObtenidas.size());
    }

    @Test
    @Rollback
    public void dadoQueExisteDisponibilidadCuandoBuscoPorIdEntoncesRetornaLaDisponibilidad() {

        Clase disponibilidad = new Clase();
        disponibilidad.setProfesor(profesor);
        disponibilidad.setDiaSemana("MIERCOLES");
        disponibilidad.setHora("10:00");
        disponibilidad.setEstado(EstadoDisponibilidad.DISPONIBLE);
        sessionFactory.getCurrentSession().save(disponibilidad);
        Long idDisponibilidad = disponibilidad.getId();
        Clase disponibilidadObtenida =
                repositorioReservaAlumno.buscarPorId(idDisponibilidad);
        assertNotNull(disponibilidadObtenida);

        assertEquals(idDisponibilidad, disponibilidadObtenida.getId());
        assertEquals(profesor, disponibilidadObtenida.getProfesor());
        assertEquals("MIERCOLES", disponibilidadObtenida.getDiaSemana());
        assertEquals("10:00", disponibilidadObtenida.getHora());
    }

    @Test
    @Rollback
    public void cuandoBuscoPorIdInexistenteEntoncesRetornaNulo() {

        Long idInexistente = 999L;
        Clase disponibilidadObtenida =
                repositorioReservaAlumno.buscarPorId(idInexistente);
        assertNull(disponibilidadObtenida);
    }

    @Test
    @Rollback
    public void cuandoGuardoUnaDisponibilidadEntoncesSeGuardaCorrectamenteEnLaBaseDeDatos() {

        Clase nuevaDisponibilidad = new Clase();
        nuevaDisponibilidad.setProfesor(profesor);
        nuevaDisponibilidad.setDiaSemana("JUEVES");
        nuevaDisponibilidad.setHora("16:00");
        nuevaDisponibilidad.setEstado(EstadoDisponibilidad.DISPONIBLE);
        repositorioReservaAlumno.guardar(nuevaDisponibilidad);
        Long idGuardado = nuevaDisponibilidad.getId();
        assertNotNull(idGuardado);
        Clase disponibilidadGuardada =
                repositorioReservaAlumno.buscarPorId(idGuardado);
        assertNotNull(disponibilidadGuardada);

        assertEquals(profesor, disponibilidadGuardada.getProfesor());
        assertEquals("JUEVES", disponibilidadGuardada.getDiaSemana());
        assertEquals("16:00", disponibilidadGuardada.getHora());
        assertEquals(EstadoDisponibilidad.DISPONIBLE, disponibilidadGuardada.getEstado());
    }

    @Test
    @Rollback
    public void cuandoModificoUnaDisponibilidadExistenteEntoncesSeActualizaCorrectamente() {

        Clase disponibilidad = new Clase();
        disponibilidad.setProfesor(profesor);
        disponibilidad.setDiaSemana("VIERNES");
        disponibilidad.setHora("11:00");
        disponibilidad.setEstado(EstadoDisponibilidad.DISPONIBLE);
        sessionFactory.getCurrentSession().save(disponibilidad);

        Long idDisponibilidad = disponibilidad.getId();
        disponibilidad.setAlumno(alumno);
        disponibilidad.setEstado(EstadoDisponibilidad.RESERVADO);
        repositorioReservaAlumno.guardar(disponibilidad);


        Clase disponibilidadModificada =
                repositorioReservaAlumno.buscarPorId(idDisponibilidad);
        assertNotNull(disponibilidadModificada);
        assertEquals(alumno, disponibilidadModificada.getAlumno());
        assertEquals(EstadoDisponibilidad.RESERVADO, disponibilidadModificada.getEstado());
    }

    @Test
    @Rollback
    public void dadoQueExistenDisponibilidadesEnFechaEspecificaCuandoBuscoPorProfesorDiaFechaEntoncesRetornaDisponibilidades() {

        Clase disponibilidad1 = new Clase();
        disponibilidad1.setProfesor(profesor);
        disponibilidad1.setDiaSemana("LUNES");
        disponibilidad1.setHora("09:00");
        disponibilidad1.setFechaEspecifica(diaLunes);
        disponibilidad1.setEstado(EstadoDisponibilidad.DISPONIBLE);
        sessionFactory.getCurrentSession().save(disponibilidad1);

        Clase disponibilidad2 = new Clase();
        disponibilidad2.setProfesor(profesor);
        disponibilidad2.setDiaSemana("LUNES");
        disponibilidad2.setHora("14:00");
        disponibilidad2.setFechaEspecifica(diaLunes);
        disponibilidad2.setEstado(EstadoDisponibilidad.DISPONIBLE);
        sessionFactory.getCurrentSession().save(disponibilidad2);

        List<Clase> disponibilidadesObtenidas =
                repositorioReservaAlumno.buscarPorProfesorDiaFecha(
                        profesor.getEmail(), "LUNES", diaLunes);
        assertNotNull(disponibilidadesObtenidas);
        assertEquals(2, disponibilidadesObtenidas.size());
        assertThat(disponibilidadesObtenidas, hasItems(disponibilidad1, disponibilidad2));
    }

    @Test
    @Rollback
    public void cuandoBuscoPorProfesorDiaFechaSinResultadosEntoncesRetornaListaVacia() {

        List<Clase> disponibilidadesObtenidas =
                repositorioReservaAlumno.buscarPorProfesorDiaFecha(
                        profesor.getEmail(), "DOMINGO", diaDomingo);

        assertNotNull(disponibilidadesObtenidas);
        assertEquals(0, disponibilidadesObtenidas.size());
    }

    @Test
    @Rollback
    public void dadoQueExisteDisponibilidadConEmailProfesorCuandoBuscoPorProfesorDiaHoraEntoncesRetornaDisponibilidad() {
        // Given
        Clase disponibilidad = new Clase();
        disponibilidad.setProfesor(profesor);
        disponibilidad.setDiaSemana("LUNES");
        disponibilidad.setHora("09:00");
        disponibilidad.setEstado(EstadoDisponibilidad.DISPONIBLE);
        sessionFactory.getCurrentSession().save(disponibilidad);

        Clase disponibilidadObtenida =
                repositorioReservaAlumno.buscarPorProfesorDiaHora(
                        profesor.getEmail(), "LUNES", "09:00");

        assertNotNull(disponibilidadObtenida);

        assertEquals(profesor, disponibilidadObtenida.getProfesor());
        assertEquals("LUNES", disponibilidadObtenida.getDiaSemana());
        assertEquals("09:00", disponibilidadObtenida.getHora());

    }

    @Test
    @Rollback
    public void cuandoBuscoPorProfesorDiaHoraInexistenteEntoncesRetornaNulo() {

        Clase disponibilidadObtenida =
                repositorioReservaAlumno.buscarPorProfesorDiaHora(
                        profesor.getEmail(), "DOMINGO", "23:00");
        assertNull(disponibilidadObtenida);
    }

    @Test
    @Rollback
    public void dadoQueExistenVariasDisponibilidadesCuandoBuscoPorProfesorEntoncesRetornaTodasLasDisponibilidades() {

        Clase disponibilidad1 = new Clase();
        disponibilidad1.setProfesor(profesor);
        disponibilidad1.setDiaSemana("LUNES");
        disponibilidad1.setHora("08:00");
        disponibilidad1.setEstado(EstadoDisponibilidad.DISPONIBLE);
        sessionFactory.getCurrentSession().save(disponibilidad1);

        Clase disponibilidad2 = new Clase();
        disponibilidad2.setProfesor(profesor);
        disponibilidad2.setDiaSemana("LUNES");
        disponibilidad2.setHora("10:00");
        disponibilidad2.setEstado(EstadoDisponibilidad.RESERVADO);
        disponibilidad2.setAlumno(alumno);
        sessionFactory.getCurrentSession().save(disponibilidad2);

        Clase disponibilidad3 = new Clase();
        disponibilidad3.setProfesor(profesor);
        disponibilidad3.setDiaSemana("MARTES");
        disponibilidad3.setHora("14:00");
        disponibilidad3.setEstado(EstadoDisponibilidad.OCUPADO);
        sessionFactory.getCurrentSession().save(disponibilidad3);


        List<Clase> disponibilidadesObtenidas =
                repositorioReservaAlumno.buscarPorProfesor(profesor.getEmail());


        assertNotNull(disponibilidadesObtenidas);
        assertEquals(3, disponibilidadesObtenidas.size());
        assertThat(disponibilidadesObtenidas, hasItems(disponibilidad1, disponibilidad2, disponibilidad3));
    }

    @Test
    @Rollback
    public void cuandoBuscoConParametrosNulosEntoncesNoLanzaExcepcion() {

        List<Clase> resultado1 = repositorioReservaAlumno.buscarPorProfesor(null);
        Clase resultado2 = repositorioReservaAlumno.buscarPorProfesorDiaHora(null, null, null);
        Clase resultado3 = repositorioReservaAlumno.buscarPorId(null);
        List<Clase> resultado4 = repositorioReservaAlumno.buscarPorProfesorDiaFecha(null, null, null);
        assertNotNull(resultado1);
        assertNotNull(resultado4);
    }

    @Test
    @Rollback
    public void guardarDisponibilidadConFechaEspecificaYRecuperarla() {
        Clase disponibilidad = new Clase();
        disponibilidad.setProfesor(profesor);
        disponibilidad.setDiaSemana("VIERNES");
        disponibilidad.setHora("15:00");
        disponibilidad.setEstado(EstadoDisponibilidad.DISPONIBLE);
        disponibilidad.setFechaEspecifica(LocalDate.of(2025, 6, 13));
        repositorioReservaAlumno.guardar(disponibilidad);

        Clase recuperada = repositorioReservaAlumno.buscarPorId(disponibilidad.getId());

        assertNotNull(recuperada);
        assertEquals(LocalDate.of(2025, 6, 13), recuperada.getFechaEspecifica());
    }

    @Test
    @Rollback
    public void actualizarEstadoAOcupadoSinAlumnoDebeGuardarCorrectamente() {
        Clase disponibilidad = new Clase();
        disponibilidad.setProfesor(profesor);
        disponibilidad.setDiaSemana("MIERCOLES");
        disponibilidad.setHora("13:00");
        disponibilidad.setEstado(EstadoDisponibilidad.DISPONIBLE);
        sessionFactory.getCurrentSession().save(disponibilidad);

        disponibilidad.setEstado(EstadoDisponibilidad.OCUPADO);
        repositorioReservaAlumno.guardar(disponibilidad);

        Clase actualizada = repositorioReservaAlumno.buscarPorId(disponibilidad.getId());
        assertEquals(EstadoDisponibilidad.OCUPADO, actualizada.getEstado());
        assertNull(actualizada.getAlumno());
    }
}