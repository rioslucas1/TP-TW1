package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.entidades.FeedbackProfesor;

import java.util.List;

public interface ServicioFeedback {
    List<FeedbackProfesor> obtenerFeedbacksPorProfesor(Long profesorId);
    FeedbackProfesor guardar(FeedbackProfesor feedback);
    void eliminar(Long feedbackId);
    FeedbackProfesor obtenerPorId(Long id);
    Double calcularPromedioCalificacion(Long profesorId);
    Integer contarFeedbackPorProfesor(Long profesorId);
    List<FeedbackProfesor> obtenerTodos();
    boolean alumnoYaDejoFeedback(Long id, Long id1);
    FeedbackProfesor buscarFeedbackDeAlumnoParaProfesor(Long alumnoId, Long profesorId);
}
