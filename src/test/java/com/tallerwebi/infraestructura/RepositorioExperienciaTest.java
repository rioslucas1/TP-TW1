package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioExperiencia;
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
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateInfraestructuraTestConfig.class})
@Transactional
public class RepositorioExperienciaTest {

    @Autowired
    private SessionFactory sessionFactory;
    private RepositorioExperiencia repositorioExperiencia;

    @BeforeEach
    public void init() {
        this.repositorioExperiencia = new RepositorioExperienciaImpl(this.sessionFactory);
    }

    @Test
    @Rollback
    public void cuandoGuardoUnaExperienciaConDatosCorrectosEntoncesSeGuardaEnLaBaseDeDatos() {

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

        ExperienciaEstudiantil experiencia = new ExperienciaEstudiantil();
        experiencia.setProfesor(profesor);
        experiencia.setInstitucion("institucion");
        experiencia.setDescripcion("descripcion");
        experiencia.setFecha("2020-12-15");
        experiencia.setTipoExperiencia("Educación");


        ExperienciaEstudiantil experienciaGuardada = repositorioExperiencia.guardar(experiencia);


        assertNotNull(experienciaGuardada);
        assertNotNull(experienciaGuardada.getId());
        assertThat(experienciaGuardada.getInstitucion(), equalTo("institucion"));
        assertThat(experienciaGuardada.getDescripcion(), equalTo("descripcion"));
        assertThat(experienciaGuardada.getFecha(), equalTo("2020-12-15"));
        assertThat(experienciaGuardada.getTipoExperiencia(), equalTo("Educación"));
        assertThat(experienciaGuardada.getProfesor().getNombre(), equalTo("Carlos"));
    }

    @Test
    @Rollback
    public void dadoQueExisteExperienciaCuandoBuscoPorIdEntoncesRetornaLaExperiencia() {

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

        ExperienciaEstudiantil experiencia = new ExperienciaEstudiantil();
        experiencia.setProfesor(profesor);
        experiencia.setInstitucion("institucion");
        experiencia.setDescripcion("descripcion");
        experiencia.setFecha("2019-06-30");
        experiencia.setTipoExperiencia("Posgrado");
        sessionFactory.getCurrentSession().save(experiencia);

        ExperienciaEstudiantil experienciaEncontrada = repositorioExperiencia.buscarPorId(experiencia.getId());

        assertNotNull(experienciaEncontrada);
        assertThat(experienciaEncontrada.getId(), equalTo(experiencia.getId()));
        assertThat(experienciaEncontrada.getInstitucion(), equalTo("institucion"));
        assertThat(experienciaEncontrada.getDescripcion(), equalTo("descripcion"));
        assertThat(experienciaEncontrada.getFecha(), equalTo("2019-06-30"));
        assertThat(experienciaEncontrada.getTipoExperiencia(), equalTo("Posgrado"));
    }

    @Test
    @Rollback
    public void cuandoBuscoExperienciaPorIdInexistenteEntoncesRetornaNulo() {

        ExperienciaEstudiantil experienciaEncontrada = repositorioExperiencia.buscarPorId(999L);
        assertNull(experienciaEncontrada);
    }

    @Test
    @Rollback
    public void dadoQueExistenExperienciasDeProfesorCuandoObtengoListaPorProfesorIdEntoncesRetornaLasExperiencias() {

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

        ExperienciaEstudiantil experiencia1 = new ExperienciaEstudiantil();
        experiencia1.setProfesor(profesor);
        experiencia1.setInstitucion("institucion");
        experiencia1.setDescripcion("descripcion");
        experiencia1.setFecha("2018-12-10");
        experiencia1.setTipoExperiencia("Grado");
        sessionFactory.getCurrentSession().save(experiencia1);

        ExperienciaEstudiantil experiencia2 = new ExperienciaEstudiantil();
        experiencia2.setProfesor(profesor);
        experiencia2.setInstitucion("institucion2");
        experiencia2.setDescripcion("descripcion2");
        experiencia2.setFecha("2021-03-15");
        experiencia2.setTipoExperiencia("Investigación");
        sessionFactory.getCurrentSession().save(experiencia2);


        List<ExperienciaEstudiantil> experiencias = repositorioExperiencia.obtenerPorProfesorId(profesor.getId());
        assertNotNull(experiencias);
        assertThat(experiencias.size(), equalTo(2));
        assertThat(experiencias, hasItems(experiencia1, experiencia2));
    }

    @Test
    @Rollback
    public void cuandoObtengoExperienciasPorProfesorIdInexistenteEntoncesRetornaListaVacia() {

        List<ExperienciaEstudiantil> experiencias = repositorioExperiencia.obtenerPorProfesorId(999L);
        assertNotNull(experiencias);
        assertThat(experiencias.size(), equalTo(0));
    }

    @Test
    @Rollback
    public void cuandoObtengoTodasLasExperienciasEntoncesRetornaListaCompleta() {

        Tema tema = new Tema();
        tema.setNombre("Historia");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor1 = new Profesor();
        profesor1.setEmail("profesor1@test.com");
        profesor1.setPassword("123456");
        profesor1.setNombre("Laura");
        profesor1.setApellido("Silva");
        profesor1.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor1);

