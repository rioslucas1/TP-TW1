package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.RepositorioUsuario;
import com.tallerwebi.dominio.entidades.Alumno;
import com.tallerwebi.dominio.entidades.Archivo;
import com.tallerwebi.dominio.entidades.Profesor;
import com.tallerwebi.dominio.entidades.Usuario;
import com.tallerwebi.dominio.servicios.ServicioArchivo;
import com.tallerwebi.dominio.servicios.ServicioLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/archivos")
public class ControladorArchivo {

    private ServicioArchivo servicioArchivo;
    private ServicioLogin servicioLogin;
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    public ControladorArchivo(ServicioArchivo servicioArchivo, ServicioLogin servicioLogin, RepositorioUsuario repositorioUsuario) {
        this.servicioArchivo = servicioArchivo;
        this.servicioLogin = servicioLogin;
        this.repositorioUsuario = repositorioUsuario;
    }

    @RequestMapping(path = "", method = RequestMethod.GET)
    public ModelAndView mostrarArchivos(HttpServletRequest request) {
        ModelMap modelo = new ModelMap();
        Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");

        if (usuario == null) {
            return new ModelAndView("redirect:/login");
        }

        modelo.put("nombreUsuario", usuario.getNombre());
        modelo.put("usuario", usuario);

        if (usuario instanceof Alumno) {
            Alumno alumno = (Alumno) usuario;
            List<Archivo> archivosDelAlumno = servicioArchivo.obtenerArchivosCompartidosConAlumnoPorSusProfesores(alumno.getId());
            modelo.put("archivos", archivosDelAlumno);
            modelo.put("rol", "alumno");
        } else if (usuario instanceof Profesor) {
            Profesor profesor = (Profesor) usuario;
            List<Archivo> archivosDelProfesor = servicioArchivo.obtenerArchivosPorProfesor(profesor.getId());
            modelo.put("archivos", archivosDelProfesor);
            modelo.put("rol", "profesor");
            modelo.put("alumnosDelProfesor", profesor.getAlumnos());
        } else {
            modelo.put("error", "Rol de usuario no reconocido.");
            return new ModelAndView("login", modelo);
        }

        return new ModelAndView("pantallaarchivos", modelo);
    }

    @RequestMapping(path = "/subir", method = RequestMethod.GET)
    public ModelAndView mostrarFormularioSubida(HttpServletRequest request) {
        ModelMap modelo = new ModelMap();
        Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");

        if (usuario == null || !(usuario instanceof Profesor)) {
            return new ModelAndView("redirect:/archivos");
        }

        Profesor profesorEnSession = (Profesor) usuario;
        Profesor profesor = (Profesor) repositorioUsuario.buscarProfesorConAlumnos(profesorEnSession.getId());

        if (profesor == null) {
            return new ModelAndView("redirect:/login");
        }

        modelo.put("profesorId", profesor.getId());
        modelo.put("alumnosDelProfesor", profesor.getAlumnos());
        modelo.put("nombreUsuario", usuario.getNombre());
        modelo.put("rol", "profesor");
        modelo.put("alumnosDisponibles", profesor.getAlumnos());

        return new ModelAndView("subir-archivo", modelo);
    }

    @RequestMapping(path = "/subir", method = RequestMethod.POST)
    public ModelAndView subirArchivo(@RequestParam("file") MultipartFile file,
                                     @RequestParam("profesorId") Long profesorId,
                                     @RequestParam("alumnoId") Long alumnoId,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");

        if (usuario == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para subir archivos.");
            return new ModelAndView("redirect:/login");
        }

        if (!(usuario instanceof Profesor)) {
            redirectAttributes.addFlashAttribute("error", "Solo los profesores pueden subir archivos.");
            return new ModelAndView("redirect:/archivos");
        }

        if (!usuario.getId().equals(profesorId)) {
            redirectAttributes.addFlashAttribute("error", "No tenes permiso para subir archivos como este profesor.");
            return new ModelAndView("redirect:/archivos");
        }

        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Debes seleccionar un archivo para subir.");
            return new ModelAndView("redirect:/archivos/subir");
        }

