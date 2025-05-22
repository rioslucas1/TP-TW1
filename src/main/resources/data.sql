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