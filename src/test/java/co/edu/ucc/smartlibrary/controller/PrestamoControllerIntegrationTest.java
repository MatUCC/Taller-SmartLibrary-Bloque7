package co.edu.ucc.smartlibrary.controller;

import co.edu.ucc.smartlibrary.model.Libro;
import co.edu.ucc.smartlibrary.repository.LibroRepository;
import co.edu.ucc.smartlibrary.repository.PrestamoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Escenarios de la Actividad 6. Cada prueba se revierte al terminar (@Transactional).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PrestamoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private LibroRepository libroRepository;

    private static String cuerpo(long estudianteId, long libroId) {
        return "{\"estudianteId\":" + estudianteId + ",\"libroId\":" + libroId + "}";
    }

    @Test
    @DisplayName("Préstamo de libro disponible -> 201 Created y préstamo registrado")
    void prestamoDeLibroDisponible() throws Exception {
        long antes = prestamoRepository.count();

        mockMvc.perform(post("/api/prestamos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(1, 2)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.estudianteId").value(1))
                .andExpect(jsonPath("$.libroId").value(2))
                .andExpect(jsonPath("$.activo").value(true));

        assertThat(prestamoRepository.count()).isEqualTo(antes + 1);
        Libro libro = libroRepository.findById(2L).orElseThrow();
        assertThat(libro.isDisponible()).isFalse(); // R3
    }

    @Test
    @DisplayName("Libro ya prestado -> 409 Conflict; sin nuevo préstamo")
    void libroYaPrestado() throws Exception {
        long antes = prestamoRepository.count();

        mockMvc.perform(post("/api/prestamos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(1, 3)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        assertThat(prestamoRepository.count()).isEqualTo(antes);
    }

    @Test
    @DisplayName("Estudiante inexistente -> 404 Not Found")
    void estudianteInexistente() throws Exception {
        long antes = prestamoRepository.count();

        mockMvc.perform(post("/api/prestamos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(999, 1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Estudiante con id 999 no existe"));

        assertThat(prestamoRepository.count()).isEqualTo(antes);
    }

    @Test
    @DisplayName("Libro inexistente -> 404 Not Found")
    void libroInexistente() throws Exception {
        mockMvc.perform(post("/api/prestamos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(1, 999)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Libro con id 999 no existe"));
    }

    @Test
    @DisplayName("Solicitud inválida -> 400 Bad Request; sin nuevo préstamo (R4)")
    void solicitudInvalida() throws Exception {
        long antes = prestamoRepository.count();

        mockMvc.perform(post("/api/prestamos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estudianteId\":null}"))
                .andExpect(status().isBadRequest());

        assertThat(prestamoRepository.count()).isEqualTo(antes);
    }

    @Test
    @DisplayName("Consulta de préstamos -> 200 OK con registros")
    void consultaDePrestamos() throws Exception {
        mockMvc.perform(get("/api/prestamos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[0].libroTitulo").value("Refactoring"))
                .andExpect(jsonPath("$[0].estudianteNombre").value("Luis Pérez"));
    }
}
