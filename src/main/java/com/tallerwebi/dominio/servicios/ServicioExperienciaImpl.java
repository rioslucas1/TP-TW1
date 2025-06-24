package com.tallerwebi.dominio.servicios;


import com.tallerwebi.dominio.RepositorioExperiencia;
import com.tallerwebi.dominio.RepositorioFeedback;
import com.tallerwebi.dominio.entidades.ExperienciaEstudiantil;
import com.tallerwebi.dominio.entidades.FeedbackProfesor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service("servicioExperiencia")
@Transactional
public class ServicioExperienciaImpl implements ServicioExperiencia {

    private RepositorioExperiencia repositorioExperiencia;

    @Autowired
    public ServicioExperienciaImpl(RepositorioExperiencia repositorioExperiencia) {;
        this.repositorioExperiencia = repositorioExperiencia;
    }

    @Override
    public List<ExperienciaEstudiantil> obtenerExperienciasPorProfesor(Long profesorId) {
        return repositorioExperiencia.obtenerPorProfesorId(profesorId);
    }

    @Override
    public ExperienciaEstudiantil guardar(ExperienciaEstudiantil experiencia) {
        return repositorioExperiencia.guardar(experiencia);
    }

    @Override
    public void eliminar(Long experienciaId) {
        repositorioExperiencia.eliminar(experienciaId);
    }

    @Override
    public ExperienciaEstudiantil obtenerPorId(Long id) {
        return repositorioExperiencia.buscarPorId(id);
    }

    @Override
    public List<ExperienciaEstudiantil> obtenerTodas() {
        return repositorioExperiencia.obtenerTodas();
    }
}
