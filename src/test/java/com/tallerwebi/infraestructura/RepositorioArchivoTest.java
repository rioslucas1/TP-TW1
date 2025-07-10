package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioArchivo;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateInfraestructuraTestConfig.class})
@Transactional
public class RepositorioArchivoTest {

    @Autowired
    private SessionFactory sessionFactory;
    private RepositorioArchivo repositorioArchivo;

    @BeforeEach
    public void init() {
        this.repositorioArchivo = new RepositorioArchivoImpl(this.sessionFactory);
    }

    @Test
    @Rollback
    public void cuandoGuardoUnArchivoConDatosCorrectosEntoncesSeGuardaEnLaBaseDeDatos() {

        Tema tema = new Tema();
        tema.setNombre("Programación");
        sessionFactory.getCurrentSession().save(tema);
        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("nombreprofesor1");
        profesor.setApellido("apellidoprofesor1");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("nombrealumno1");
        alumno.setApellido("apellidoalumno1");
        sessionFactory.getCurrentSession().save(alumno);

        Archivo archivo = new Archivo();
        archivo.setNombre("ejercicios.pdf");
        archivo.setTipoContenido("application/pdf");
        archivo.setRutaAlmacenamiento("/uploads/ejercicios.pdf");
        archivo.setProfesor(profesor);
        archivo.setAlumno(alumno);
        archivo.setFechaSubida(LocalDateTime.now());

        repositorioArchivo.guardarArchivo(archivo);

        String hql = "FROM Archivo WHERE nombre = :nombre";
        Query query = sessionFactory.getCurrentSession().createQuery(hql);
        query.setParameter("nombre", "ejercicios.pdf");
        Archivo archivoGuardado = (Archivo) query.getSingleResult();

        assertNotNull(archivoGuardado);
        assertThat(archivoGuardado.getNombre(), equalTo("ejercicios.pdf"));
        assertThat(archivoGuardado.getTipoContenido(), equalTo("application/pdf"));
        assertThat(archivoGuardado.getProfesor(), equalTo(profesor));
        assertThat(archivoGuardado.getAlumno(), equalTo(alumno));
    }

