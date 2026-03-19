package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para empleados.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

    /**
     * Elimina un empleado por su documento.
     */
    void deleteByDocument(Long document);

    /**
     * Busca un empleado por su documento.
     */
    EmployeeEntity findByDocument(Long document);

    /**
     * Busca un empleado por su UUID público.
     */
    Optional<EmployeeEntity> findByUuid(String uuid);
}