package com.tallerwebi.dominio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Array;
import java.util.List;

@Service("servicioMapa")
@Transactional
public class ServicioMapaImpl implements ServicioMapa {

 @Autowired
 private RepositorioUsuario repositorioUsuario;

 @Override
 public List<Usuario> obtenerProfesores() {
  return repositorioUsuario.buscarPorRol("PROFESOR");
 }
}
