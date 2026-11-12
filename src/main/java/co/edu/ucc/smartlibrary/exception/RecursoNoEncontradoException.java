package co.edu.ucc.smartlibrary.exception;

/** Se traduce a 404 Not Found (estudiante o libro inexistente). */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, Long id) {
        super(recurso + " con id " + id + " no existe");
    }
}
