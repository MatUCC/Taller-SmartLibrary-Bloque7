package co.edu.ucc.smartlibrary.exception;

/** Se traduce a 409 Conflict (R1 / R5: el libro ya está prestado). */
public class LibroNoDisponibleException extends RuntimeException {

    public LibroNoDisponibleException(Long libroId) {
        super("El libro con id " + libroId + " no está disponible");
    }
}
