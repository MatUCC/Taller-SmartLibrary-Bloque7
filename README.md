# Taller Bloque 7 – SmartLibrary

Del modelo de clases orientado a objetos a un diseño por **tres capas** (Controller → Service → Repository) para una API REST con Spring Boot. Caso de uso: **Registrar préstamo** y consulta de préstamos.

**Diseño de Software – Cuarto semestre**

**Integrantes:**
- Matías Castaño Raigoza
- Juan David Acosta Urbano

📄 **Informe completo en Word:** [`docs/entrega/Informe-Taller-SmartLibrary.docx`](docs/entrega/Informe-Taller-SmartLibrary.docx). Incluye todas las actividades, los cuatro diagramas, las tablas y las evidencias de pruebas.

## Contenido

```
├── src/main/java/co/edu/ucc/smartlibrary
│   ├── controller/   PrestamoController            (presentación: HTTP)
│   ├── dto/          PrestamoRequest, PrestamoResponse, ErrorResponse
│   ├── service/      PrestamoService               (negocio: reglas R1–R5)
│   ├── repository/   Estudiante/Libro/PrestamoRepository (acceso a datos)
│   ├── model/        Estudiante, Libro, Prestamo   (dominio, entidades JPA)
│   ├── exception/    404 / 409 / 400 + GlobalExceptionHandler
│   └── config/       ClockConfig
├── src/main/resources/  application.properties, data.sql (datos iniciales H2)
├── src/test/...         Pruebas de los escenarios de la Actividad 6
├── diagramas/
│   ├── visual-paradigm/SmartLibrary.vpp   proyecto de Visual Paradigm con los 4 diagramas
│   ├── visual-paradigm/export/            PNG exportados por Visual Paradigm
│   └── plantuml/                          versión alternativa en PlantUML
├── docs/entrega/       informe del taller en Word (.docx)
├── docs/DISENO.md      Actividad 1, tabla de responsabilidades, relaciones, defensa y decisiones
└── docs/evidencias/    salida de las pruebas automatizadas y de las pruebas de la API con curl
```

## Diagramas (Visual Paradigm)

Los cuatro diagramas están en el proyecto [`diagramas/visual-paradigm/SmartLibrary.vpp`](diagramas/visual-paradigm/SmartLibrary.vpp) (Visual Paradigm 18.1). Ábrelo con `Project > Open` y búscalos en el *Diagram Navigator*.

### 1. Diagrama inicial anotado (Actividad 1)
El modelo de partida (`GestorPrestamos`, `Estudiante`, `Libro`, `Prestamo`) con sus asociaciones y multiplicidades, y notas con las observaciones: baja cohesión del gestor, reglas que no se validan, por qué son asociaciones y no composiciones, y qué responsabilidades faltan (HTTP y acceso a datos).

![Diagrama inicial anotado](<diagramas/visual-paradigm/export/Diagrama inicial anotado.png>)

### 2. Diagrama de clases por capas (Actividad 3)
Paquetes `controller`, `dto`, `service`, `exception`, `repository` y `model` con atributos y operaciones. Incluye:
- **Asociaciones dirigidas con multiplicidad:** `Prestamo 0..* → 1 Estudiante`, `Prestamo 0..* → 1 Libro` y las referencias inyectadas entre capas (`1 → 1`).
- **Generalización** de cada repositorio hacia `JpaRepository<T, ID>`.
- **Dependencias** `«use»`, `«create»`, `«throw»` y `«handle»`.

![Diagrama de clases](<diagramas/visual-paradigm/export/Diagrama de clases por capas.png>)

### 3. Diagrama de arquitectura en capas
Capas Presentación → Negocio → Acceso a Datos, el modelo del dominio como paquete transversal, el cliente HTTP y la base de datos H2.

![Diagrama de capas](<diagramas/visual-paradigm/export/Diagrama de arquitectura en capas.png>)

### 4. Diagrama de secuencia – Registrar préstamo (Actividad 4)
`POST /api/prestamos` con las líneas de vida Usuario, `PrestamoController`, `PrestamoService`, los tres repositorios, `libro:Libro` y `prestamo:Prestamo`. Un fragmento `alt` separa el préstamo exitoso (201) del libro no disponible (409). Cada mensaje corresponde a una operación del diagrama de clases.

![Diagrama de secuencia](<diagramas/visual-paradigm/export/Diagrama de secuencia - Registrar prestamo.png>)

> Las imágenes se exportaron con una licencia de evaluación de Visual Paradigm, por eso llevan la marca de agua. Con una licencia activada, `Project > Export > Active Diagram as Image` las genera limpias.

Los `.puml` de `diagramas/plantuml/` son una versión alternativa de los mismos diagramas en PlantUML.

## Ejecutar

Requisitos: Java 21 (probado con Temurin 21.0.13). También compila con Java 17 usando `-Djava.version=17`.

```bash
./mvnw spring-boot:run
```

- API: `http://localhost:8080/api/prestamos`
- Consola H2: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:smartlibrary`, usuario `sa`)

```bash
# Préstamo exitoso -> 201
curl -i -X POST http://localhost:8080/api/prestamos -H "Content-Type: application/json" -d '{"estudianteId":1,"libroId":2}'
# Libro ya prestado (libro 3) -> 409
curl -i -X POST http://localhost:8080/api/prestamos -H "Content-Type: application/json" -d '{"estudianteId":1,"libroId":3}'
# Consulta -> 200
curl -i http://localhost:8080/api/prestamos
```

## Pruebas (Actividad 6)

```bash
./mvnw test
```

| Escenario | Resultado esperado | Prueba |
|---|---|---|
| Préstamo de libro disponible | 201 Created y préstamo registrado | `prestamoDeLibroDisponible` |
| Libro ya prestado | 409 Conflict; sin nuevo préstamo | `libroYaPrestado` |
| Estudiante inexistente | 404 Not Found | `estudianteInexistente` |
| Libro inexistente | 404 Not Found | `libroInexistente` |
| Consulta de préstamos | 200 OK con registros | `consultaDePrestamos` |
| Solicitud inválida (R4) | 400 Bad Request; sin préstamo | `solicitudInvalida` |

### Evidencias
- [`docs/evidencias/salida-mvn-test.txt`](docs/evidencias/salida-mvn-test.txt): las 6 pruebas automatizadas en verde.
- [`docs/evidencias/pruebas-api-curl.txt`](docs/evidencias/pruebas-api-curl.txt): peticiones reales a la API en ejecución y sus respuestas (201, 409, 409, 404, 404, 400 y 200). La consulta final confirma que los casos de error no crearon préstamos.
- [`docs/evidencias/pruebas-api.sh`](docs/evidencias/pruebas-api.sh): el script que genera ese archivo (`./docs/evidencias/pruebas-api.sh 8080` con la app levantada).

## Reglas de negocio

- **R1** solo un libro disponible puede prestarse → `PrestamoService.validarDisponibilidad`
- **R2** el préstamo registra estudiante, libro y fecha → constructor de `Prestamo`
- **R3** al prestarlo, el libro deja de estar disponible → `Libro.marcarPrestado`
- **R4** una solicitud inválida no crea préstamo → `@Valid` + `@Transactional`
- **R5** varios préstamos históricos, solo uno activo → `PrestamoRepository.existsByLibroIdAndFechaDevolucionIsNull`

Más detalle en [docs/DISENO.md](docs/DISENO.md).
