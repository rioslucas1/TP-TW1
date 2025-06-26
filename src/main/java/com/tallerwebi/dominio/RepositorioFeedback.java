package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.FeedbackProfesor;

import java.util.List;

public interface RepositorioFeedback {
    List<FeedbackProfesor> obtenerPorProfesorId(Long profesorId);
    FeedbackProfesor guardar(FeedbackProfesor feedback);
    void eliminar(Long feedbackId);
    FeedbackProfesor buscarPorId(Long id);
    List<FeedbackProfesor> obtenerTodos();
    boolean existeFeedbackDeAlumnoParaProfesor(Long alumnoId, Long profesorId);
    FeedbackProfesor buscarPorAlumnoYProfesor(Long alumnoId, Long profesorId);
}