        try {
            Profesor profesor = repositorioUsuario.buscarProfesorConAlumnos(profesorId);
            Alumno alumno = (Alumno) repositorioUsuario.buscarPorId(alumnoId);

            if (profesor == null) {
                redirectAttributes.addFlashAttribute("error", "Profesor no encontrado.");
                return new ModelAndView("redirect:/archivos/subir");
            }

            if (alumno == null) {
                redirectAttributes.addFlashAttribute("error", "Alumno no encontrado.");
                return new ModelAndView("redirect:/archivos/subir");
            }

            if (!repositorioUsuario.alumnoPertenece(alumno.getId(), profesor.getId())) {
                redirectAttributes.addFlashAttribute("error", "El alumno seleccionado no está suscripto con este profesor.");
                return new ModelAndView("redirect:/archivos/subir");
            }
            servicioArchivo.subirArchivo(file, profesor.getId(), alumno.getId());
            redirectAttributes.addFlashAttribute("mensaje", "Archivo subido exitosamente.");

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir el archivo: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Error en los datos proporcionados: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error inesperado al subir el archivo: " + e.getMessage());
        }

        return new ModelAndView("redirect:/archivos");
    }

    @RequestMapping(path = "/ver/{archivoId}", method = RequestMethod.GET)
    public ResponseEntity<Resource> verArchivo(@PathVariable Long archivoId, HttpServletRequest request) {
        try {
            Archivo archivo = servicioArchivo.buscarArchivoPorId(archivoId);
            Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");

            if (archivo == null) {
                System.err.println("Archivo no encontrado con ID: " + archivoId);
                return ResponseEntity.notFound().build();
            }

            boolean canView = false;
            if (usuario instanceof Profesor && archivo.getProfesor().getId().equals(usuario.getId())) {
                canView = true;
            } else if (usuario instanceof Alumno && archivo.getAlumno().getId().equals(usuario.getId())) {
                canView = true;
            }

            if (!canView) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            Path filePath = Paths.get(archivo.getRutaAlmacenamiento()).normalize();
            if (!Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header("Content-Type", "text/plain")
                        .body(null);
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(archivo.getTipoContenido()))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + archivo.getNombre() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .header("Content-Type", "text/plain")
                        .body(null);
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @RequestMapping(path = "/descargar/{archivoId}", method = RequestMethod.GET)
    public ResponseEntity<Resource> descargarArchivo(@PathVariable Long archivoId, HttpServletRequest request) {
        try {
            Archivo archivo = servicioArchivo.buscarArchivoPorId(archivoId);
            Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");

            if (archivo == null) {
                return ResponseEntity.notFound().build();
            }
            boolean canDownload = false;
            if (usuario instanceof Profesor && archivo.getProfesor().getId().equals(usuario.getId())) {
                canDownload = true;
            } else if (usuario instanceof Alumno && archivo.getAlumno().getId().equals(usuario.getId())) {
                canDownload = true;
            }

            if (!canDownload) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            Path filePath = Paths.get(archivo.getRutaAlmacenamiento()).normalize();
            if (!Files.exists(filePath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(archivo.getTipoContenido()))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + archivo.getNombre() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @RequestMapping(path = "/eliminar/{archivoId}", method = RequestMethod.POST)
    public ModelAndView eliminarArchivo(@PathVariable Long archivoId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) request.getSession().getAttribute("USUARIO");

        if (usuario == null || !(usuario instanceof Profesor)) {
            redirectAttributes.addFlashAttribute("error", "No tenes permiso para eliminar archivos.");
            return new ModelAndView("redirect:/archivos");
        }

        Archivo archivo = servicioArchivo.buscarArchivoPorId(archivoId);
        if (archivo != null && !archivo.getProfesor().getId().equals(usuario.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tenes permiso para eliminar este archivo.");
            return new ModelAndView("redirect:/archivos");
        }

        try {
            servicioArchivo.eliminarArchivo(archivoId);
            redirectAttributes.addFlashAttribute("mensaje", "Archivo eliminado exitosamente.");
        } catch (Exception e) {
            System.err.println("Error al eliminar archivo: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el archivo: " + e.getMessage());
        }

        return new ModelAndView("redirect:/archivos");
    }
}