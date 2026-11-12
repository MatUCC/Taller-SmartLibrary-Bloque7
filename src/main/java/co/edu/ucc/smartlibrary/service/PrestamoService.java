package co.edu.ucc.smartlibrary.service;

import co.edu.ucc.smartlibrary.dto.PrestamoRequest;
import co.edu.ucc.smartlibrary.dto.PrestamoResponse;
import co.edu.ucc.smartlibrary.exception.LibroNoDisponibleException;
import co.edu.ucc.smartlibrary.exception.RecursoNoEncontradoException;
import co.edu.ucc.smartlibrary.model.Estudiante;
import co.edu.ucc.smartlibrary.model.Libro;
import co.edu.ucc.smartlibrary.model.Prestamo;
import co.edu.ucc.smartlibrary.repository.EstudianteRepository;
import co.edu.ucc.smartlibrary.repository.LibroRepository;
import co.edu.ucc.smartlibrary.repository.PrestamoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Capa de negocio. Controlador GRASP del caso de uso "Registrar préstamo":
 * coordina repositorios y entidades y aplica las reglas R1–R5.
 */
@Service
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;
    private final EstudianteRepository estudianteRepository;
    private final Clock clock;

    public PrestamoService(PrestamoRepository prestamoRepository,
                           LibroRepository libroRepository,
                           EstudianteRepository estudianteRepository,
                           Clock clock) {
        this.prestamoRepository = prestamoRepository;
        this.libroRepository = libroRepository;
        this.estudianteRepository = estudianteRepository;
        this.clock = clock;
    }

    /**
     * Registra un préstamo. Transaccional: el cambio de disponibilidad del libro
     * y la creación del préstamo se confirman juntos o no se confirma ninguno.
     */
    @Transactional
    public PrestamoResponse registrarPrestamo(PrestamoRequest request) {
        Estudiante estudiante = buscarEstudiante(request.estudianteId());
        Libro libro = buscarLibro(request.libroId());

        validarDisponibilidad(libro);                              // R1, R5

        Prestamo prestamo = new Prestamo(null, LocalDate.now(clock), estudiante, libro); // R2
        libro.marcarPrestado();                                     // R3

        libroRepository.save(libro);
        Prestamo guardado = prestamoRepository.save(prestamo);
        return PrestamoResponse.desde(guardado);
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponse> listarPrestamos() {
        return prestamoRepository.findAllConDetalle().stream()
                .map(PrestamoResponse::desde)
                .toList();
    }

    private Estudiante buscarEstudiante(Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Estudiante", id));
    }

    private Libro buscarLibro(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro", id));
    }

    private void validarDisponibilidad(Libro libro) {
        if (!libro.isDisponible() || prestamoRepository.existsByLibroIdAndFechaDevolucionIsNull(libro.getId())) {
            throw new LibroNoDisponibleException(libro.getId());
        }
    }
}
