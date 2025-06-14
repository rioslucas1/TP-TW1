package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.RepositorioReservaAlumno;
import com.tallerwebi.dominio.entidades.disponibilidadProfesor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service("servicioReservaAlumno")
@Transactional
public class ServicioReservaAlumnoImpl implements ServicioReservaAlumno {

    private RepositorioReservaAlumno repositorioReservaAlumno;

    @Autowired
    public ServicioReservaAlumnoImpl(RepositorioReservaAlumno repositorioReservaAlumno) {;
        this.repositorioReservaAlumno = repositorioReservaAlumno;
    }

    @Override
    public List<disponibilidadProfesor> obtenerDisponibilidadProfesor(String emailProfesor) {
        return repositorioReservaAlumno.buscarPorProfesor(emailProfesor);
    }

    @Override
    public void reservarClase(String emailProfesor, String diaSemana, String hora, String emailAlumno) {
        disponibilidadProfesor disponibilidadExistente = repositorioReservaAlumno
                .buscarPorProfesorDiaHora(emailProfesor, diaSemana, hora);

        if (disponibilidadExistente != null && disponibilidadExistente.isDisponible()) {
            disponibilidadExistente.marcarComoReservado();
            disponibilidadExistente.setMailAlumno(emailAlumno);
            repositorioReservaAlumno.guardar(disponibilidadExistente);
        } else {
            throw new RuntimeException("El horario no está disponible para reservar");
        }
    }

        @Override
        public void reservarClasePorId(Long disponibilidadId, String emailAlumno) {

            disponibilidadProfesor disponibilidad = repositorioReservaAlumno.buscarPorId(disponibilidadId);

            if (disponibilidad != null && disponibilidad.isDisponible()) {

            disponibilidad.marcarComoReservado();
            disponibilidad.setMailAlumno(emailAlumno);
            repositorioReservaAlumno.guardar(disponibilidad);
        } else {
                throw new RuntimeException("El horario no está disponible para reservar");
            }
        }
        @Override
        public List<disponibilidadProfesor> obtenerDisponibilidadProfesorPorSemana(
                String emailProfesor, LocalDate fechaInicioSemana) {

            List<disponibilidadProfesor> disponibilidadesSemanales = new ArrayList<>();

            String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};

            for (int i = 0; i < dias.length; i++) {
                LocalDate fechaEspecifica = fechaInicioSemana.plusDays(i);
                String diaSemana = dias[i];
                List<disponibilidadProfesor> disponibilidadesDia =
                        repositorioReservaAlumno.buscarPorProfesorDiaFecha(
                                emailProfesor, diaSemana, fechaEspecifica);

                disponibilidadesSemanales.addAll(disponibilidadesDia);
            }

            return disponibilidadesSemanales;
        }
        }




