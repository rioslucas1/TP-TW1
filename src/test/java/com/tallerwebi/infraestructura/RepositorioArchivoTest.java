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


    @Test
    @Rollback
    public void dadoQueExistenArchivosRecientesCuandoSoyAlumnoEntoncesObtengoLosArchivosLimitados() {

        Tema tema = new Tema();
        tema.setNombre("Matemáticas");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("Juan");
        profesor.setApellido("Pérez");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("María");
        alumno.setApellido("García");
        sessionFactory.getCurrentSession().save(alumno);

        profesor.agregarAlumno(alumno);
        sessionFactory.getCurrentSession().update(profesor);
        sessionFactory.getCurrentSession().update(alumno);

        LocalDateTime fechaBase = LocalDateTime.now().minusDays(10);

        for (int i = 0; i < 5; i++) {
            Archivo archivo = new Archivo();
            archivo.setNombre("archivo" + i + ".pdf");
            archivo.setTipoContenido("application/pdf");
            archivo.setRutaAlmacenamiento("/uploads/archivo" + i + ".pdf");
            archivo.setProfesor(profesor);
            archivo.setAlumno(alumno);
            archivo.setFechaSubida(fechaBase.plusDays(i));
            sessionFactory.getCurrentSession().save(archivo);
        }
        List<Archivo> archivosRecientes = repositorioArchivo.obtenerArchivosRecientes(alumno.getId(), "ALUMNO", 3);

        assertNotNull(archivosRecientes);
        assertThat(archivosRecientes.size(), equalTo(3));
        assertThat(archivosRecientes.get(0).getNombre(), equalTo("archivo4.pdf"));
        assertThat(archivosRecientes.get(1).getNombre(), equalTo("archivo3.pdf"));
        assertThat(archivosRecientes.get(2).getNombre(), equalTo("archivo2.pdf"));
    }

    @Test
    @Rollback
    public void dadoQueExistenArchivosRecientesCuandoSoyProfesorEntoncesObtengoLosArchivosLimitados() {
        Tema tema = new Tema();
        tema.setNombre("Física");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("Carlos");
        profesor.setApellido("López");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("Ana");
        alumno.setApellido("Martínez");
        sessionFactory.getCurrentSession().save(alumno);
        LocalDateTime fechaBase = LocalDateTime.now().minusDays(7);

        for (int i = 0; i < 4; i++) {
            Archivo archivo = new Archivo();
            archivo.setNombre("tarea" + i + ".docx");
            archivo.setTipoContenido("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            archivo.setRutaAlmacenamiento("/uploads/tarea" + i + ".docx");
            archivo.setProfesor(profesor);
            archivo.setAlumno(alumno);
            archivo.setFechaSubida(fechaBase.plusDays(i));
            sessionFactory.getCurrentSession().save(archivo);
        }

        List<Archivo> archivosRecientes = repositorioArchivo.obtenerArchivosRecientes(profesor.getId(), "PROFESOR", 2);

        assertNotNull(archivosRecientes);
        assertThat(archivosRecientes.size(), equalTo(2));
        assertThat(archivosRecientes.get(0).getNombre(), equalTo("tarea3.docx"));
        assertThat(archivosRecientes.get(1).getNombre(), equalTo("tarea2.docx"));
    }

    @Test
    @Rollback
    public void cuandoAlumnoNoTieneArchivosRecientesEntoncesRetornaListaVacia() {
        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("Pedro");
        alumno.setApellido("Rodríguez");
        sessionFactory.getCurrentSession().save(alumno);

        List<Archivo> archivosRecientes = repositorioArchivo.obtenerArchivosRecientes(alumno.getId(), "ALUMNO", 5);

        assertNotNull(archivosRecientes);
        assertTrue(archivosRecientes.isEmpty());
    }

    @Test
    @Rollback
    public void cuandoProfesorNoTieneArchivosRecientesEntoncesRetornaListaVacia() {
        Tema tema = new Tema();
        tema.setNombre("Química");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("Luis");
        profesor.setApellido("González");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        List<Archivo> archivosRecientes = repositorioArchivo.obtenerArchivosRecientes(profesor.getId(), "PROFESOR", 3);

        assertNotNull(archivosRecientes);
        assertTrue(archivosRecientes.isEmpty());
    }

    @Test
    @Rollback
    public void cuandoSeEspecificaRolInvalidoEntoncesLanzaExcepcion() {
        Tema tema = new Tema();
        tema.setNombre("Historia");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("Roberto");
        profesor.setApellido("Fernández");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            repositorioArchivo.obtenerArchivosRecientes(profesor.getId(), "ADMINISTRADOR", 5);
        });

        assertThat(exception.getMessage(), containsString("Tipo de usuario no válido"));
    }

    @Test
    @Rollback
    public void dadoQueExistenMuchosArchivosCuandoSolicitoLimiteEspecificoEntoncesRetornaExactamenteLaCantidadSolicitada() {
        Tema tema = new Tema();
        tema.setNombre("Biología");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("Elena");
        profesor.setApellido("Vásquez");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("Diego");
        alumno.setApellido("Morales");
        sessionFactory.getCurrentSession().save(alumno);

        profesor.agregarAlumno(alumno);
        sessionFactory.getCurrentSession().update(profesor);
        sessionFactory.getCurrentSession().update(alumno);

        LocalDateTime fechaBase = LocalDateTime.now().minusDays(15);

        for (int i = 0; i < 10; i++) {
            Archivo archivo = new Archivo();
            archivo.setNombre("estudio" + i + ".pdf");
            archivo.setTipoContenido("application/pdf");
            archivo.setRutaAlmacenamiento("/uploads/estudio" + i + ".pdf");
            archivo.setProfesor(profesor);
            archivo.setAlumno(alumno);
            archivo.setFechaSubida(fechaBase.plusDays(i));
            sessionFactory.getCurrentSession().save(archivo);
        }


        List<Archivo> archivosRecientes = repositorioArchivo.obtenerArchivosRecientes(alumno.getId(), "ALUMNO", 1);

        assertNotNull(archivosRecientes);
        assertThat(archivosRecientes.size(), equalTo(1));
        assertThat(archivosRecientes.get(0).getNombre(), equalTo("estudio9.pdf"));
    }

    @Test
    @Rollback
    public void dadoQueAlumnoTieneArchivosCompartidosPorVariosProfesoresCuandoObtengoArchivosRecientesEntoncesRetornaArchivosDeTodosLosProfesores() {

        Tema tema1 = new Tema();
        tema1.setNombre("Matemáticas");
        sessionFactory.getCurrentSession().save(tema1);

        Tema tema2 = new Tema();
        tema2.setNombre("Física");
        sessionFactory.getCurrentSession().save(tema2);

        Profesor profesor1 = new Profesor();
        profesor1.setEmail("profesor1@test.com");
        profesor1.setPassword("123456");
        profesor1.setNombre("María");
        profesor1.setApellido("Rodríguez");
        profesor1.setTema(tema1);
        sessionFactory.getCurrentSession().save(profesor1);

        Profesor profesor2 = new Profesor();
        profesor2.setEmail("profesor2@test.com");
        profesor2.setPassword("123456");
        profesor2.setNombre("Carlos");
        profesor2.setApellido("López");
        profesor2.setTema(tema2);
        sessionFactory.getCurrentSession().save(profesor2);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("José");
        alumno.setApellido("Pérez");
        sessionFactory.getCurrentSession().save(alumno);


        profesor1.agregarAlumno(alumno);
        profesor2.agregarAlumno(alumno);
        sessionFactory.getCurrentSession().update(profesor1);
        sessionFactory.getCurrentSession().update(profesor2);
        sessionFactory.getCurrentSession().update(alumno);

        LocalDateTime fechaBase = LocalDateTime.now().minusDays(5);

        Archivo archivo1 = new Archivo();
        archivo1.setNombre("matematicas.pdf");
        archivo1.setTipoContenido("application/pdf");
        archivo1.setRutaAlmacenamiento("/uploads/matematicas.pdf");
        archivo1.setProfesor(profesor1);
        archivo1.setAlumno(alumno);
        archivo1.setFechaSubida(fechaBase.plusDays(1));
        sessionFactory.getCurrentSession().save(archivo1);

        Archivo archivo2 = new Archivo();
        archivo2.setNombre("fisica.pdf");
        archivo2.setTipoContenido("application/pdf");
        archivo2.setRutaAlmacenamiento("/uploads/fisica.pdf");
        archivo2.setProfesor(profesor2);
        archivo2.setAlumno(alumno);
        archivo2.setFechaSubida(fechaBase.plusDays(2));
        sessionFactory.getCurrentSession().save(archivo2);

        Archivo archivo3 = new Archivo();
        archivo3.setNombre("algebra.pdf");
        archivo3.setTipoContenido("application/pdf");
        archivo3.setRutaAlmacenamiento("/uploads/algebra.pdf");
        archivo3.setProfesor(profesor1);
        archivo3.setAlumno(alumno);
        archivo3.setFechaSubida(fechaBase.plusDays(3));
        sessionFactory.getCurrentSession().save(archivo3);

        List<Archivo> archivosRecientes = repositorioArchivo.obtenerArchivosRecientes(alumno.getId(), "ALUMNO", 5);

        assertNotNull(archivosRecientes);
        assertThat(archivosRecientes.size(), equalTo(3));

        assertThat(archivosRecientes.get(0).getNombre(), equalTo("algebra.pdf"));
        assertThat(archivosRecientes.get(1).getNombre(), equalTo("fisica.pdf"));
        assertThat(archivosRecientes.get(2).getNombre(), equalTo("matematicas.pdf"));

        boolean tieneArchivosProfesor1 = archivosRecientes.stream()
                .anyMatch(a -> a.getProfesor().getId().equals(profesor1.getId()));
        boolean tieneArchivosProfesor2 = archivosRecientes.stream()
                .anyMatch(a -> a.getProfesor().getId().equals(profesor2.getId()));

        assertTrue(tieneArchivosProfesor1);
        assertTrue(tieneArchivosProfesor2);
    }

}