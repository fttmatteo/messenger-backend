package app.infrastructure.persistence.repository;

import app.domain.model.enums.Status;
import app.infrastructure.persistence.entities.ServiceDeliveryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repositorio JPA para la entidad ServiceDeliveryEntity.
 * 
 * Soporta búsquedas avanzadas por estado, mensajero, placa y concesionario.
 * Centraliza todas las consultas relacionadas con servicios de entrega de
 * placas.
 * 
 * Operaciones disponibles:
 * - CRUD completo (heredado de JpaRepository)
 * - Búsqueda por estado actual (ASSIGNED, DELIVERED, PENDING, etc.)
 * - Búsqueda por documento del mensajero asignado
 * - Búsqueda por número de placa
 * - Búsqueda por ID de concesionario
 */
@Repository
public interface ServiceDeliveryRepository extends JpaRepository<ServiceDeliveryEntity, Long> {

    /**
     * Busca todos los servicios de entrega que tienen un estado específico.
     * 
     * Útil para obtener todos los servicios pendientes, entregados, cancelados,
     * etc.
     * 
     * @param currentStatus Estado actual del servicio (ASSIGNED, DELIVERED,
     *                      PENDING, FAILED, etc.)
     * @return Lista de servicios con el estado especificado
     */
    List<ServiceDeliveryEntity> findByCurrentStatus(Status currentStatus);

    /**
     * Busca todos los servicios asignados a un mensajero específico.
     * 
     * Utiliza el documento de identidad del mensajero para la búsqueda.
     * 
     * @param messengerDocument Documento de identidad del mensajero
     * @return Lista de servicios asignados al mensajero
     */
    List<ServiceDeliveryEntity> findByMessenger_Document(Long messengerDocument);

    /**
     * Busca todos los servicios asociados a un número de placa específico.
     * 
     * Permite rastrear el historial de entregas de una placa en particular.
     * 
     * @param plateNumber Número de placa a buscar
     * @return Lista de servicios asociados a la placa
     */
    List<ServiceDeliveryEntity> findByPlate_PlateNumber(String plateNumber);

    /**
     * Busca todos los servicios destinados a un concesionario específico.
     * 
     * Útil para ver todas las entregas pendientes o realizadas a un concesionario.
     * 
     * @param dealershipId ID del concesionario
     * @return Lista de servicios destinados al concesionario
     */
    List<ServiceDeliveryEntity> findByDealership_IdDealership(Long dealershipId);
}