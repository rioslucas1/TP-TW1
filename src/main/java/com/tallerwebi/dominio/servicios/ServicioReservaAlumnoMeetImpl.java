/*package com.tallerwebi.dominio.servicios;
import com.tallerwebi.dominio.RepositorioReservaAlumno;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.disponibilidadProfesor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service("servicioReservaAlumnoConMeet")
@Transactional
public class ServicioReservaAlumnoMeetImpl implements ServicioReservaAlumno {

    private RepositorioReservaAlumno repositorioReservaAlumno;
    private ServicioCrearReunion servicioCrearReunion;

    @Autowired
    public ServicioReservaAlumnoMeetImpl(RepositorioReservaAlumno repositorioReservaAlumno,
                                            ServicioCrearReunion servicioCrearReunion) {
        this.repositorioReservaAlumno = repositorioReservaAlumno;
        this.servicioCrearReunion = servicioCrearReunion;
    }

    @Override
    public List<disponibilidadProfesor> obtenerDisponibilidadProfesor(String emailProfesor) {
        return repositorioReservaAlumno.buscarPorProfesor(emailProfesor);
    }

    @Override
    public void reservarClase(String emailProfesor, String diaSemana, String hora, Alumno alumno) {
        disponibilidadProfesor disponibilidadExistente = repositorioReservaAlumno
                .buscarPorProfesorDiaHora(emailProfesor, diaSemana, hora);

        if (disponibilidadExistente != null && disponibilidadExistente.isDisponible()) {

            disponibilidadExistente.marcarComoReservado();
            disponibilidadExistente.setAlumno(alumno);

            try {

                String enlaceMeet = servicioCrearReunion.crearReunionMeet(disponibilidadExistente);
                disponibilidadExistente.setEnlace_meet(enlaceMeet);

                repositorioReservaAlumno.guardar(disponibilidadExistente);

            } catch (Exception e) {

                disponibilidadExistente.marcarComoDisponible();
                disponibilidadExistente.setAlumno(null);
                throw e;
            }
        } else {
            throw new RuntimeException("El horario no está disponible para reservar");
        }
    }

    @Override
    public void reservarClasePorId(Long disponibilidadId, Alumno alumno) {
        disponibilidadProfesor disponibilidad = repositorioReservaAlumno.buscarPorId(disponibilidadId);

        if (disponibilidad != null && disponibilidad.isDisponible()) {

            disponibilidad.marcarComoReservado();
            disponibilidad.setAlumno(alumno);

            try {

                String enlaceMeet = servicioCrearReunion.crearReunionMeet(disponibilidad);
                disponibilidad.setEnlace_meet(enlaceMeet);

                repositorioReservaAlumno.guardar(disponibilidad);

            } catch (Exception e) {

                disponibilidad.marcarComoDisponible();
                disponibilidad.setAlumno(null);
                throw e;
            }
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

            @Override
            public disponibilidadProfesor obtenerDisponibilidadPorId(Long disponibilidadId) {
                return repositorioReservaAlumno.buscarPorId(disponibilidadId);
            }


}

 */