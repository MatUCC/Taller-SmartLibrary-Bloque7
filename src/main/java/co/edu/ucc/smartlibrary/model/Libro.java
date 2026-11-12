package co.edu.ucc.smartlibrary.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad del dominio. Es el Experto en Información (GRASP) sobre su propia
 * disponibilidad: sabe si puede prestarse y cómo cambiar su estado (R1, R3).
 */
@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private boolean disponible;

    protected Libro() {
        // requerido por JPA
    }

    public Libro(Long id, String titulo, boolean disponible) {
        this.id = id;
        this.titulo = titulo;
        this.disponible = disponible;
    }

    /** R3: al prestarlo, el libro deja de estar disponible. */
    public void marcarPrestado() {
        if (!disponible) {
            throw new IllegalStateException("El libro " + id + " ya está prestado");
        }
        this.disponible = false;
    }

    public void marcarDevuelto() {
        this.disponible = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
}
