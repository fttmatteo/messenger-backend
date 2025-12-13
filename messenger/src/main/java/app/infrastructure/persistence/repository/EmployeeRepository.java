package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad EmployeeEntity.
 * Permite buscar empleados por documento, nombre de usuario y gestionar sus
 * datos.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {
    EmployeeEntity findByDocument(Long document);

    void deleteByDocument(Long document);

    EmployeeEntity findByUserName(String userName);
}