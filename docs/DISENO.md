# Decisiones de diseño – SmartLibrary (Taller Bloque 7)

## 0. Lectura del modelo inicial (Actividad 1)

Diagrama anotado: `diagramas/visual-paradigm/export/Diagrama inicial anotado.png` (y en `SmartLibrary.vpp`).

1. **Datos y operaciones propios del dominio:** `Estudiante` (id, nombre, correo), `Libro` (id, titulo, disponible, `isDisponible()`, `marcarPrestado()`) y `Prestamo` (id, fechaPrestamo, fechaDevolucion [0..1], estudiante, libro). `GestorPrestamos` no es del dominio: es un contenedor en memoria con lógica de coordinación.
2. **¿Por qué Libro–Prestamo y Estudiante–Prestamo son asociaciones y no composiciones?** El libro y el estudiante existen antes del préstamo y siguen existiendo cuando se devuelve o se borra; no hay relación todo-parte ni dependencia de ciclo de vida. Además, un libro participa en muchos préstamos históricos (1 a 0..*), y en una composición una parte pertenece a un solo todo.
3. **Operaciones necesarias para consultar, validar y guardar:** buscar estudiante y libro por id, consultar si el libro está disponible y si tiene un préstamo activo (R1, R5), crear el préstamo (R2), marcar el libro como prestado (R3), guardar ambos cambios en una transacción (R4) y listar los préstamos.
4. **Responsabilidades que faltan:**
   - Para recibir la petición HTTP: leer el JSON, validarlo y devolver 201/404/409/400. Esto lo hace `PrestamoController` junto con `GlobalExceptionHandler`.
   - Para acceder a datos: buscar por id y persistir en H2. Esto lo hacen `EstudianteRepository`, `LibroRepository` y `PrestamoRepository`.
   - Para coordinar el caso de uso: lo hace `PrestamoService`, que reemplaza a `GestorPrestamos`.

**Observaciones adicionales:** `GestorPrestamos` tiene baja cohesión (mezcla datos y negocio). `registrarPrestamo(e, l)` recibe objetos ya cargados, así que no puede distinguir 404 de 409. `setDisponible(valor)` es público y permite saltarse R3, por eso es mejor usar `marcarPrestado()`.

## 1. Tabla de responsabilidades (Actividad 2)

| Operación o decisión | Clase responsable | Justificación |
|---|---|---|
| Recibir `POST /api/prestamos` | `PrestamoController` | Capa de presentación: traduce HTTP/JSON a un `PrestamoRequest` y valida su formato (`@Valid`). |
| Buscar estudiante por identificador | `EstudianteRepository` (invocado por `PrestamoService`) | Acceso a datos; el servicio decide qué hacer si no existe (404). |
| Buscar libro por identificador | `LibroRepository` (invocado por `PrestamoService`) | Acceso a datos; el servicio lanza `RecursoNoEncontradoException` si no existe. |
| Validar disponibilidad | `PrestamoService` + `Libro.isDisponible()` + `PrestamoRepository.existsByLibroIdAndFechaDevolucionIsNull` | Regla de negocio (R1, R5). `Libro` es experto en su estado; el repositorio confirma que no haya préstamo activo. |
| Crear el préstamo | `PrestamoService` (Creador GRASP) | Tiene los datos de inicialización (estudiante, libro, fecha) → R2. |
| Cambiar disponibilidad | `Libro.marcarPrestado()` | Experto en información: el libro conoce y cambia su propio estado (R3). |
| Guardar cambios | `PrestamoRepository.save` / `LibroRepository.save` dentro de `@Transactional` | Persistencia atómica: o se guardan ambos cambios o ninguno (R4). |
| Devolver respuesta HTTP | `PrestamoController` / `GlobalExceptionHandler` | 201 con `PrestamoResponse`; 404, 409 y 400 se traducen en el `@RestControllerAdvice`. |

## 2. Relaciones UML y multiplicidades

