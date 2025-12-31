package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.DealershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para concesionarios.
 */
@Repository
public interface DealershipRepository extends JpaRepository<DealershipEntity, Long> {

    /**
     * Elimina un concesionario por su nombre.
     */
    void deleteByName(String name);

    /**
     * Busca un concesionario por su nombre.
     */
    DealershipEntity findByName(String name);
}