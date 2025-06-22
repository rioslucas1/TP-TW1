package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.RepositorioUsuario;
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
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HibernateInfraestructuraTestConfig.class})
@Transactional
public class RepositorioUsuarioImplTest {


    @Autowired
    private SessionFactory sessionFactory;
    private RepositorioUsuario repositorioUsuario;



    @BeforeEach
    public void init() {
        this.repositorioUsuario = new RepositorioUsuarioImpl(this.sessionFactory);
    }

        @Test
        @Rollback
        public void cuandoGuardoUnAlumnoConDatosCorrectosEntoncesSeGuardaEnLaBaseDeDatos() {

            Alumno alumno = new Alumno();
            alumno.setEmail("alumno@test.com");
            alumno.setPassword("123456");
            alumno.setNombre("Maria");
            alumno.setApellido("Garcia");

            repositorioUsuario.guardar(alumno);
            String hql = "FROM Alumno WHERE email = :email";
            Query query = sessionFactory.getCurrentSession().createQuery(hql);
            query.setParameter("email", "alumno@test.com");
            Alumno alumnoGuardado = (Alumno) query.getSingleResult();

            assertNotNull(alumnoGuardado);
            assertThat(alumnoGuardado.getEmail(), equalTo("alumno@test.com"));
            assertThat(alumnoGuardado.getNombre(), equalTo("Maria"));
            assertThat(alumnoGuardado.getApellido(), equalTo("Garcia"));
        }

        @Test
        @Rollback
        public void cuandoGuardoUnProfesorConDatosCorrectosEntoncesSeGuardaEnLaBaseDeDatos() {
            Tema tema = new Tema();
            tema.setNombre("Matemáticas");
            sessionFactory.getCurrentSession().save(tema);

            Profesor profesor = new Profesor();
            profesor.setEmail("profesor@test.com");
            profesor.setPassword("123456");
            profesor.setNombre("Carlos");
            profesor.setApellido("Rodriguez");
            profesor.setTema(tema);

            repositorioUsuario.guardar(profesor);
            String hql = "FROM Profesor WHERE email = :email";
            Query query = sessionFactory.getCurrentSession().createQuery(hql);
            query.setParameter("email", "profesor@test.com");
            Profesor profesorGuardado = (Profesor) query.getSingleResult();

            assertNotNull(profesorGuardado);
            assertThat(profesorGuardado.getEmail(), equalTo("profesor@test.com"));
            assertThat(profesorGuardado.getNombre(), equalTo("Carlos"));
            assertThat(profesorGuardado.getTema().getNombre(), equalTo("Matemáticas"));
        }

        @Test
        @Rollback
        public void dadoQueExisteUnUsuarioEnLaBDCuandoBuscoPorEmailYPasswordMeDevuelveElUsuario() {

            Alumno usuario = new Alumno();
            usuario.setEmail("test@ejemplo.com");
            usuario.setPassword("contra123");
            usuario.setNombre("nombreTest");
            usuario.setApellido("ApellidoTest");
            sessionFactory.getCurrentSession().save(usuario);
            Usuario usuarioEncontrado = repositorioUsuario.buscarUsuario("test@ejemplo.com", "contra123");
            assertNotNull(usuarioEncontrado);
            assertThat(usuarioEncontrado.getEmail(), equalTo("test@ejemplo.com"));
            assertThat(usuarioEncontrado.getPassword(), equalTo("contra123"));
        }

        @Test
        @Rollback
        public void cuandoBuscoUsuarioConCredencialesIncorrectasEntoncesRetornaNulo() {

            Alumno usuario = new Alumno();
            usuario.setEmail("test@ejemplo.com");
            usuario.setPassword("contra123");
            usuario.setNombre("nombreTest");
            usuario.setApellido("ApellidoTest");
            sessionFactory.getCurrentSession().save(usuario);
            Usuario usuarioEncontrado = repositorioUsuario.buscarUsuario("test@example.com", "123");
            assertNull(usuarioEncontrado);
        }

