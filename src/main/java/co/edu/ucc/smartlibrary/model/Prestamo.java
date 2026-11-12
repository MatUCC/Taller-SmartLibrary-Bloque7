package co.edu.ucc.smartlibrary.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Entidad del dominio. R2: registra estudiante, libro y fecha.
 * Asociaciones (no composición): Prestamo 0..* -> 1 Estudiante y Prestamo 0..* -> 1 Libro,
 * porque el ciclo de vida del estudiante y del libro no depende del préstamo.
 */
@Entity
@Table(name = "prestamos")
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fechaPrestamo;

    /** [0..1]: es nula mientras el préstamo está activo. */
    private LocalDate fechaDevolucion;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;

    protected Prestamo() {
        // requerido por JPA
    }

    public Prestamo(Long id, LocalDate fechaPrestamo, Estudiante estudiante, Libro libro) {
        this.id = id;
        this.fechaPrestamo = fechaPrestamo;
        this.estudiante = estudiante;
        this.libro = libro;
    }

    /** R5: un préstamo está activo mientras no tenga fecha de devolución. */
    public boolean estaActivo() {
        return fechaDevolucion == null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDate fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }

    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDate fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }

    public Libro getLibro() { return libro; }
    public void setLibro(Libro libro) { this.libro = libro; }
}
