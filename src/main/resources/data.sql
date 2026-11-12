-- Datos iniciales de prueba (estudiantes y libros ya registrados)
INSERT INTO estudiantes (nombre, correo) VALUES ('Ana Gómez', 'ana.gomez@campusucc.edu.co');
INSERT INTO estudiantes (nombre, correo) VALUES ('Luis Pérez', 'luis.perez@campusucc.edu.co');
INSERT INTO estudiantes (nombre, correo) VALUES ('María Ruiz', 'maria.ruiz@campusucc.edu.co');

INSERT INTO libros (titulo, disponible) VALUES ('Clean Code', TRUE);
INSERT INTO libros (titulo, disponible) VALUES ('Design Patterns', TRUE);
INSERT INTO libros (titulo, disponible) VALUES ('Refactoring', FALSE);
INSERT INTO libros (titulo, disponible) VALUES ('Domain-Driven Design', TRUE);

-- El libro 3 ya tiene un préstamo activo (sirve para probar el 409 Conflict)
INSERT INTO prestamos (fecha_prestamo, fecha_devolucion, estudiante_id, libro_id) VALUES (DATE '2026-11-01', NULL, 2, 3);
-- Préstamo histórico ya devuelto del libro 1 (R5: varios históricos, uno activo)
INSERT INTO prestamos (fecha_prestamo, fecha_devolucion, estudiante_id, libro_id) VALUES (DATE '2026-10-01', DATE '2026-10-15', 1, 1);
