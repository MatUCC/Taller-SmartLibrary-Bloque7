package co.edu.ucc.smartlibrary.controller;

import co.edu.ucc.smartlibrary.dto.PrestamoRequest;
import co.edu.ucc.smartlibrary.dto.PrestamoResponse;
import co.edu.ucc.smartlibrary.service.PrestamoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Capa de presentación: solo entrada/salida HTTP. No contiene reglas de negocio
 * ni accede a los repositorios (Controller -> Service -> Repository).
 */
@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @PostMapping
    public ResponseEntity<PrestamoResponse> registrarPrestamo(@Valid @RequestBody PrestamoRequest request) {
        PrestamoResponse creado = prestamoService.registrarPrestamo(request);
        return ResponseEntity.created(URI.create("/api/prestamos/" + creado.id())).body(creado);
    }

    @GetMapping
    public ResponseEntity<List<PrestamoResponse>> listarPrestamos() {
        return ResponseEntity.ok(prestamoService.listarPrestamos());
    }
}
