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
import java.time.LocalDateTime;
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

    @Test
    @Rollback
    public void dadoQueExistenProfesoresAsociadosAUnAlumnoCuandoObtengoProfesoresDeAlumnoEntoncesRetornaListaDeProfesores() {

        Tema tema1 = new Tema();
        tema1.setNombre("Matemáticas");
        sessionFactory.getCurrentSession().save(tema1);

        Tema tema2 = new Tema();
        tema2.setNombre("Física");
        sessionFactory.getCurrentSession().save(tema2);

        Profesor profesor1 = new Profesor();
        profesor1.setEmail("profesor1@test.com");
        profesor1.setPassword("123456");
        profesor1.setNombre("Carlos");
        profesor1.setApellido("Rodriguez");
        profesor1.setTema(tema1);
        sessionFactory.getCurrentSession().save(profesor1);

        Profesor profesor2 = new Profesor();
        profesor2.setEmail("profesor2@test.com");
        profesor2.setPassword("123456");
        profesor2.setNombre("Ana");
        profesor2.setApellido("Martinez");
        profesor2.setTema(tema2);
        sessionFactory.getCurrentSession().save(profesor2);

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("Juan");
        alumno.setApellido("Pérez");
        sessionFactory.getCurrentSession().save(alumno);

        profesor1.agregarAlumno(alumno);
        profesor2.agregarAlumno(alumno);
        sessionFactory.getCurrentSession().update(profesor1);
        sessionFactory.getCurrentSession().update(profesor2);
        sessionFactory.getCurrentSession().update(alumno);

        List<Profesor> profesores = repositorioUsuario.obtenerProfesoresDeAlumno(alumno.getId());

        assertNotNull(profesores);
        assertThat(profesores.size(), equalTo(2));
        assertThat(profesores, hasItems(profesor1, profesor2));
    }

    @Test
    @Rollback
    public void cuandoBuscoUsuarioPorIdExistenteEntoncesRetornaElUsuario() {

        Alumno usuario = new Alumno();
        usuario.setEmail("usuario@test.com");
        usuario.setPassword("123456");
        usuario.setNombre("TestUser");
        usuario.setApellido("TestApellido");
        sessionFactory.getCurrentSession().save(usuario);
        sessionFactory.getCurrentSession().flush();

        Usuario usuarioEncontrado = repositorioUsuario.buscarPorId(usuario.getId());

        assertNotNull(usuarioEncontrado);
        assertThat(usuarioEncontrado.getId(), equalTo(usuario.getId()));
        assertThat(usuarioEncontrado.getEmail(), equalTo("usuario@test.com"));
        assertThat(usuarioEncontrado.getNombre(), equalTo("TestUser"));
    }

    @Test
    @Rollback
    public void cuandoBuscoUsuarioPorIdInexistenteEntoncesRetornaNulo() {

        Usuario usuarioEncontrado = repositorioUsuario.buscarPorId(999L);
        assertNull(usuarioEncontrado);
    }

    @Test
    @Rollback
    public void dadoQueExisteProfesorConExperienciasCuandoBuscoProfesorConExperienciasEntoncesRetornaProfesorConExperienciasCargadas() {

        Tema tema = new Tema();
        tema.setNombre("Ingeniería");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("Dr. Pedro");
        profesor.setApellido("Gomez");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        ExperienciaEstudiantil experiencia1 = new ExperienciaEstudiantil();
        experiencia1.setProfesor(profesor);
        experiencia1.setInstitucion("Universidad Nacional");
        experiencia1.setDescripcion("Licenciatura en Ingeniería");
        experiencia1.setFecha("2015-2020");
        experiencia1.setTipoExperiencia("Pregrado");
        sessionFactory.getCurrentSession().save(experiencia1);

        ExperienciaEstudiantil experiencia2 = new ExperienciaEstudiantil();
        experiencia2.setProfesor(profesor);
        experiencia2.setInstitucion("Instituto Tecnológico");
        experiencia2.setDescripcion("Maestría en Ingeniería de Software");
        experiencia2.setFecha("2020-2022");
        experiencia2.setTipoExperiencia("Posgrado");
        sessionFactory.getCurrentSession().save(experiencia2);

        profesor.agregarExperiencia(experiencia1);
        profesor.agregarExperiencia(experiencia2);
        sessionFactory.getCurrentSession().update(profesor);
        sessionFactory.getCurrentSession().flush();

        Profesor profesorConExperiencias = ((RepositorioUsuarioImpl) repositorioUsuario)
                .buscarProfesorConExperiencias(profesor.getId());


        assertNotNull(profesorConExperiencias);
        assertThat(profesorConExperiencias.getId(), equalTo(profesor.getId()));
        assertThat(profesorConExperiencias.getNombre(), equalTo("Dr. Pedro"));
        assertNotNull(profesorConExperiencias.getExperiencias());
        assertThat(profesorConExperiencias.getExperiencias().size(), equalTo(2));


        boolean tieneExperiencia1 = profesorConExperiencias.getExperiencias().stream()
                .anyMatch(exp -> "Universidad Nacional".equals(exp.getInstitucion()));
        boolean tieneExperiencia2 = profesorConExperiencias.getExperiencias().stream()
                .anyMatch(exp -> "Instituto Tecnológico".equals(exp.getInstitucion()));

        assertTrue(tieneExperiencia1);
        assertTrue(tieneExperiencia2);
    }

    @Test
    @Rollback
    public void cuandoBuscoProfesorConExperienciasConIdInexistenteEntoncesRetornaNulo() {

        Profesor profesorConExperiencias = ((RepositorioUsuarioImpl) repositorioUsuario)
                .buscarProfesorConExperiencias(999L);

        assertNull(profesorConExperiencias);
    }

    @Test
    @Rollback
    public void cuandoObtengoProfesoresDeAlumnoSinProfesoresEntoncesRetornaListaVacia() {

        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("Juan");
        alumno.setApellido("Pérez");
        sessionFactory.getCurrentSession().save(alumno);
        List<Profesor> profesores = repositorioUsuario.obtenerProfesoresDeAlumno(alumno.getId());
        assertNotNull(profesores);
        assertTrue(profesores.isEmpty());
    }

    @Test
    @Rollback
    public void dadoQueExistenClasesPasadasYFuturasCuandoObtengClasesProfesorEntoncesRetornaSoloLasFuturas() {

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

        Clase clasePasada = new Clase();
        clasePasada.setProfesor(profesor);
        clasePasada.setFechaEspecifica(LocalDate.now().minusDays(1));
        clasePasada.setHora("10:00");
        clasePasada.setEstado(EstadoDisponibilidad.DISPONIBLE);
        sessionFactory.getCurrentSession().save(clasePasada);

        Clase claseFutura = new Clase();
        claseFutura.setProfesor(profesor);
        claseFutura.setFechaEspecifica(LocalDate.now().plusDays(1));
        claseFutura.setHora("14:00");
        claseFutura.setEstado(EstadoDisponibilidad.DISPONIBLE);
        sessionFactory.getCurrentSession().save(claseFutura);

        List<Clase> clases = repositorioUsuario.obtenerClasesProfesor(profesor.getId());

        assertNotNull(clases);
        assertThat(clases.size(), equalTo(1));
        assertThat(clases.get(0).getFechaEspecifica(), equalTo(LocalDate.now().plusDays(1)));
    }

    @Test
    @Rollback
    public void cuandoGuardoDosUsuariosConElMismoEmailEntoncesLanzaExcepcion() {
        Alumno alumno1 = new Alumno();
        alumno1.setEmail("duplicado@test.com");
        alumno1.setPassword("abc123");
        alumno1.setNombre("Juan");
        alumno1.setApellido("Perez");
        sessionFactory.getCurrentSession().save(alumno1);
        sessionFactory.getCurrentSession().flush();

        Alumno alumno2 = new Alumno();
        alumno2.setEmail("duplicado@test.com");
        alumno2.setPassword("xyz789");
        alumno2.setNombre("Pedro");
        alumno2.setApellido("Lopez");

        assertThrows(Exception.class, () -> {
            sessionFactory.getCurrentSession().save(alumno2);
            sessionFactory.getCurrentSession().flush();
        });
    }

    @Test
    @Rollback
    public void cuandoAlumnoNoTieneClasesNiProfesoresEntoncesAmbasListasSonVacias() {
        Alumno alumno = new Alumno();
        alumno.setEmail("nada@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("Libre");
        alumno.setApellido("Estudiante");
        sessionFactory.getCurrentSession().save(alumno);

        List<Profesor> profesores = repositorioUsuario.obtenerProfesoresDeAlumno(alumno.getId());
        List<Clase> clases = repositorioUsuario.obtenerClasesAlumno(alumno.getId());

        assertTrue(profesores.isEmpty());
        assertTrue(clases.isEmpty());
    }
    @Test
    @Rollback
    public void cuandoEliminoUsuarioEntoncesYaNoPuedeSerEncontrado() {
        Alumno alumno = new Alumno();
        alumno.setEmail("eliminar@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("Eliminar");
        alumno.setApellido("Prueba");
        sessionFactory.getCurrentSession().save(alumno);
        sessionFactory.getCurrentSession().flush();

        sessionFactory.getCurrentSession().delete(alumno);
        sessionFactory.getCurrentSession().flush();

        Usuario usuarioEncontrado = repositorioUsuario.buscar("eliminar@test.com");
        assertNull(usuarioEncontrado);
    }
    @Test
    @Rollback
    public void cuandoGuardoUsuarioConEmailNuloEntoncesLanzaExcepcion() {
        Alumno alumno = new Alumno();
        alumno.setEmail(null);
        alumno.setPassword("123456");
        alumno.setNombre("Nombre");
        alumno.setApellido("Apellido");

        assertThrows(Exception.class, () -> {
            repositorioUsuario.guardar(alumno);
            sessionFactory.getCurrentSession().flush();
        });
    }
    @Test
    @Rollback
    public void cuandoGuardoUsuarioConNombreNuloEntoncesLanzaExcepcion() {
        Alumno alumno = new Alumno();
        alumno.setEmail("test@ejemplo.com");
        alumno.setPassword("123456");
        alumno.setNombre(null);
        alumno.setApellido("Apellido");

        assertThrows(Exception.class, () -> {
            repositorioUsuario.guardar(alumno);
            sessionFactory.getCurrentSession().flush();
        });
    }
    @Test
    @Rollback
    public void cuandoModificoElEmailDelUsuarioEntoncesSeActualizaCorrectamente() {
        Alumno alumno = new Alumno();
        alumno.setEmail("original@correo.com");
        alumno.setPassword("123456");
        alumno.setNombre("Test");
        alumno.setApellido("Apellido");
        sessionFactory.getCurrentSession().save(alumno);

        alumno.setEmail("nuevo@correo.com");
        repositorioUsuario.modificar(alumno);
        sessionFactory.getCurrentSession().flush();

        Usuario usuarioModificado = repositorioUsuario.buscar("nuevo@correo.com");

        assertNotNull(usuarioModificado);
        assertEquals("nuevo@correo.com", usuarioModificado.getEmail());
    }
    @Test
    @Rollback
    public void dadoClasesPasadasYFuturasCuandoObtengoClasesProfesorEntoncesIgnoraLasPasadas() {
        Tema tema = new Tema();
        tema.setNombre("Arte");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("arte@prof.com");
        profesor.setPassword("123456");
        profesor.setNombre("ArteProf");
        profesor.setApellido("Pintor");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Clase clasePasada = new Clase();
        clasePasada.setProfesor(profesor);
        clasePasada.setFechaEspecifica(LocalDate.now().minusDays(10));
        clasePasada.setHora("08:00");
        clasePasada.setEstado(EstadoDisponibilidad.DISPONIBLE);
        sessionFactory.getCurrentSession().save(clasePasada);

        Clase claseFutura = new Clase();
        claseFutura.setProfesor(profesor);
        claseFutura.setFechaEspecifica(LocalDate.now().plusDays(3));
        claseFutura.setHora("10:00");
        claseFutura.setEstado(EstadoDisponibilidad.DISPONIBLE);
        sessionFactory.getCurrentSession().save(claseFutura);

        List<Clase> clases = repositorioUsuario.obtenerClasesProfesor(profesor.getId());

        assertNotNull(clases);
        assertEquals(1, clases.size());
        assertEquals(claseFutura.getFechaEspecifica(), clases.get(0).getFechaEspecifica());
    }

    @Test
    @Rollback
    public void cuandoBuscoUsuarioPorNombreExistenteEntoncesRetornaElUsuario() {
        Alumno usuario = new Alumno();
        usuario.setEmail("usuario@test.com");
        usuario.setPassword("123456");
        usuario.setNombre("NombreUnico");
        usuario.setApellido("ApellidoTest");
        sessionFactory.getCurrentSession().save(usuario);

        Usuario usuarioEncontrado = repositorioUsuario.buscarPorNombre("NombreUnico");

        assertNotNull(usuarioEncontrado);
        assertThat(usuarioEncontrado.getNombre(), equalTo("NombreUnico"));
        assertThat(usuarioEncontrado.getEmail(), equalTo("usuario@test.com"));
    }

    @Test
    @Rollback
    public void cuandoBuscoUsuarioPorNombreInexistenteEntoncesRetornaNulo() {
        Usuario usuarioEncontrado = repositorioUsuario.buscarPorNombre("NombreInexistente");

        assertNull(usuarioEncontrado);
    }

    @Test
    @Rollback
    public void cuandoBuscoAlumnoPorNombreExistenteEntoncesRetornaElAlumno() {
        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("AlumnoEspecifico");
        alumno.setApellido("ApellidoTest");
        sessionFactory.getCurrentSession().save(alumno);

        Alumno alumnoEncontrado = repositorioUsuario.buscarAlumnoPorNombre("AlumnoEspecifico");

        assertNotNull(alumnoEncontrado);
        assertThat(alumnoEncontrado.getNombre(), equalTo("AlumnoEspecifico"));
        assertThat(alumnoEncontrado.getEmail(), equalTo("alumno@test.com"));
    }

    @Test
    @Rollback
    public void cuandoBuscoAlumnoPorNombreInexistenteEntoncesRetornaNulo() {
        Alumno alumnoEncontrado = repositorioUsuario.buscarAlumnoPorNombre("AlumnoInexistente");

        assertNull(alumnoEncontrado);
    }

    @Test
    @Rollback
    public void cuandoBuscoProfesorPorNombreExistenteEntoncesRetornaElProfesor() {
        Tema tema = new Tema();
        tema.setNombre("Literatura");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("ProfesorEspecifico");
        profesor.setApellido("ApellidoTest");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Profesor profesorEncontrado = repositorioUsuario.buscarProfesorPorNombre("ProfesorEspecifico");

        assertNotNull(profesorEncontrado);
        assertThat(profesorEncontrado.getNombre(), equalTo("ProfesorEspecifico"));
        assertThat(profesorEncontrado.getEmail(), equalTo("profesor@test.com"));
    }

    @Test
    @Rollback
    public void cuandoBuscoProfesorPorNombreInexistenteEntoncesRetornaNulo() {
        Profesor profesorEncontrado = repositorioUsuario.buscarProfesorPorNombre("ProfesorInexistente");

        assertNull(profesorEncontrado);
    }

    @Test
    @Rollback
    public void dadoQueExisteProfesorConAlumnosCuandoBuscoProfesorConAlumnosEntoncesRetornaProfesorConAlumnosCargados() {
        Tema tema = new Tema();
        tema.setNombre("Química");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("ProfesorTest");
        profesor.setApellido("ApellidoTest");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno1 = new Alumno();
        alumno1.setEmail("alumno1@test.com");
        alumno1.setPassword("123456");
        alumno1.setNombre("Alumno1");
        alumno1.setApellido("Apellido1");
        sessionFactory.getCurrentSession().save(alumno1);

        Alumno alumno2 = new Alumno();
        alumno2.setEmail("alumno2@test.com");
        alumno2.setPassword("123456");
        alumno2.setNombre("Alumno2");
        alumno2.setApellido("Apellido2");
        sessionFactory.getCurrentSession().save(alumno2);

        profesor.agregarAlumno(alumno1);
        profesor.agregarAlumno(alumno2);
        sessionFactory.getCurrentSession().update(profesor);
        sessionFactory.getCurrentSession().flush();

        Profesor profesorConAlumnos = repositorioUsuario.buscarProfesorConAlumnos(profesor.getId());

        assertNotNull(profesorConAlumnos);
        assertThat(profesorConAlumnos.getId(), equalTo(profesor.getId()));
        assertThat(profesorConAlumnos.getNombre(), equalTo("ProfesorTest"));
        assertNotNull(profesorConAlumnos.getAlumnos());
        assertThat(profesorConAlumnos.getAlumnos().size(), equalTo(2));
    }

    @Test
    @Rollback
    public void cuandoBuscoProfesorConAlumnosConIdInexistenteEntoncesLanzaExcepcion() {
        assertThrows(Exception.class, () -> {
            repositorioUsuario.buscarProfesorConAlumnos(999L);
        });
    }

    @Test
    @Rollback
    public void dadoQueAlumnoPerteneceAProfesorCuandoVerificoPertenenciaEntoncesRetornaTrue() {
        Tema tema = new Tema();
        tema.setNombre("Biología");
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

        profesor.agregarAlumno(alumno);
        sessionFactory.getCurrentSession().update(profesor);
        sessionFactory.getCurrentSession().flush();

        boolean pertenece = repositorioUsuario.alumnoPertenece(alumno.getId(), profesor.getId());

        assertTrue(pertenece);
    }

    @Test
    @Rollback
    public void dadoQueAlumnoNoPerteneceAProfesorCuandoVerificoPertenenciaEntoncesRetornaFalse() {
        Tema tema = new Tema();
        tema.setNombre("Geografía");
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

        boolean pertenece = repositorioUsuario.alumnoPertenece(alumno.getId(), profesor.getId());

        assertFalse(pertenece);
    }

    @Test
    @Rollback
    public void cuandoVerificoPertenenciaConAlumnoInexistenteEntoncesRetornaFalse() {
        Tema tema = new Tema();
        tema.setNombre("Arte");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("ProfesorTest");
        profesor.setApellido("ApellidoTest");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        boolean pertenece = repositorioUsuario.alumnoPertenece(999L, profesor.getId());

        assertFalse(pertenece);
    }

    @Test
    @Rollback
    public void cuandoVerificoPertenenciaConProfesorInexistenteEntoncesRetornaFalse() {
        Alumno alumno = new Alumno();
        alumno.setEmail("alumno@test.com");
        alumno.setPassword("123456");
        alumno.setNombre("AlumnoTest");
        alumno.setApellido("ApellidoTest");
        sessionFactory.getCurrentSession().save(alumno);

        boolean pertenece = repositorioUsuario.alumnoPertenece(alumno.getId(), 999L);

        assertFalse(pertenece);
    }

    @Test
    @Rollback
    public void dadoQueExistenAlumnosDeProfesorCuandoObtengoAlumnosEntoncesRetornaListaDeAlumnos() {
        Tema tema = new Tema();
        tema.setNombre("Música");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("ProfesorTest");
        profesor.setApellido("ApellidoTest");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        Alumno alumno1 = new Alumno();
        alumno1.setEmail("alumno1@test.com");
        alumno1.setPassword("123456");
        alumno1.setNombre("Alumno1");
        alumno1.setApellido("Apellido1");
        sessionFactory.getCurrentSession().save(alumno1);

        Alumno alumno2 = new Alumno();
        alumno2.setEmail("alumno2@test.com");
        alumno2.setPassword("123456");
        alumno2.setNombre("Alumno2");
        alumno2.setApellido("Apellido2");
        sessionFactory.getCurrentSession().save(alumno2);

        profesor.agregarAlumno(alumno1);
        profesor.agregarAlumno(alumno2);
        sessionFactory.getCurrentSession().update(profesor);
        sessionFactory.getCurrentSession().flush();

        List<Alumno> alumnos = repositorioUsuario.obtenerAlumnosDeProfesor(profesor.getId());

        assertNotNull(alumnos);
        assertThat(alumnos.size(), equalTo(2));
        assertThat(alumnos, hasItems(alumno1, alumno2));
    }

    @Test
    @Rollback
    public void cuandoObtengoAlumnosDeProfesorSinAlumnosEntoncesRetornaListaVacia() {
        Tema tema = new Tema();
        tema.setNombre("Programación");
        sessionFactory.getCurrentSession().save(tema);

        Profesor profesor = new Profesor();
        profesor.setEmail("profesor@test.com");
        profesor.setPassword("123456");
        profesor.setNombre("ProfesorTest");
        profesor.setApellido("ApellidoTest");
        profesor.setTema(tema);
        sessionFactory.getCurrentSession().save(profesor);

        List<Alumno> alumnos = repositorioUsuario.obtenerAlumnosDeProfesor(profesor.getId());

        assertNotNull(alumnos);
        assertTrue(alumnos.isEmpty());
    }

    @Test
    @Rollback
    public void cuandoGuardoUsuarioEntoncesActualizaUltimaConexion() {
        Alumno usuario = new Alumno();
        usuario.setEmail("test@ejemplo.com");
        usuario.setPassword("123456");
        usuario.setNombre("TestUser");
        usuario.setApellido("TestApellido");
        sessionFactory.getCurrentSession().save(usuario);

        LocalDateTime fechaAntes = LocalDateTime.now().minusMinutes(1);
        repositorioUsuario.guardarUltimaConexion(usuario);

        Usuario usuarioActualizado = repositorioUsuario.buscarPorId(usuario.getId());

        assertNotNull(usuarioActualizado.getUltimaConexion());
        assertTrue(usuarioActualizado.getUltimaConexion().isAfter(fechaAntes));
    }

    @Test
    @Rollback
    public void cuandoGuardoUltimaConexionConUsuarioNuloEntoncesNoLanzaExcepcion() {
        assertDoesNotThrow(() -> {
            repositorioUsuario.guardarUltimaConexion(null);
        });
    }


}
