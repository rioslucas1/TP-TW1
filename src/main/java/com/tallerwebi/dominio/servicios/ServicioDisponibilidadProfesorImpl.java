package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.RepositorioDisponibilidadProfesor;
import com.tallerwebi.dominio.entidades.EstadoDisponibilidad;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.disponibilidadProfesor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service("servicioDisponibilidadProfesor")
@Transactional
public class ServicioDisponibilidadProfesorImpl implements ServicioDisponibilidadProfesor {

    private RepositorioDisponibilidadProfesor repositorioDisponibilidadProfesor;

    @Autowired
    public ServicioDisponibilidadProfesorImpl(RepositorioDisponibilidadProfesor repositorioDisponibilidadProfesor) {
        this.repositorioDisponibilidadProfesor = repositorioDisponibilidadProfesor;
    }

    @Override
    public void toggleDisponibilidad(Profesor profesor, String diaSemana, String hora) {
        disponibilidadProfesor disponibilidadExistente = repositorioDisponibilidadProfesor
                .buscarPorProfesorDiaHora(profesor, diaSemana, hora);

        if (disponibilidadExistente != null) {
            if (disponibilidadExistente.isDisponible()) {
                disponibilidadExistente.marcarComoOcupado();
            } else if (disponibilidadExistente.isOcupado()) {
                disponibilidadExistente.marcarComoDisponible();
            }
            repositorioDisponibilidadProfesor.guardar(disponibilidadExistente);
        } else {
            disponibilidadProfesor nuevaDisponibilidad = new disponibilidadProfesor(
                    profesor, diaSemana, hora, EstadoDisponibilidad.DISPONIBLE);
            repositorioDisponibilidadProfesor.guardar(nuevaDisponibilidad);
        }
    }

    @Override
    public void cambiarEstadoDisponibilidad(Profesor profesor, String diaSemana, String hora,
                                            EstadoDisponibilidad nuevoEstado) {
        disponibilidadProfesor disponibilidadExistente = repositorioDisponibilidadProfesor
                .buscarPorProfesorDiaHora(profesor, diaSemana, hora);
        if (disponibilidadExistente != null) {
            disponibilidadExistente.setEstado(nuevoEstado);
            repositorioDisponibilidadProfesor.guardar(disponibilidadExistente);
        } else {
            disponibilidadProfesor nuevaDisponibilidad = new disponibilidadProfesor(
                    profesor, diaSemana, hora, nuevoEstado);
            repositorioDisponibilidadProfesor.guardar(nuevaDisponibilidad);
        }
    }

    @Override
    public void desagendarHorario(Profesor profesor, String diaSemana, String hora) {
        cambiarEstadoDisponibilidad(profesor, diaSemana, hora, EstadoDisponibilidad.DISPONIBLE);
    }

    @Override
    public void marcarComoReservado(Profesor profesor, String diaSemana, String hora) {
        cambiarEstadoDisponibilidad(profesor, diaSemana, hora, EstadoDisponibilidad.RESERVADO);
    }

    @Override
    public List<disponibilidadProfesor> obtenerDisponibilidadProfesor(Profesor profesor) {
        return repositorioDisponibilidadProfesor.buscarPorProfesor(profesor);
    }

    @Override
    public disponibilidadProfesor obtenerDisponibilidadEspecifica(Profesor profesor, String diaSemana, String hora) {
        return repositorioDisponibilidadProfesor.buscarPorProfesorDiaHora(profesor, diaSemana, hora);
    }

    @Override
    public void reservarHorario(Profesor profesor, String diaSemana, String hora) {
        cambiarEstadoDisponibilidad(profesor, diaSemana, hora, EstadoDisponibilidad.RESERVADO);
    }

    @Override
    public List<disponibilidadProfesor> obtenerDisponibilidadProfesorPorSemana(
            Profesor profesor, LocalDate fechaInicioSemana) {

        List<disponibilidadProfesor> disponibilidadesSemanales = new ArrayList<>();
        String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};

        for (int i = 0; i < dias.length; i++) {
            LocalDate fechaEspecifica = fechaInicioSemana.plusDays(i);
            String diaSemana = dias[i];
            List<disponibilidadProfesor> disponibilidadesDia =
                    repositorioDisponibilidadProfesor.buscarPorProfesorDiaFecha(
                            profesor, diaSemana, fechaEspecifica);
            disponibilidadesSemanales.addAll(disponibilidadesDia);
        }
        return disponibilidadesSemanales;
    }

    @Override
    public void toggleDisponibilidadConFecha(Profesor profesor, String diaSemana,
                                             String hora, LocalDate fechaEspecifica) {
        disponibilidadProfesor disponibilidadExistente = repositorioDisponibilidadProfesor
                .buscarPorProfesorDiaHoraFecha(profesor, diaSemana, hora, fechaEspecifica);

        if (disponibilidadExistente != null) {
            if (disponibilidadExistente.isDisponible()) {
                disponibilidadExistente.marcarComoOcupado();
            } else if (disponibilidadExistente.isOcupado()) {
                disponibilidadExistente.marcarComoDisponible();
            }
            repositorioDisponibilidadProfesor.guardar(disponibilidadExistente);
        } else {
            disponibilidadProfesor nuevaDisponibilidad = new disponibilidadProfesor(
                    profesor, diaSemana, hora, fechaEspecifica, EstadoDisponibilidad.DISPONIBLE);
            repositorioDisponibilidadProfesor.guardar(nuevaDisponibilidad);
        }
    }
}