package app.infrastructure.persistence.repository;

import app.domain.model.enums.Role;
import app.infrastructure.persistence.entities.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para empleados.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {
    void deleteByDocument(Long document);

    EmployeeEntity findByDocument(Long document);

    Optional<EmployeeEntity> findByUuid(String uuid);

    List<EmployeeEntity> findByRole(Role role);
}