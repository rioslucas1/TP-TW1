package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.ClaseReservada;
import com.tallerwebi.dominio.entidades.EstadoDisponibilidad;
import com.tallerwebi.dominio.entidades.disponibilidadProfesor;
import com.tallerwebi.infraestructura.RepositorioReservaAlumnoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
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
    public void reservarClase(String emailAlumno, String emailProfesor, String diaSemana, String hora) {
        disponibilidadProfesor disponibilidad = repositorioReservaAlumno
                .buscarPorProfesorDiaHora(emailProfesor, diaSemana, hora);

        if (disponibilidad == null || !disponibilidad.isDisponible()) {
            throw new RuntimeException("El horario no está disponible para reserva");
        }
        
        LocalDate fechaClase = calcularProximaFecha(diaSemana);

        if (existeReservaEnFecha(emailProfesor, fechaClase, hora)) {
            throw new RuntimeException("Ya existe una reserva para esa fecha y hora");
        }
        
        ClaseReservada claseReservada = new ClaseReservada(
                emailProfesor, emailAlumno, diaSemana, hora, fechaClase
        );

        // 5. Guardar la reserva
        repositorioClaseReservada.guardar(claseReservada);
    }

    private LocalDate calcularProximaFecha(String diaSemana) {
        LocalDate hoy = LocalDate.now();
        DayOfWeek diaObjetivo = convertirStringADayOfWeek(diaSemana);
        LocalDate proximaSemana = hoy.plusWeeks(1);
        LocalDate fechaObjetivo = proximaSemana.with(diaObjetivo);
        return fechaObjetivo;
    }




}