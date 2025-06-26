package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioFeedback;
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
public class RepositorioFeedbackTest {

    @Autowired
    private SessionFactory sessionFactory;
    private RepositorioFeedback repositorioFeedback;

    @BeforeEach
    public void init() {
        this.repositorioFeedback = new RepositorioFeedbackImpl(this.sessionFactory);
    }

    @Test
    @Rollback
    public void cuandoGuardoUnFeedbackConDatosCorrectosEntoncesSeGuardaEnLaBaseDeDatos() {
        Tema tema = new Tema();
        tema.setNombre("Matemáticas");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("nombre");
        profesor.setApellido("apellido");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("nombre2");
        alumno.setApellido("apellido");
        sessionFactory.getCurrentSession().save(alumno);

        FeedbackProfesor feedback = new FeedbackProfesor();
        feedback.setProfesor(profesor);
        feedback.setAlumno(alumno);
        feedback.setCalificacion(5);
        feedback.setComentario("Excelente profesor, muy clara la explicación");
        feedback.setFechaCreacion(LocalDateTime.now());

        FeedbackProfesor feedbackGuardado = repositorioFeedback.guardar(feedback);
        assertNotNull(feedbackGuardado);
        assertNotNull(feedbackGuardado.getId());
        assertThat(feedbackGuardado.getCalificacion(), equalTo(5));
        assertThat(feedbackGuardado.getComentario(), equalTo("Excelente profesor, muy clara la explicación"));
        assertThat(feedbackGuardado.getProfesor().getNombre(), equalTo("nombre"));
        assertThat(feedbackGuardado.getAlumno().getNombre(), equalTo("nombre2"));
    }

    @Test
    @Rollback
    public void dadoQueExisteFeedbackCuandoBuscoPorIdEntoncesRetornaElFeedback() {

        Tema tema = new Tema();
        tema.setNombre("Física");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("nombre");
        profesor.setApellido("apellido");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("nombre");
        alumno.setApellido("apellido");
        sessionFactory.getCurrentSession().save(alumno);

        FeedbackProfesor feedback = new FeedbackProfesor();
        feedback.setProfesor(profesor);
        feedback.setAlumno(alumno);
        feedback.setCalificacion(4);
        feedback.setComentario("Muy buena clase");
        sessionFactory.getCurrentSession().save(feedback);

        FeedbackProfesor feedbackEncontrado = repositorioFeedback.buscarPorId(feedback.getId());
        assertNotNull(feedbackEncontrado);
        assertThat(feedbackEncontrado.getId(), equalTo(feedback.getId()));
        assertThat(feedbackEncontrado.getCalificacion(), equalTo(4));
        assertThat(feedbackEncontrado.getComentario(), equalTo("Muy buena clase"));
    }

    @Test
    @Rollback
    public void cuandoBuscoFeedbackPorIdInexistenteEntoncesRetornaNulo() {

        FeedbackProfesor feedbackEncontrado = repositorioFeedback.buscarPorId(999L);


        assertNull(feedbackEncontrado);
    }

    @Test
    @Rollback
    public void dadoQueExistenFeedbacksDeProfesorCuandoObtengoListaPorProfesorIdEntoncesRetornaLosFeedbacks() {

        Tema tema = new Tema();
        tema.setNombre("Química");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("nombre");
        profesor.setApellido("apellido");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno1 = new Alumno();
        alumno1.setEmail("alumno1@test.com");
        alumno1.setPassword("123456");
        alumno1.setNombre("nombre");
        alumno1.setApellido("apellido");
        sessionFactory.getCurrentSession().save(alumno1);

        Alumno alumno2 = new Alumno();
        alumno2.setEmail("alumno2@test.com");
        alumno2.setPassword("123456");
        alumno2.setNombre("nombre2");
        alumno2.setApellido("apellido");
        sessionFactory.getCurrentSession().save(alumno2);

        FeedbackProfesor feedback1 = new FeedbackProfesor();
        feedback1.setProfesor(profesor);
        feedback1.setAlumno(alumno1);
        feedback1.setCalificacion(5);
        feedback1.setComentario("Excelente");
        sessionFactory.getCurrentSession().save(feedback1);

        FeedbackProfesor feedback2 = new FeedbackProfesor();
        feedback2.setProfesor(profesor);
        feedback2.setAlumno(alumno2);
        feedback2.setCalificacion(4);
        feedback2.setComentario("Muy bueno");
        sessionFactory.getCurrentSession().save(feedback2);


        List<FeedbackProfesor> feedbacks = repositorioFeedback.obtenerPorProfesorId(profesor.getId());
        assertNotNull(feedbacks);
        assertThat(feedbacks.size(), equalTo(2));
        assertThat(feedbacks, hasItems(feedback1, feedback2));
    }

