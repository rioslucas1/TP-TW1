package com.tallerwebi.dominio;

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

            repositorioDisponibilidadProfesor.eliminar(disponibilidadExistente);
        } else {
            disponibilidadProfesor nuevaDisponibilidad = new disponibilidadProfesor(emailProfesor, diaSemana, hora);
            repositorioDisponibilidadProfesor.guardar(nuevaDisponibilidad);
        }
    }

    @Override
    public List<disponibilidadProfesor> obtenerDisponibilidadProfesor(String emailProfesor) {
        return repositorioDisponibilidadProfesor.buscarPorProfesor(emailProfesor);
    }
}