        @Test
        @Rollback
        public void dadoQueExisteUnUsuarioEnLaBDCuandoBuscoPorEmailMeDevuelveElUsuario() {

            Alumno usuario = new Alumno();
            usuario.setEmail("test@ejemplo.com");
            usuario.setPassword("contra123");
            usuario.setNombre("nombreTest");
            usuario.setApellido("ApellidoTest");
            sessionFactory.getCurrentSession().save(usuario);
            Usuario usuarioEncontrado = repositorioUsuario.buscar("test@ejemplo.com");
            assertNotNull(usuarioEncontrado);
            assertThat(usuarioEncontrado.getEmail(), equalTo("test@ejemplo.com"));
            assertThat(usuarioEncontrado.getNombre(), equalTo("nombreTest"));
        }

        @Test
        @Rollback
        public void cuandoBuscoUsuarioPorEmailInexistenteEntoncesRetornaNulo() {

            Usuario usuarioEncontrado = repositorioUsuario.buscar("inexistente@example.com");
            assertNull(usuarioEncontrado);
        }

        @Test
        @Rollback
        public void dadoQueExisteUnUsuarioEnLaBDCuandoLoModificoEntoncesSeActualizaCorrectamente() {

            Alumno usuario = new Alumno();
            usuario.setEmail("modify@example.com");
            usuario.setPassword("oldpassword");
            usuario.setNombre("OldName");
            usuario.setApellido("OldLastName");
            sessionFactory.getCurrentSession().save(usuario);
            usuario.setNombre("NewName");
            usuario.setApellido("NewLastName");
            repositorioUsuario.modificar(usuario);
            String hql = "FROM Usuario WHERE email = :email";
            Query query = sessionFactory.getCurrentSession().createQuery(hql);
            query.setParameter("email", "modify@example.com");
            Usuario usuarioModificado = (Usuario) query.getSingleResult();
            assertThat(usuarioModificado.getNombre(), equalTo("NewName"));
            assertThat(usuarioModificado.getApellido(), equalTo("NewLastName"));
        }

        @Test
        @Rollback
        public void cuandoBuscoProfesoresPorTipoEntoncesRetornaListaDeProfesores() {

            Tema tema1 = new Tema();
            tema1.setNombre("Física");
            sessionFactory.getCurrentSession().save(tema1);

            Tema tema2 = new Tema();
            tema2.setNombre("Química");
            sessionFactory.getCurrentSession().save(tema2);

            Profesor profesor1 = new Profesor();
            profesor1.setEmail("profesor1@test.com");
            profesor1.setPassword("123456");
            profesor1.setNombre("Profesor1");
            profesor1.setApellido("ApellidoTest");
            profesor1.setTema(tema1);
            sessionFactory.getCurrentSession().save(profesor1);

            Profesor profesor2 = new Profesor();
            profesor2.setEmail("profesor2@test.com");
            profesor2.setPassword("123456");
            profesor2.setNombre("Profesor2");
            profesor2.setApellido("ApellidoTest2");
            profesor2.setTema(tema2);
            sessionFactory.getCurrentSession().save(profesor2);
            List<Usuario> profesores = repositorioUsuario.buscarPorTipo(Profesor.class);
            assertNotNull(profesores);
            assertThat(profesores.size(), equalTo(2));
            assertThat(profesores, hasItems(profesor1, profesor2));
        }

        @Test
        @Rollback
        public void cuandoBuscoAlumnosPorTipoEntoncesRetornaListaDeAlumnos() {

            Alumno alumno1 = new Alumno();
            alumno1.setEmail("alumno1@test.com");
            alumno1.setPassword("123456");
            alumno1.setNombre("Alumno1");
            alumno1.setApellido("ApellidoTest");
            sessionFactory.getCurrentSession().save(alumno1);

            Alumno alumno2 = new Alumno();
            alumno2.setEmail("alumno2@test.com");
            alumno2.setPassword("123456");
            alumno2.setNombre("Alumno2");
            alumno2.setApellido("ApellidoTest2");
            sessionFactory.getCurrentSession().save(alumno2);


            List<Usuario> alumnos = repositorioUsuario.buscarPorTipo(Alumno.class);

            assertNotNull(alumnos);
            assertThat(alumnos.size(), equalTo(2));
            assertThat(alumnos, hasItems(alumno1, alumno2));
        }

