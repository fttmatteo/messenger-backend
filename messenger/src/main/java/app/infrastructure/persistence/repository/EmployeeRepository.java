package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad EmployeeEntity.
 * 
 * Permite buscar empleados por documento y gestionar sus datos.
 * Soporta operaciones de autenticación y gestión de usuarios del sistema.
 * 
 * Operaciones disponibles:
 * - CRUD completo (heredado de JpaRepository)
 * - Búsqueda por documento de identidad (para login)
 * - Eliminación por documento
 */
@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

    /**
     * Elimina un empleado por su número de documento.
     * 
     * @param document Número de documento del empleado a eliminar
     */
    void deleteByDocument(Long document);

    /**
     * Busca un empleado por su número de documento.
     * 
     * Utilizado principalmente para autenticación y login en el sistema.
     * 
     * @param document Número de documento del empleado
     * @return La entidad del empleado encontrado, o null si no existe
     */
    EmployeeEntity findByDocument(Long document);
}