    @Test
    @Rollback
    public void cuandoObtengoFeedbacksPorProfesorIdInexistenteEntoncesRetornaListaVacia() {

        List<FeedbackProfesor> feedbacks = repositorioFeedback.obtenerPorProfesorId(999L);

        assertNotNull(feedbacks);
        assertThat(feedbacks.size(), equalTo(0));
    }

    @Test
    @Rollback
    public void cuandoObtengoTodosLosFeedbacksEntoncesRetornaListaCompleta() {

        Tema tema = new Tema();
        tema.setNombre("Historia");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor1 = new Profesor();
        profesor1.setEmail("profesor1@test.com");
        profesor1.setPassword("123456");
        profesor1.setNombre("nombre");
        profesor1.setApellido("apellido");
        profesor1.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor1);

        Profesor profesor2 = new Profesor();
        profesor2.setEmail("profesor2@test.com");
        profesor2.setPassword("123456");
        profesor2.setNombre("nombre2");
        profesor2.setApellido("apellido2");
        profesor2.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor2);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("Carmen");
        alumno.setApellido("Ruiz");
        sessionFactory.getCurrentSession().save(alumno);

        FeedbackProfesor feedback1 = new FeedbackProfesor();
        feedback1.setProfesor(profesor1);
        feedback1.setAlumno(alumno);
        feedback1.setCalificacion(3);
        feedback1.setComentario("Regular");
        sessionFactory.getCurrentSession().save(feedback1);

        FeedbackProfesor feedback2 = new FeedbackProfesor();
        feedback2.setProfesor(profesor2);
        feedback2.setAlumno(alumno);
        feedback2.setCalificacion(5);
        feedback2.setComentario("Fantástico");
        sessionFactory.getCurrentSession().save(feedback2);

        List<FeedbackProfesor> todosLosFeedbacks = repositorioFeedback.obtenerTodos();

        assertNotNull(todosLosFeedbacks);
        assertThat(todosLosFeedbacks.size(), equalTo(2));
        assertThat(todosLosFeedbacks, hasItems(feedback1, feedback2));
    }

    @Test
    @Rollback
    public void dadoQueTengoUnFeedbackGuardadoCuandoLoEliminoEntoncesYaNoExiste() {
        Tema tema = new Tema();
        tema.setNombre("Literatura");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("nombre");
        profesor.setApellido("apellido");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("nombre");
        alumno.setApellido("apellido");
        sessionFactory.getCurrentSession().save(alumno);

        FeedbackProfesor feedback = new FeedbackProfesor();
        feedback.setProfesor(profesor);
        feedback.setAlumno(alumno);
        feedback.setCalificacion(2);
        feedback.setComentario("Necesita mejorar");
        sessionFactory.getCurrentSession().save(feedback);

        Long feedbackId = feedback.getId();

        FeedbackProfesor feedbackAntesDeEliminar = repositorioFeedback.buscarPorId(feedbackId);
        assertNotNull(feedbackAntesDeEliminar);

        repositorioFeedback.eliminar(feedbackId);

        FeedbackProfesor feedbackDespuesDeEliminar = repositorioFeedback.buscarPorId(feedbackId);
        assertNull(feedbackDespuesDeEliminar);
    }

    @Test
    @Rollback
    public void cuandoIntentoEliminarFeedbackInexistenteEntoncesNoOcurreError() {

        assertDoesNotThrow(() -> {
            repositorioFeedback.eliminar(999L);
        });
    }

    @Test
    @Rollback
    public void dadoQueTengoUnFeedbackCuandoLoModificoYGuardoEntoncesSeActualiza() {

        Tema tema = new Tema();
        tema.setNombre("Arte");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("nombre");
        profesor.setApellido("apellido");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("nombre");
        alumno.setApellido("apellido");
        sessionFactory.getCurrentSession().save(alumno);

        FeedbackProfesor feedback = new FeedbackProfesor();
        feedback.setProfesor(profesor);
        feedback.setAlumno(alumno);
        feedback.setCalificacion(3);
        feedback.setComentario("Comentario inicial");
        sessionFactory.getCurrentSession().save(feedback);

        feedback.setCalificacion(5);
        feedback.setComentario("Comentario actualizado - mucho mejor");
        FeedbackProfesor feedbackActualizado = repositorioFeedback.guardar(feedback);
        assertNotNull(feedbackActualizado);
        assertThat(feedbackActualizado.getCalificacion(), equalTo(5));
        assertThat(feedbackActualizado.getComentario(), equalTo("Comentario actualizado - mucho mejor"));

        FeedbackProfesor feedbackDesdeBD = repositorioFeedback.buscarPorId(feedback.getId());
        assertThat(feedbackDesdeBD.getCalificacion(), equalTo(5));
        assertThat(feedbackDesdeBD.getComentario(), equalTo("Comentario actualizado - mucho mejor"));
    }


    @Test
    @Rollback
    public void cuandoVerificoSiExisteFeedbackDeAlumnoParaProfesorYExisteEntoncesRetornaTrue() {

        Tema tema = new Tema();
        tema.setNombre("Biología");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("nombre");
        profesor.setApellido("apellido");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("nombre");
        alumno.setApellido("apellido");
        sessionFactory.getCurrentSession().save(alumno);

        FeedbackProfesor feedback = new FeedbackProfesor();
        feedback.setProfesor(profesor);
        feedback.setAlumno(alumno);
        feedback.setCalificacion(4);
        feedback.setComentario("Muy buena clase");
        sessionFactory.getCurrentSession().save(feedback);
        boolean existe = repositorioFeedback.existeFeedbackDeAlumnoParaProfesor(alumno.getId(), profesor.getId());
        assertTrue(existe);
    }

    @Test
    @Rollback
    public void cuandoVerificoSiExisteFeedbackDeAlumnoParaProfesorYNoExisteEntoncesRetornaFalse() {

        Tema tema = new Tema();
        tema.setNombre("Geografía");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("nombre");
        profesor.setApellido("apellido");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("nombre");
        alumno.setApellido("apellido");
        sessionFactory.getCurrentSession().save(alumno);

        boolean existe = repositorioFeedback.existeFeedbackDeAlumnoParaProfesor(alumno.getId(), profesor.getId());


        assertFalse(existe);
    }

    @Test
    @Rollback
    public void cuandoBuscoFeedbackPorAlumnoYProfesorYExisteEntoncesRetornaElFeedback() {

        Tema tema = new Tema();
        tema.setNombre("Inglés");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("nombre");
        profesor.setApellido("apellido");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("nombre");
        alumno.setApellido("apellido");
        sessionFactory.getCurrentSession().save(alumno);

        FeedbackProfesor feedback = new FeedbackProfesor();
        feedback.setProfesor(profesor);
        feedback.setAlumno(alumno);
        feedback.setCalificacion(5);
        feedback.setComentario("Excelente metodología");
        sessionFactory.getCurrentSession().save(feedback);


        FeedbackProfesor feedbackEncontrado = repositorioFeedback.buscarPorAlumnoYProfesor(alumno.getId(), profesor.getId());

        assertNotNull(feedbackEncontrado);
        assertThat(feedbackEncontrado.getId(), equalTo(feedback.getId()));
        assertThat(feedbackEncontrado.getCalificacion(), equalTo(5));
        assertThat(feedbackEncontrado.getComentario(), equalTo("Excelente metodología"));
        assertThat(feedbackEncontrado.getAlumno().getId(), equalTo(alumno.getId()));
        assertThat(feedbackEncontrado.getProfesor().getId(), equalTo(profesor.getId()));
    }

    @Test
    @Rollback
    public void cuandoBuscoFeedbackPorAlumnoYProfesorYNoExisteEntoncesRetornaNulo() {

        Tema tema = new Tema();
        tema.setNombre("Portugués");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("nombre");
        profesor.setApellido("apellido");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("nombre");
        alumno.setApellido("apellido");
        sessionFactory.getCurrentSession().save(alumno);
        FeedbackProfesor feedbackEncontrado = repositorioFeedback.buscarPorAlumnoYProfesor(alumno.getId(), profesor.getId());
        assertNull(feedbackEncontrado);
    }

    @Test
    @Rollback
    public void cuandoGuardoFeedbackConComentarioLargoEntoncesSeGuardaCorrectamente() {

        Tema tema = new Tema();
        tema.setNombre("Filosofía");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("nombre");
        profesor.setApellido("apellido");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("nombre");
        alumno.setApellido("apellido");
        sessionFactory.getCurrentSession().save(alumno);

        String comentarioLargo = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla pulvinar posuere vulputate. Etiam ut fermentum elit. Morbi dig.";

        FeedbackProfesor feedback = new FeedbackProfesor();
        feedback.setProfesor(profesor);
        feedback.setAlumno(alumno);
        feedback.setCalificacion(5);
        feedback.setComentario(comentarioLargo);
        feedback.setFechaCreacion(LocalDateTime.now());


        FeedbackProfesor feedbackGuardado = repositorioFeedback.guardar(feedback);

        assertNotNull(feedbackGuardado);
        assertNotNull(feedbackGuardado.getId());
        assertThat(feedbackGuardado.getComentario(), equalTo(comentarioLargo));
        assertThat(feedbackGuardado.getComentario().length(), greaterThan(120));
    }

    @Test
    @Rollback
    public void cuandoGuardoFeedbackConCaracteresEspecialesEntoncesSeGuardaCorrectamente() {

        Tema tema = new Tema();
        tema.setNombre("Español");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("nombre1");
        profesor.setApellido("apellido1");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("nombre2");
        alumno.setApellido("apellido2");
        sessionFactory.getCurrentSession().save(alumno);

        String comentarioConCaracteresEspeciales = "¡Excelente! El profesor enseña muy bien los acentos (á, é, í, ó, ú), " +
                "la ñ, y otros símbolos como: @#$%&*()[]{}=+¿?¡!«»''…–—•‰€£¥₹";

        FeedbackProfesor feedback = new FeedbackProfesor();
        feedback.setProfesor(profesor);
        feedback.setAlumno(alumno);
        feedback.setCalificacion(4);
        feedback.setComentario(comentarioConCaracteresEspeciales);
        feedback.setFechaCreacion(LocalDateTime.now());


        FeedbackProfesor feedbackGuardado = repositorioFeedback.guardar(feedback);


        assertNotNull(feedbackGuardado);
        assertNotNull(feedbackGuardado.getId());
        assertThat(feedbackGuardado.getComentario(), equalTo(comentarioConCaracteresEspeciales));
        assertThat(feedbackGuardado.getProfesor().getNombre(), equalTo("nombre1"));
        assertThat(feedbackGuardado.getProfesor().getApellido(), equalTo("apellido1"));
        assertThat(feedbackGuardado.getAlumno().getNombre(), equalTo("nombre2"));
        assertThat(feedbackGuardado.getAlumno().getApellido(), equalTo("apellido2"));
    }

    @Test
    @Rollback
    public void cuandoGuardoFeedbackConComentarioVacioEntoncesSeGuardaCorrectamente() {

        Tema tema = new Tema();
        tema.setNombre("Educación Física");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("nombre");
        profesor.setApellido("apellido");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("nombre");
        alumno.setApellido("apellido");
        sessionFactory.getCurrentSession().save(alumno);

        FeedbackProfesor feedback = new FeedbackProfesor();
        feedback.setProfesor(profesor);
        feedback.setAlumno(alumno);
        feedback.setCalificacion(3);
        feedback.setComentario("");
        feedback.setFechaCreacion(LocalDateTime.now());

        FeedbackProfesor feedbackGuardado = repositorioFeedback.guardar(feedback);


        assertNotNull(feedbackGuardado);
        assertNotNull(feedbackGuardado.getId());
        assertThat(feedbackGuardado.getComentario(), equalTo(""));
        assertThat(feedbackGuardado.getCalificacion(), equalTo(3));
    }

    @Test
    @Rollback
    public void cuandoGuardoFeedbackConComentarioNullEntoncesSeGuardaCorrectamente() {
        Tema tema = new Tema();
        tema.setNombre("Informática");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("nombre");
        profesor.setApellido("apellido");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("nombre");
        alumno.setApellido("apellido");
        sessionFactory.getCurrentSession().save(alumno);

        FeedbackProfesor feedback = new FeedbackProfesor();
        feedback.setProfesor(profesor);
        feedback.setAlumno(alumno);
        feedback.setCalificacion(4);
        feedback.setComentario(null);
        feedback.setFechaCreacion(LocalDateTime.now());
        FeedbackProfesor feedbackGuardado = repositorioFeedback.guardar(feedback);
        assertNotNull(feedbackGuardado);
        assertNotNull(feedbackGuardado.getId());
        assertNull(feedbackGuardado.getComentario());
        assertThat(feedbackGuardado.getCalificacion(), equalTo(4));
    }




}