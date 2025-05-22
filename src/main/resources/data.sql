INSERT INTO Usuario(id, email, nombre, apellido, password, rol, activo) VALUES(null, 'test@unlam.edu.ar', 'testnombre', 'testapellido', 'test', 'ADMIN', true);
INSERT INTO Tema(id, nombre) VALUES (1, 'Programación');
INSERT INTO Tema(id, nombre) VALUES (2, 'Diseño');
INSERT INTO Tema(id, nombre) VALUES (3, 'Inglés');

INSERT INTO Usuario(nombre, apellido, email, password, rol, activo, tema_id)
VALUES ('Maria', 'García', 'ana@unlam.edu.ar', 'clave1', 'profesor', true, 1);
INSERT INTO Usuario(nombre, apellido, email, password, rol, activo, tema_id)
VALUES ('Carlos', 'Pérez', 'carlos@unlam.edu.ar', 'clave2', 'profesor', true, 2);
INSERT INTO Usuario(nombre, apellido, email, password, rol, activo, tema_id)
VALUES ('Lucía', 'Martínez', 'lucia@unlam.edu.ar', 'clave3', 'profesor', true, 3);
INSERT INTO Profesor(id, nombre, apellido, estatus, materia, latitud, longitud)
VALUES (1, 'Ana', 'Gómez', true, 'MATEMATICAS', -34.66959360601952, -58.56196214273482),
       (2, 'Luis', 'Pérez', true, 'FISICA', -34.66974769686015, -58.56316068241746);
