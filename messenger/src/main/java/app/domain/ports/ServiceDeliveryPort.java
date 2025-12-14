package app.domain.ports;

import app.domain.model.ServiceDelivery;
import app.domain.model.enums.Status;
import java.util.List;

/**
 * Puerto (interfaz) para operaciones de persistencia de servicios de entrega.
 * 
 * Define el contrato para acceder y manipular datos de servicios de entrega
 * sin depender de detalles de implementación específicos (JPA, MongoDB, etc.).
 * Implementado por adaptadores en la capa de infraestructura.
 */
public interface ServiceDeliveryPort {
    /**
     * Guarda o actualiza un servicio de entrega.
     * 
     * @param serviceDelivery Servicio a guardar.
     */
    void save(ServiceDelivery serviceDelivery);

    /**
     * Elimina un servicio de entrega por su ID.
     * 
     * @param idServiceDelivery ID del servicio a eliminar.
     */
    void deleteById(Long idServiceDelivery);

    /**
     * Busca un servicio de entrega por su ID.
     * 
     * @param idServiceDelivery ID del servicio.
     * @return Servicio encontrado o null si no existe.
     */
    ServiceDelivery findById(Long idServiceDelivery);

    /**
     * Obtiene todos los servicios de entrega.
     * 
     * @return Lista de todos los servicios.
     */
    List<ServiceDelivery> findAll();

    /**
     * Busca servicios por estado.
     * 
     * @param status Estado del servicio (PENDING, DELIVERED, etc).
     * @return Lista de servicios con el estado especificado.
     */
    List<ServiceDelivery> findByStatus(Status status);

    /**
     * Busca servicios asignados a un mensajero específico.
     * 
     * @param messengerDocument Documento del mensajero.
     * @return Lista de servicios asignados al mensajero.
     */
    List<ServiceDelivery> findByMessengerDocument(Long messengerDocument);

    /**
     * Busca servicios asociados a un número de placa.
     * 
     * @param plateNumber Número de placa vehicular.
     * @return Lista de servicios relacionados con la placa.
     */
    List<ServiceDelivery> findByPlateNumber(String plateNumber);

    /**
     * Busca servicios destinados a un concesionario específico.
     * 
     * @param dealershipId ID del concesionario.
     * @return Lista de servicios del concesionario.
     */
    List<ServiceDelivery> findByDealershipId(Long dealershipId);
}