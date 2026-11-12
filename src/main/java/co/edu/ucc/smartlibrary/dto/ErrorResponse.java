package co.edu.ucc.smartlibrary.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        int status,
        String error,
        String mensaje,
        List<String> detalles,
        LocalDateTime timestamp
) {

    public static ErrorResponse de(int status, String error, String mensaje) {
        return new ErrorResponse(status, error, mensaje, List.of(), LocalDateTime.now());
    }

    public static ErrorResponse de(int status, String error, String mensaje, List<String> detalles) {
        return new ErrorResponse(status, error, mensaje, detalles, LocalDateTime.now());
    }
}
