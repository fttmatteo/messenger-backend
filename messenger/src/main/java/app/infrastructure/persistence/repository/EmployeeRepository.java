package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}