| Relación | Tipo | Multiplicidad | Por qué |
|---|---|---|---|
| `Prestamo` → `Estudiante` | Asociación dirigida | `0..*` a `1` | El préstamo guarda una referencia persistente (`@ManyToOne`). No es composición: el estudiante existe sin el préstamo. |
| `Prestamo` → `Libro` | Asociación dirigida | `0..*` a `1` | Igual que el anterior; un libro tiene varios préstamos históricos y solo uno activo (R5). |
| `PrestamoController` → `PrestamoService` | Asociación dirigida | `1` a `1` | Referencia mantenida como atributo inyectado (no es un uso puntual). |
| `PrestamoService` → `*Repository` | Asociación dirigida | `1` a `1` | Atributos inyectados por constructor. |
| `*Repository` → `JpaRepository<T,ID>` | Generalización entre interfaces | – | `extends` de interfaz a interfaz. No hay realización propia: Spring Data genera la implementación. |
| `PrestamoController` ⇢ `PrestamoRequest`/`PrestamoResponse` | Dependencia «use» | – | Solo parámetros/retorno, sin atributo. |
| `PrestamoService` ⇢ `Prestamo` | Dependencia «create» | – | Lo instancia dentro de `registrarPrestamo`. |
| `PrestamoService` ⇢ excepciones | Dependencia «throw» | – | Uso puntual al lanzar 404/409. |

Se eliminó `GestorPrestamos`: sus listas en memoria pasan a los repositorios y su método `registrarPrestamo` pasa a `PrestamoService`.

## 3. Respuestas de la revisión (Actividad 7)

1. **¿Qué responsabilidades cambiaron?** `GestorPrestamos` concentraba datos (listas) y lógica. Ahora la entrada HTTP está en el controller, la coordinación y las reglas en el service, y el almacenamiento en los repositorios. Las entidades conservan solo su estado y comportamiento propio (`marcarPrestado`, `estaActivo`).
2. **Patrón GRASP de coordinación:** *Controlador* (`PrestamoService` maneja el evento de sistema "registrar préstamo"). Un controlador GRASP no equivale a `@RestController` porque el GRASP es un concepto de responsabilidad (quién coordina el caso de uso), mientras que `@RestController` es un adaptador técnico HTTP; si el caso de uso se invocara desde consola o mensajería, el controlador GRASP seguiría siendo el servicio.
3. **¿Dónde está la regla de disponibilidad?** En la capa de negocio (`PrestamoService.validarDisponibilidad`) apoyada en el experto `Libro`. No en el controller (sería lógica en presentación) ni en el repositorio (solo persiste).
4. **Relaciones conservadas/modificadas:** se conservan las asociaciones `Prestamo → Estudiante` y `Prestamo → Libro` (0..* a 1, ahora navegables solo desde `Prestamo`). Se eliminan las asociaciones del gestor con colecciones y aparecen asociaciones de inyección entre capas, dependencias hacia DTO/excepciones y generalización hacia `JpaRepository`.
5. **Trazabilidad requisito → clase → operación → prueba:**

| Requisito | Clase | Operación | Prueba |
|---|---|---|---|
| R1 | `PrestamoService`, `Libro` | `validarDisponibilidad`, `isDisponible` | `libroYaPrestado` (409) |
| R2 | `Prestamo` | constructor `Prestamo(id, fecha, estudiante, libro)` | `prestamoDeLibroDisponible` (201) |
| R3 | `Libro` | `marcarPrestado` | `prestamoDeLibroDisponible` (verifica `disponible=false`) |
| R4 | `PrestamoRequest`, `GlobalExceptionHandler` | `@Valid`, `manejarValidacion` | `solicitudInvalida` (400), `estudianteInexistente`, `libroInexistente` (404) |
| R5 | `PrestamoRepository` | `existsByLibroIdAndFechaDevolucionIsNull` | `libroYaPrestado`; datos con préstamo histórico devuelto |

## 4. Explicación breve de decisiones

- **Tres capas con dependencias en un solo sentido** (Controller → Service → Repository): bajo acoplamiento, el controller nunca toca repositorios.
- **Alta cohesión:** cada clase tiene un único motivo de cambio (HTTP, reglas, persistencia).
- **Entidades como modelo del dominio**, no como cuarta capa: las usan negocio y datos.
- **DTO de entrada y salida** para no exponer las entidades JPA y validar la solicitud antes de llegar al negocio.
- **`@Transactional`** en `registrarPrestamo`: el cambio de disponibilidad y la creación del préstamo son atómicos.
- **Excepciones de negocio + `@RestControllerAdvice`**: el servicio no conoce HTTP; el handler traduce a 404/409/400.
- **`Clock` inyectado** para que la fecha del préstamo sea controlable en pruebas.
