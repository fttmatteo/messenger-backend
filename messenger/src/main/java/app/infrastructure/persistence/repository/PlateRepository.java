package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.PlateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad PlateEntity.
 * 
 * Permite la búsqueda y gestión de placas vehiculares registradas en el
 * sistema.
 * Las placas son detectadas automáticamente por OCR o ingresadas manualmente.
 * 
 * Operaciones disponibles:
 * - CRUD completo (heredado de JpaRepository)
 * - Búsqueda por número de placa
 */
@Repository
public interface PlateRepository extends JpaRepository<PlateEntity, Long> {

    /**
     * Busca una placa por su número exacto.
     * 
     * El número de placa debe coincidir exactamente, incluyendo formato
     * (ej: "ABC 123" para carros, "ABC 12A" para motos, "123 ABC" para motocarros).
     * 
     * @param plateNumber Número de placa a buscar
     * @return La entidad de la placa encontrada, o null si no existe
     */
    PlateEntity findByPlateNumber(String plateNumber);
}