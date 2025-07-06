package com.tallerwebi.dominio.servicios;

import com.tallerwebi.dominio.RepositorioReservaAlumno;
import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Clase;
import com.tallerwebi.dominio.entidades.Usuario;
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
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    public ServicioReservaAlumnoImpl(RepositorioReservaAlumno repositorioReservaAlumno, RepositorioUsuario repositorioUsuario) {;
        this.repositorioReservaAlumno = repositorioReservaAlumno;
        this.repositorioUsuario = repositorioUsuario;
    }

    @Override
    public List<Clase> obtenerDisponibilidadProfesor(String emailProfesor) {
        return repositorioReservaAlumno.buscarPorProfesor(emailProfesor);
    }

    @Override
    public void reservarClase(String emailProfesor, String diaSemana, String hora, Alumno alumno) {
        Clase disponibilidadExistente = repositorioReservaAlumno
                .buscarPorProfesorDiaHora(emailProfesor, diaSemana, hora);

        if (disponibilidadExistente != null && disponibilidadExistente.isDisponible()) {
            disponibilidadExistente.marcarComoReservado();
            disponibilidadExistente.setAlumno(alumno);
            repositorioReservaAlumno.guardar(disponibilidadExistente);
        } else {
            throw new RuntimeException("El horario no está disponible para reservar");
        }
    }

        @Override
        public void reservarClasePorId(Long disponibilidadId, Alumno alumno) {
            Clase disponibilidad = repositorioReservaAlumno.buscarPorId(disponibilidadId);

            if (disponibilidad != null && disponibilidad.isDisponible()) {
                disponibilidad.marcarComoReservado();
                disponibilidad.setAlumno(alumno);
                repositorioReservaAlumno.guardar(disponibilidad);
            } else {
                throw new RuntimeException("El horario no está disponible para reservar");
            }
        }

        @Override
        public List<Clase> obtenerDisponibilidadProfesorPorSemana(
                String emailProfesor, LocalDate fechaInicioSemana) {

            List<Clase> disponibilidadesSemanales = new ArrayList<>();
            String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};

            for (int i = 0; i < dias.length; i++) {
                LocalDate fechaEspecifica = fechaInicioSemana.plusDays(i);
                String diaSemana = dias[i];
                List<Clase> disponibilidadesDia =
                        repositorioReservaAlumno.buscarPorProfesorDiaFecha(
                                emailProfesor, diaSemana, fechaEspecifica);

                disponibilidadesSemanales.addAll(disponibilidadesDia);
            }

            return disponibilidadesSemanales;
        }

    @Override
    public Clase obtenerDisponibilidadPorId(Long disponibilidadId) {
        return repositorioReservaAlumno.buscarPorId(disponibilidadId);
    }

    @Override
    public boolean estaSuscritoAProfesor(Long alumnoId, String emailProfesor) {
        Usuario usuario = repositorioUsuario.buscarPorId(alumnoId);
        if (!(usuario instanceof Alumno)) {
            return false;
        }
        Alumno alumno = (Alumno) usuario;

        return alumno.getProfesores().stream()
                .anyMatch(profesor -> profesor.getEmail().equals(emailProfesor));
}

    @Override
    public List<Clase> obtenerClasesPorProfesorYAlumno(String emailProfesor, String emailAlumno) {
        return repositorioReservaAlumno.buscarClasesPorProfesorYAlumno(emailProfesor, emailAlumno);
    }


    @Override
    public List<Clase> obtenerTodasLasClasesPorProfesor(Long profesorId) { //
        return repositorioReservaAlumno.obtenerTodasLasClasesPorProfesor(profesorId);
    }

    @Override
    public void actualizarClase(Clase clase) {
        if (clase != null && clase.getId() != null) {
            repositorioReservaAlumno.guardar(clase);
        } else {
            throw new IllegalArgumentException("Clase inválida para actualizar");
        }
    }


}




