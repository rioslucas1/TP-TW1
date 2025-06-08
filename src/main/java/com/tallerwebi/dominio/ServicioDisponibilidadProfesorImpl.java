package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.EstadoDisponibilidad;
import com.tallerwebi.dominio.entidades.disponibilidadProfesor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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
    public void toggleDisponibilidad(String emailProfesor, String diaSemana, String hora) {
        disponibilidadProfesor disponibilidadExistente = repositorioDisponibilidadProfesor
                .buscarPorProfesorDiaHora(emailProfesor, diaSemana, hora);

        if (disponibilidadExistente != null) {
            if (disponibilidadExistente.isDisponible()) {
                disponibilidadExistente.marcarComoOcupado();
            } else if (disponibilidadExistente.isOcupado()) {
                disponibilidadExistente.marcarComoDisponible();
            }
            repositorioDisponibilidadProfesor.guardar(disponibilidadExistente);
        } else {
            disponibilidadProfesor nuevaDisponibilidad = new disponibilidadProfesor(
                    emailProfesor, diaSemana, hora, EstadoDisponibilidad.DISPONIBLE);
            repositorioDisponibilidadProfesor.guardar(nuevaDisponibilidad);
        }
    }

    @Override
    public void cambiarEstadoDisponibilidad(String emailProfesor, String diaSemana, String hora,
                                            EstadoDisponibilidad nuevoEstado) {
        disponibilidadProfesor disponibilidadExistente = repositorioDisponibilidadProfesor
                .buscarPorProfesorDiaHora(emailProfesor, diaSemana, hora);
        if (disponibilidadExistente != null) {
            disponibilidadExistente.setEstado(nuevoEstado);
            repositorioDisponibilidadProfesor.guardar(disponibilidadExistente);
        } else {
            disponibilidadProfesor nuevaDisponibilidad = new disponibilidadProfesor(
                    emailProfesor, diaSemana, hora, nuevoEstado);
            repositorioDisponibilidadProfesor.guardar(nuevaDisponibilidad);
        }
    }

    public void desagendarHorario(String emailProfesor, String diaSemana, String hora) {
        cambiarEstadoDisponibilidad(emailProfesor, diaSemana, hora,
               EstadoDisponibilidad.DISPONIBLE);
    }

    @Override
    public void marcarComoReservado(String emailProfesor, String diaSemana, String hora) {
        cambiarEstadoDisponibilidad(emailProfesor, diaSemana, hora,
                EstadoDisponibilidad.RESERVADO);
    }

    @Override
    public List<disponibilidadProfesor> obtenerDisponibilidadProfesor(String emailProfesor) {
        return repositorioDisponibilidadProfesor.buscarPorProfesor(emailProfesor);
    }

    @Override
    public disponibilidadProfesor obtenerDisponibilidadEspecifica(String emailProfesor, String diaSemana, String hora) {
        return repositorioDisponibilidadProfesor.buscarPorProfesorDiaHora(emailProfesor, diaSemana, hora);
    }

    @Override
    public void reservarHorario(String emailProfesor, String diaSemana, String hora) {
        cambiarEstadoDisponibilidad(emailProfesor, diaSemana, hora, EstadoDisponibilidad.RESERVADO);
    }

}