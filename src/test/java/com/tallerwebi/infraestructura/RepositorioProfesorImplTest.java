package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = HibernateTestConfig.class)
@Transactional
public class RepositorioProfesorImplTest {

    @Autowired
    private SessionFactory sessionFactory;

    private RepositorioProfesorImpl repositorioProfesor;

    @BeforeEach
    public void init() {
        repositorioProfesor = new RepositorioProfesorImpl(sessionFactory);
    }
}
    /*@Test
    public void deberiaBuscarTutoresFiltradosCorrectamente() {
        // Arrange: crear y guardar profesores de prueba
        Profesor profe1 = new Profesor();
        profe1.setNombre("Juan");
        profe1.setApellido("Gómez");
        profe1.setEmail("juan.gomez@email.com");
        profe1.setCategoria("Matemática");
        profe1.setModalidad("Online");
        profe1.setDuracion("1 hora");

        Profesor profe2 = new Profesor();
        profe2.setNombre("Pedro");
        profe2.setApellido("Pérez");
        profe1.setEmail("pedro.perez@email.com");
        profe2.setCategoria("Lengua");
        profe2.setModalidad("Presencial");
        profe2.setDuracion("2 horas");

        sessionFactory.getCurrentSession().save(profe1);
        sessionFactory.getCurrentSession().save(profe2);

        // Act: buscar filtrando por Matemática y Online
        List<Profesor> resultado = repositorioProfesor.buscarTutoresFiltrados("Matemática", "Online", "1 hora", "Juan");

        // Assert: sólo debería encontrar a Juan
        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getNombre().contains("Juan"));
    }
}*/