        Profesor profesor2 = new Profesor();
        profesor2.setEmail("profesor2@test.com");
        profesor2.setPassword("123456");
        profesor2.setNombre("Roberto");
        profesor2.setApellido("Vargas");
        profesor2.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor2);

        ExperienciaEstudiantil experiencia1 = new ExperienciaEstudiantil();
        experiencia1.setProfesor(profesor1);
        experiencia1.setInstitucion("institucion");
        experiencia1.setDescripcion("descripcion");
        experiencia1.setFecha("2017-09-20");
        experiencia1.setTipoExperiencia("Doctorado");
        sessionFactory.getCurrentSession().save(experiencia1);

        ExperienciaEstudiantil experiencia2 = new ExperienciaEstudiantil();
        experiencia2.setProfesor(profesor2);
        experiencia2.setInstitucion("institucion2");
        experiencia2.setDescripcion("descripcion2");
        experiencia2.setFecha("2020-01-10");
        experiencia2.setTipoExperiencia("Experiencia Laboral");
        sessionFactory.getCurrentSession().save(experiencia2);

        List<ExperienciaEstudiantil> todasLasExperiencias = repositorioExperiencia.obtenerTodas();
        assertNotNull(todasLasExperiencias);
        assertThat(todasLasExperiencias.size(), equalTo(2));
        assertThat(todasLasExperiencias, hasItems(experiencia1, experiencia2));
    }

    @Test
    @Rollback
    public void dadoQueTengoUnaExperienciaGuardadaCuandoLaEliminoEntoncesYaNoExiste() {

        Tema tema = new Tema();
        tema.setNombre("Literatura");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("Miguel");
        profesor.setApellido("Torres");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        ExperienciaEstudiantil experiencia = new ExperienciaEstudiantil();
        experiencia.setProfesor(profesor);
        experiencia.setInstitucion("institucion");
        experiencia.setDescripcion("Taller de escritura creativa");
        experiencia.setFecha("2022-05-18");
        experiencia.setTipoExperiencia("Curso");
        sessionFactory.getCurrentSession().save(experiencia);

        Long experienciaId = experiencia.getId();
        ExperienciaEstudiantil experienciaAntesDeEliminar = repositorioExperiencia.buscarPorId(experienciaId);
        assertNotNull(experienciaAntesDeEliminar);
        repositorioExperiencia.eliminar(experienciaId);
        ExperienciaEstudiantil experienciaDespuesDeEliminar = repositorioExperiencia.buscarPorId(experienciaId);
        assertNull(experienciaDespuesDeEliminar);
    }

    @Test
    @Rollback
    public void cuandoIntentoEliminarExperienciaInexistenteEntoncesNoOcurreError() {

        assertDoesNotThrow(() -> {
            repositorioExperiencia.eliminar(999L);
        });
    }

    @Test
    @Rollback
    public void dadoQueTengoUnaExperienciaCuandoLaModificoYGuardoEntoncesSeActualiza() {

        Tema tema = new Tema();
        tema.setNombre("Arte");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("Isabel");
        profesor.setApellido("Morales");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        ExperienciaEstudiantil experiencia = new ExperienciaEstudiantil();
        experiencia.setProfesor(profesor);
        experiencia.setInstitucion("Instituto de Artes");
        experiencia.setDescripcion("Descripción inicial");
        experiencia.setFecha("2021-08-12");
        experiencia.setTipoExperiencia("Inicial");
        sessionFactory.getCurrentSession().save(experiencia);

        experiencia.setInstitucion("institucion");
        experiencia.setDescripcion("descripcion");
        experiencia.setTipoExperiencia("Especialización");

        ExperienciaEstudiantil experienciaActualizada = repositorioExperiencia.guardar(experiencia);

        assertNotNull(experienciaActualizada);
        assertThat(experienciaActualizada.getInstitucion(), equalTo("institucion"));
        assertThat(experienciaActualizada.getDescripcion(), equalTo("descripcion"));
        assertThat(experienciaActualizada.getTipoExperiencia(), equalTo("Especialización"));
        ExperienciaEstudiantil experienciaDesdeBD = repositorioExperiencia.buscarPorId(experiencia.getId());
        assertThat(experienciaDesdeBD.getInstitucion(), equalTo("institucion"));
        assertThat(experienciaDesdeBD.getDescripcion(), equalTo("descripcion"));
        assertThat(experienciaDesdeBD.getTipoExperiencia(), equalTo("Especialización"));
    }

    @Test
    @Rollback
    public void cuandoGuardoExperienciaSinProfesorEntoncesLanzaExcepcion() {
        ExperienciaEstudiantil experiencia = new ExperienciaEstudiantil();
        experiencia.setInstitucion("SinProfesor");
        experiencia.setDescripcion("No debería guardarse");
        experiencia.setFecha("2023-01-01");
        experiencia.setTipoExperiencia("Error");

        assertThrows(Exception.class, () -> {
            repositorioExperiencia.guardar(experiencia);
            sessionFactory.getCurrentSession().flush();
        });
    }

    @Test
    @Rollback
    public void cuandoActualizoExperienciaConCamposNulosEntoncesSePersisten() {
        Tema tema = new Tema();
        tema.setNombre("TestNulls");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setNombre("Null");
        profesor.setApellido("Campos");
        profesor.setEmail("null@campos.com");
        profesor.setPassword("abc123");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        ExperienciaEstudiantil experiencia = new ExperienciaEstudiantil();
        experiencia.setProfesor(profesor);
        experiencia.setInstitucion("Instituto");
        experiencia.setDescripcion("Descripción inicial");
        experiencia.setFecha("2022-01-01");
        experiencia.setTipoExperiencia("Curso");
        sessionFactory.getCurrentSession().save(experiencia);

        experiencia.setDescripcion(null);
        experiencia.setTipoExperiencia(null);
        repositorioExperiencia.guardar(experiencia);

        ExperienciaEstudiantil actualizada = repositorioExperiencia.buscarPorId(experiencia.getId());

        assertNull(actualizada.getDescripcion());
        assertNull(actualizada.getTipoExperiencia());
    }

}