    @Test
    @Rollback
    public void dadoQueExisteUnArchivoEnLaBDCuandoBuscoPorIdMeDevuelveElArchivo() {

        Tema tema = new Tema();
        tema.setNombre("Diseño");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("nombreprofesor2");
        profesor.setApellido("apellidoprofesor2");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("nombrealumno2");
        alumno.setApellido("apellidoalumno2");
        sessionFactory.getCurrentSession().save(alumno);

        Archivo archivo = new Archivo();
        archivo.setNombre("tarea.docx");
        archivo.setTipoContenido("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        archivo.setRutaAlmacenamiento("/uploads/tarea.docx");
        archivo.setProfesor(profesor);
        archivo.setAlumno(alumno);
        archivo.setFechaSubida(LocalDateTime.now());
        sessionFactory.getCurrentSession().save(archivo);

        Archivo archivoEncontrado = repositorioArchivo.buscarArchivoPorId(archivo.getId());

        assertNotNull(archivoEncontrado);
        assertThat(archivoEncontrado.getId(), equalTo(archivo.getId()));
        assertThat(archivoEncontrado.getNombre(), equalTo("tarea.docx"));
        assertThat(archivoEncontrado.getProfesor(), equalTo(profesor));
        assertThat(archivoEncontrado.getAlumno(), equalTo(alumno));
    }

    @Test
    @Rollback
    public void cuandoBuscoArchivoPorIdInexistenteEntoncesRetornaNulo() {
        Archivo archivoEncontrado = repositorioArchivo.buscarArchivoPorId(999L);
        assertNull(archivoEncontrado);
    }

    @Test
    @Rollback
    public void dadoQueExistenArchivosDeUnAlumnoCuandoObtengoPorAlumnoIdEntoncesRetornaLosArchivosOrdenadosPorFecha() {

        Tema tema = new Tema();
        tema.setNombre("Programación");
        sessionFactory.getCurrentSession().save(tema);
        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("nombreprofesor3");
        profesor.setApellido("apellidoprofesor3");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("nombrealumno3");
        alumno.setApellido("apellidoalumno3");
        sessionFactory.getCurrentSession().save(alumno);

        Archivo archivo1 = new Archivo();
        archivo1.setNombre("archivo1.pdf");
        archivo1.setTipoContenido("application/pdf");
        archivo1.setRutaAlmacenamiento("/uploads/archivo1.pdf");
        archivo1.setProfesor(profesor);
        archivo1.setAlumno(alumno);
        archivo1.setFechaSubida(LocalDateTime.now().minusDays(2));
        sessionFactory.getCurrentSession().save(archivo1);

        Archivo archivo2 = new Archivo();
        archivo2.setNombre("archivo2.pdf");
        archivo2.setTipoContenido("application/pdf");
        archivo2.setRutaAlmacenamiento("/uploads/archivo2.pdf");
        archivo2.setProfesor(profesor);
        archivo2.setAlumno(alumno);
        archivo2.setFechaSubida(LocalDateTime.now().minusDays(1));
        sessionFactory.getCurrentSession().save(archivo2);

        List<Archivo> archivos = repositorioArchivo.obtenerArchivosPorAlumno(alumno.getId());

        assertNotNull(archivos);
        assertThat(archivos.size(), equalTo(2));
        // Verificar que están ordenados por fecha descendente
        assertThat(archivos.get(0).getNombre(), equalTo("archivo2.pdf"));
        assertThat(archivos.get(1).getNombre(), equalTo("archivo1.pdf"));
    }

    @Test
    @Rollback
    public void dadoQueExistenArchivosDeUnProfesorCuandoObtengoPorProfesorIdEntoncesRetornaLosArchivosOrdenadosPorFecha() {

        Tema tema = new Tema();
        tema.setNombre("Diseño");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("nombreprofesor4");
        profesor.setApellido("apellidoprofesor4");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("nombrealumno4");
        alumno.setApellido("apellidoalumno4");
        sessionFactory.getCurrentSession().save(alumno);

        Archivo archivo1 = new Archivo();
        archivo1.setNombre("experimento1.pdf");
        archivo1.setTipoContenido("application/pdf");
        archivo1.setRutaAlmacenamiento("/uploads/experimento1.pdf");
        archivo1.setProfesor(profesor);
        archivo1.setAlumno(alumno);
        archivo1.setFechaSubida(LocalDateTime.now().minusDays(3));
        sessionFactory.getCurrentSession().save(archivo1);

        Archivo archivo2 = new Archivo();
        archivo2.setNombre("experimento2.pdf");
        archivo2.setTipoContenido("application/pdf");
        archivo2.setRutaAlmacenamiento("/uploads/experimento2.pdf");
        archivo2.setProfesor(profesor);
        archivo2.setAlumno(alumno);
        archivo2.setFechaSubida(LocalDateTime.now().minusDays(1));
        sessionFactory.getCurrentSession().save(archivo2);

        List<Archivo> archivos = repositorioArchivo.obtenerArchivosPorProfesor(profesor.getId());

        assertNotNull(archivos);
        assertThat(archivos.size(), equalTo(2));

        assertThat(archivos.get(0).getNombre(), equalTo("experimento2.pdf"));
        assertThat(archivos.get(1).getNombre(), equalTo("experimento1.pdf"));
    }

    @Test
    @Rollback
    public void dadoQueExistenArchivosCompartidosEntreProfesorYAlumnoCuandoObtengoPorAmbosIdsEntoncesRetornaLosArchivosCompartidos() {

        Tema tema = new Tema();
        tema.setNombre("Programación");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("nombreprofesor5");
        profesor.setApellido("apellidoprofesor5");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno1 = new Alumno();
        alumno1.setEmail("alumno1@test.com");
        alumno1.setPassword("123456");
        alumno1.setNombre("nombrealumno5");
        alumno1.setApellido("apellidoalumno5");
        sessionFactory.getCurrentSession().save(alumno1);

        Alumno alumno2 = new Alumno();
        alumno2.setEmail("alumno2@test.com");
        alumno2.setPassword("123456");
        alumno2.setNombre("nombrealumno6");
        alumno2.setApellido("apellidoalumno6");
        sessionFactory.getCurrentSession().save(alumno2);

        Archivo archivo1 = new Archivo();
        archivo1.setNombre("ensayo1.pdf");
        archivo1.setTipoContenido("application/pdf");
        archivo1.setRutaAlmacenamiento("/uploads/ensayo1.pdf");
        archivo1.setProfesor(profesor);
        archivo1.setAlumno(alumno1);
        archivo1.setFechaSubida(LocalDateTime.now().minusDays(2));
        sessionFactory.getCurrentSession().save(archivo1);

        Archivo archivo2 = new Archivo();
        archivo2.setNombre("ensayo2.pdf");
        archivo2.setTipoContenido("application/pdf");
        archivo2.setRutaAlmacenamiento("/uploads/ensayo2.pdf");
        archivo2.setProfesor(profesor);
        archivo2.setAlumno(alumno2);
        archivo2.setFechaSubida(LocalDateTime.now().minusDays(1));
        sessionFactory.getCurrentSession().save(archivo2);

        List<Archivo> archivos = repositorioArchivo.obtenerArchivosCompartidosEntreProfesorYAlumno(
                profesor.getId(), alumno1.getId());

        assertNotNull(archivos);
        assertThat(archivos.size(), equalTo(1));
        assertThat(archivos.get(0).getNombre(), equalTo("ensayo1.pdf"));
        assertThat(archivos.get(0).getProfesor(), equalTo(profesor));
        assertThat(archivos.get(0).getAlumno(), equalTo(alumno1));
    }

    @Test
    @Rollback
    public void dadoQueExistenArchivosCompartidosConAlumnoPorSusProfesoresCuandoObtengoPorAlumnoIdEntoncesRetornaLosArchivosCompartidos() {

        Tema tema1 = new Tema();
        tema1.setNombre("Programación");
        sessionFactory.getCurrentSession().save(tema1);

        Tema tema2 = new Tema();
        tema2.setNombre("Diseño");
        sessionFactory.getCurrentSession().save(tema2);
        Profesor profesor1 = new Profesor();
        profesor1.setEmail("profesor1@test.com");
        profesor1.setPassword("123456");
        profesor1.setNombre("nombreprofesor6");
        profesor1.setApellido("apellidoprofesor6");
        profesor1.setTema(tema1);
        sessionFactory.getCurrentSession().save(profesor1);

        Profesor profesor2 = new Profesor();
        profesor2.setEmail("profesor2@test.com");
        profesor2.setPassword("123456");
        profesor2.setNombre("nombreprofesor7");
        profesor2.setApellido("apellidoprofesor7");
        profesor2.setTema(tema2);
        sessionFactory.getCurrentSession().save(profesor2);
        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("nombrealumno7");
        alumno.setApellido("apellidoalumno7");
        sessionFactory.getCurrentSession().save(alumno);
        profesor1.agregarAlumno(alumno);
        profesor2.agregarAlumno(alumno);
        sessionFactory.getCurrentSession().update(profesor1);
        sessionFactory.getCurrentSession().update(profesor2);
        sessionFactory.getCurrentSession().update(alumno);
        Archivo archivo1 = new Archivo();
        archivo1.setNombre("calculo.pdf");
        archivo1.setTipoContenido("application/pdf");
        archivo1.setRutaAlmacenamiento("/uploads/calculo.pdf");
        archivo1.setProfesor(profesor1);
        archivo1.setAlumno(alumno);
        archivo1.setFechaSubida(LocalDateTime.now().minusDays(3));
        sessionFactory.getCurrentSession().save(archivo1);

        Archivo archivo2 = new Archivo();
        archivo2.setNombre("mecanica.pdf");
        archivo2.setTipoContenido("application/pdf");
        archivo2.setRutaAlmacenamiento("/uploads/mecanica.pdf");
        archivo2.setProfesor(profesor2);
        archivo2.setAlumno(alumno);
        archivo2.setFechaSubida(LocalDateTime.now().minusDays(1));
        sessionFactory.getCurrentSession().save(archivo2);

        List<Archivo> archivos = repositorioArchivo.obtenerArchivosCompartidosConAlumnoPorSusProfesores(alumno.getId());

        assertNotNull(archivos);
        assertThat(archivos.size(), equalTo(2));
        assertThat(archivos.get(0).getNombre(), equalTo("mecanica.pdf"));
        assertThat(archivos.get(1).getNombre(), equalTo("calculo.pdf"));
    }

    @Test
    @Rollback
    public void dadoQueExisteUnArchivoCuandoLoEliminoEntoncesYaNoPuedeSerEncontrado() {

        Tema tema = new Tema();
        tema.setNombre("Diseño");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("nombreprofesor8");
        profesor.setApellido("apellidoprofesor8");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("nombrealumno8");
        alumno.setApellido("apellidoalumno8");
        sessionFactory.getCurrentSession().save(alumno);

        Archivo archivo = new Archivo();
        archivo.setNombre("grammar.pdf");
        archivo.setTipoContenido("application/pdf");
        archivo.setRutaAlmacenamiento("/uploads/grammar.pdf");
        archivo.setProfesor(profesor);
        archivo.setAlumno(alumno);
        archivo.setFechaSubida(LocalDateTime.now());
        sessionFactory.getCurrentSession().save(archivo);
        sessionFactory.getCurrentSession().flush();

        Long archivoId = archivo.getId();

        repositorioArchivo.eliminarArchivo(archivo);
        sessionFactory.getCurrentSession().flush();

        Archivo archivoEliminado = repositorioArchivo.buscarArchivoPorId(archivoId);
        assertNull(archivoEliminado);
    }

    @Test
    @Rollback
    public void cuandoAlumnoNoTieneArchivosEntoncesRetornaListaVacia() {
        Alumno alumno = new Alumno();
        alumno.setEmail("sinarchivos@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("nombrealumno9");
        alumno.setApellido("apellidoalumno9");
        sessionFactory.getCurrentSession().save(alumno);

        List<Archivo> archivos = repositorioArchivo.obtenerArchivosPorAlumno(alumno.getId());

        assertNotNull(archivos);
        assertTrue(archivos.isEmpty());
    }

    @Test
    @Rollback
    public void cuandoProfesorNoTieneArchivosEntoncesRetornaListaVacia() {
        Tema tema = new Tema();
        tema.setNombre("Programación");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("sinarchivos@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("nombreprofesor9");
        profesor.setApellido("apellidoprofesor9");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        List<Archivo> archivos = repositorioArchivo.obtenerArchivosPorProfesor(profesor.getId());

        assertNotNull(archivos);
        assertTrue(archivos.isEmpty());
    }

    @Test
    @Rollback
    public void cuandoNoExistenArchivosCompartidosEntreProfesorYAlumnoEntoncesRetornaListaVacia() {
        // Crear entidades necesarias
        Tema tema = new Tema();
        tema.setNombre("Diseño");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("nombreprofesor10");
        profesor.setApellido("apellidoprofesor10");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("nombrealumno10");
        alumno.setApellido("apellidoalumno10");
        sessionFactory.getCurrentSession().save(alumno);

        List<Archivo> archivos = repositorioArchivo.obtenerArchivosCompartidosEntreProfesorYAlumno(
                profesor.getId(), alumno.getId());

        assertNotNull(archivos);
        assertTrue(archivos.isEmpty());
    }

    @Test
    @Rollback
    public void dadoQueExistenMultiplesArchivosCuandoObtengoPorAlumnoEntoncesRetornaEnOrdenCorrectoPorFecha() {

        Tema tema = new Tema();
        tema.setNombre("Programación");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("nombreprofesor11");
        profesor.setApellido("apellidoprofesor11");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("nombrealumno11");
        alumno.setApellido("apellidoalumno11");
        sessionFactory.getCurrentSession().save(alumno);
        LocalDateTime fechaBase = LocalDateTime.now().minusDays(5);

        Archivo archivo1 = new Archivo();
        archivo1.setNombre("antiguo.pdf");
        archivo1.setTipoContenido("application/pdf");
        archivo1.setRutaAlmacenamiento("/uploads/antiguo.pdf");
        archivo1.setProfesor(profesor);
        archivo1.setAlumno(alumno);
        archivo1.setFechaSubida(fechaBase);
        sessionFactory.getCurrentSession().save(archivo1);

        Archivo archivo2 = new Archivo();
        archivo2.setNombre("medio.pdf");
        archivo2.setTipoContenido("application/pdf");
        archivo2.setRutaAlmacenamiento("/uploads/medio.pdf");
        archivo2.setProfesor(profesor);
        archivo2.setAlumno(alumno);
        archivo2.setFechaSubida(fechaBase.plusDays(2));
        sessionFactory.getCurrentSession().save(archivo2);

        Archivo archivo3 = new Archivo();
        archivo3.setNombre("reciente.pdf");
        archivo3.setTipoContenido("application/pdf");
        archivo3.setRutaAlmacenamiento("/uploads/reciente.pdf");
        archivo3.setProfesor(profesor);
        archivo3.setAlumno(alumno);
        archivo3.setFechaSubida(fechaBase.plusDays(4));
        sessionFactory.getCurrentSession().save(archivo3);

        List<Archivo> archivos = repositorioArchivo.obtenerArchivosPorAlumno(alumno.getId());

        assertNotNull(archivos);
        assertThat(archivos.size(), equalTo(3));
        // Verificar orden descendente por fecha
        assertThat(archivos.get(0).getNombre(), equalTo("reciente.pdf"));
        assertThat(archivos.get(1).getNombre(), equalTo("medio.pdf"));
        assertThat(archivos.get(2).getNombre(), equalTo("antiguo.pdf"));
    }

    @Test
    @Rollback
    public void cuandoModificoUnArchivoEntoncesSeActualizaCorrectamente() {

        Tema tema = new Tema();
        tema.setNombre("Diseño");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("nombreprofesor12");
        profesor.setApellido("apellidoprofesor12");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("nombrealumno12");
        alumno.setApellido("apellidoalumno12");
        sessionFactory.getCurrentSession().save(alumno);

        Archivo archivo = new Archivo();
        archivo.setNombre("archivo.pdf");
        archivo.setTipoContenido("application/pdf");
        archivo.setRutaAlmacenamiento("/uploads/archivo.pdf");
        archivo.setProfesor(profesor);
        archivo.setAlumno(alumno);
        archivo.setFechaSubida(LocalDateTime.now());
        sessionFactory.getCurrentSession().save(archivo);


        archivo.setNombre("archivo_modificado.pdf");
        archivo.setRutaAlmacenamiento("/uploads/archivo_modificado.pdf");
        repositorioArchivo.guardarArchivo(archivo);

        Archivo archivoModificado = repositorioArchivo.buscarArchivoPorId(archivo.getId());

        assertNotNull(archivoModificado);
        assertThat(archivoModificado.getNombre(), equalTo("archivo_modificado.pdf"));
        assertThat(archivoModificado.getRutaAlmacenamiento(), equalTo("/uploads/archivo_modificado.pdf"));
    }
}