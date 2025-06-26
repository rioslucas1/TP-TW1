package com.tallerwebi.dominio.servicios;


import com.tallerwebi.dominio.RepositorioFeedback;
import com.tallerwebi.dominio.RepositorioReservaAlumno;
import com.tallerwebi.dominio.entidades.FeedbackProfesor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service("servicioFeedback")
@Transactional
public class ServicioFeedbackImpl implements ServicioFeedback {


    private RepositorioFeedback repositorioFeedback;

    @Autowired
    public ServicioFeedbackImpl(RepositorioFeedback repositorioFeedback) {
        this.repositorioFeedback = repositorioFeedback;
    }

    @Override
    public List<FeedbackProfesor> obtenerFeedbacksPorProfesor(Long id) {
        return repositorioFeedback.obtenerPorProfesorId(id);
    }

    @Override
    public FeedbackProfesor guardar(FeedbackProfesor feedback) {
        return repositorioFeedback.guardar(feedback);
    }

    @Override
    public void eliminar(Long feedbackId) {
        repositorioFeedback.eliminar(feedbackId);
    }

    @Override
    public FeedbackProfesor obtenerPorId(Long id) {
        return repositorioFeedback.buscarPorId(id);
    }

    @Override
    public Double calcularPromedioCalificacion(Long profesorId) {
        List<FeedbackProfesor> feedbacks = repositorioFeedback.obtenerPorProfesorId(profesorId);

        if (feedbacks == null || feedbacks.isEmpty()) {
            return 0.0;
        }

        double suma = feedbacks.stream()
                .mapToInt(FeedbackProfesor::getCalificacion)
                .sum();

        return suma / feedbacks.size();
    }

    @Override
    public Integer contarFeedbackPorProfesor(Long profesorId) {
        List<FeedbackProfesor> feedbacks = repositorioFeedback.obtenerPorProfesorId(profesorId);
        return feedbacks != null ? feedbacks.size() : 0;
    }

    @Override
    public List<FeedbackProfesor> obtenerTodos() {
        return repositorioFeedback.obtenerTodos();
    }

    @Override
    public boolean alumnoYaDejoFeedback(Long alumnoId, Long profesorId) {
        return repositorioFeedback.existeFeedbackDeAlumnoParaProfesor(alumnoId, profesorId);
    }

    @Override
    public FeedbackProfesor buscarFeedbackDeAlumnoParaProfesor(Long alumnoId, Long profesorId) {
        return repositorioFeedback.buscarPorAlumnoYProfesor(alumnoId, profesorId);
    }


}
