package app.domain.services;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.application.exceptions.BusinessException;
import app.domain.model.ServiceDelivery;
import app.domain.model.enums.Status;
import app.domain.ports.ServiceDeliveryPort;

/**
 * Servicio de dominio para búsqueda y recuperación de servicios de entrega.
 * 
 * Proporciona múltiples criterios de búsqueda:
 * 
 * Por ID, estado, mensajero, concesionario o número de placa
 * Listado completo de servicios
 */
@Service
public class SearchServiceDelivery {

    private static final Logger logger = LoggerFactory.getLogger(SearchServiceDelivery.class);

    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;

    /**
     * Obtiene todos los servicios de entrega registrados.
     * 
     * @return Lista completa de servicios.
     */
    public List<ServiceDelivery> findAll() {
        logger.debug("Buscando todos los servicios de entrega");
        List<ServiceDelivery> services = serviceDeliveryPort.findAll();
        logger.debug("Servicios encontrados: {}", services.size());
        return services;
    }

    /**
     * Busca un servicio de entrega por su ID.
     * 
     * @param id ID del servicio.
     * @return Servicio encontrado.
     * @throws BusinessException Si el servicio no existe.
     */
    public ServiceDelivery findById(Long id) throws BusinessException {
        ServiceDelivery service = serviceDeliveryPort.findById(id);
        if (service == null) {
            throw new BusinessException("El servicio con ID " + id + " no existe.");
        }
        return service;
    }

    /**
     * Busca servicios por estado.
     * 
     * @param status Estado del servicio.
     * @return Lista de servicios con el estado especificado.
     */
    public List<ServiceDelivery> findByStatus(Status status) {
        return serviceDeliveryPort.findByStatus(status);
    }

    /**
     * Busca servicios asignados a un mensajero.
     * 
     * @param messengerDocument Documento del mensajero.
     * @return Lista de servicios del mensajero.
     */
    public List<ServiceDelivery> findByMessenger(Long messengerDocument) {
        return serviceDeliveryPort.findByMessengerDocument(messengerDocument);
    }

    /**
     * Busca servicios destinados a un concesionario.
     * 
     * @param dealershipId ID del concesionario.
     * @return Lista de servicios del concesionario.
     */
    public List<ServiceDelivery> findByDealership(Long dealershipId) {
        return serviceDeliveryPort.findByDealershipId(dealershipId);
    }

    /**
     * Busca servicios asociados a un número de placa.
     * 
     * @param plateNumber Número de placa vehicular.
     * @return Lista de servicios relacionados con la placa.
     */
    public List<ServiceDelivery> findByPlate(String plateNumber) {
        String normalized = plateNumber.trim().toUpperCase();
        return serviceDeliveryPort.findByPlateNumber(normalized);
    }
}