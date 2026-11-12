package co.edu.ucc.smartlibrary.repository;

import co.edu.ucc.smartlibrary.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    /** R5: ¿el libro ya tiene un préstamo activo (sin fecha de devolución)? */
    boolean existsByLibroIdAndFechaDevolucionIsNull(Long libroId);

    /** Consulta de préstamos cargando estudiante y libro en una sola consulta. */
    @Query("select p from Prestamo p join fetch p.estudiante join fetch p.libro order by p.id")
    List<Prestamo> findAllConDetalle();
}
