package co.edu.ucc.smartlibrary.dto;

import co.edu.ucc.smartlibrary.model.Prestamo;

import java.time.LocalDate;

/**
 * Objeto de salida: evita exponer directamente las entidades JPA en la API.
 */
public record PrestamoResponse(
        Long id,
        Long estudianteId,
        String estudianteNombre,
        Long libroId,
        String libroTitulo,
        LocalDate fechaPrestamo,
        LocalDate fechaDevolucion,
        boolean activo
) {

    public static PrestamoResponse desde(Prestamo p) {
        return new PrestamoResponse(
                p.getId(),
                p.getEstudiante().getId(),
                p.getEstudiante().getNombre(),
                p.getLibro().getId(),
                p.getLibro().getTitulo(),
                p.getFechaPrestamo(),
                p.getFechaDevolucion(),
                p.estaActivo()
        );
    }
}
