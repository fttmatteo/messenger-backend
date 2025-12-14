package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.DealershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad DealershipEntity.
 * 
 * Proporciona métodos CRUD estándar heredados de JpaRepository y consultas
 * personalizadas para gestión de concesionarios por nombre.
 * 
 * Operaciones disponibles:
 * - CRUD completo (heredado de JpaRepository)
 * - Búsqueda por nombre
 * - Eliminación por nombre
 */
@Repository
public interface DealershipRepository extends JpaRepository<DealershipEntity, Long> {

    /**
     * Elimina un concesionario por su nombre.
     * 
     * @param name Nombre del concesionario a eliminar
     */
    void deleteByName(String name);

    /**
     * Busca un concesionario por su nombre exacto.
     * 
     * @param name Nombre del concesionario a buscar
     * @return La entidad del concesionario encontrado, o null si no existe
     */
    DealershipEntity findByName(String name);
}