        @Test
        @Rollback
        public void dadoQueExistenDisponibilidadesDeProfesorCuandoObtengoPorProfesorIdEntoncesRetornaLasClasesFuturas() {

            Tema tema = new Tema();
            tema.setNombre("Historia");
            sessionFactory.getCurrentSession().save(tema);

            Profesor profesor = new Profesor();
            profesor.setEmail("profesor@test.com");
            profesor.setPassword("123456");
            profesor.setNombre("ProfesorTest");
            profesor.setApellido("ApellidoTest");
            profesor.setTema(tema);
            sessionFactory.getCurrentSession().save(profesor);

            Clase disponibilidad1 = new Clase();
            disponibilidad1.setProfesor(profesor);
            disponibilidad1.setFechaEspecifica(LocalDate.now().plusDays(1));
            disponibilidad1.setHora("10:00");
            disponibilidad1.setEstado(EstadoDisponibilidad.DISPONIBLE);
            sessionFactory.getCurrentSession().save(disponibilidad1);

            Clase disponibilidad2 = new Clase();
            disponibilidad2.setProfesor(profesor);
            disponibilidad2.setFechaEspecifica(LocalDate.now().plusDays(2));
            disponibilidad2.setHora("14:00");
            disponibilidad2.setEstado(EstadoDisponibilidad.DISPONIBLE);
            sessionFactory.getCurrentSession().save(disponibilidad2);

            List<Clase> clases = repositorioUsuario.obtenerClasesProfesor(profesor.getId());

            assertNotNull(clases);
            assertEquals(2, clases.size());
            assertEquals(LocalDate.now().plusDays(1), clases.get(0).getFechaEspecifica());
            assertEquals(LocalDate.now().plusDays(2), clases.get(1).getFechaEspecifica());
        }

        @Test
        @Rollback
        public void dadoQueExistenClasesReservadasParaAlumnoCuandoObtengoPorAlumnoIdEntoncesRetornaLasClasesReservadas() {

            Tema tema = new Tema();
            tema.setNombre("Literatura");
            sessionFactory.getCurrentSession().save(tema);

            Profesor profesor = new Profesor();
            profesor.setEmail("profesor@test.com");
            profesor.setPassword("123456");
            profesor.setNombre("ProfesorTest");
            profesor.setApellido("ApellidoTest");
            profesor.setTema(tema);
            sessionFactory.getCurrentSession().save(profesor);

            Alumno alumno = new Alumno();
            alumno.setEmail("alumno@test.com");
            alumno.setPassword("123456");
            alumno.setNombre("AlumnoTest");
            alumno.setApellido("ApellidoTest");
            sessionFactory.getCurrentSession().save(alumno);

            Clase disponibilidad = new Clase();
            disponibilidad.setProfesor(profesor);
            disponibilidad.setAlumno(alumno);
            disponibilidad.setFechaEspecifica(LocalDate.now().plusDays(1));
            disponibilidad.setHora("16:00");
            disponibilidad.setEstado(EstadoDisponibilidad.RESERVADO);
            sessionFactory.getCurrentSession().save(disponibilidad);
            List<Clase> clasesAlumno = repositorioUsuario.obtenerClasesAlumno(alumno.getId());
            assertNotNull(clasesAlumno);
            assertThat(clasesAlumno.size(), equalTo(1));
            assertThat(clasesAlumno.get(0).getAlumno(), equalTo(alumno));
            assertThat(clasesAlumno.get(0).getEstado(), equalTo(EstadoDisponibilidad.RESERVADO));
        }
}
