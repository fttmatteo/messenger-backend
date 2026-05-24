package app.application.usecase.delivery;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import app.domain.exception.BusinessException;
import app.domain.model.ServiceDelivery;
import app.domain.ports.ServiceDeliveryPort;

/**
 * Servicio para búsqueda de servicios de entrega.
 * Por defecto, excluye los servicios eliminados (en papelera).
 */
@Service
public class SearchServiceDeliveryUseCase {

    private static final Logger logger = LoggerFactory.getLogger(SearchServiceDeliveryUseCase.class);
    private final ServiceDeliveryPort serviceDeliveryPort;

    public SearchServiceDeliveryUseCase(ServiceDeliveryPort serviceDeliveryPort) {
        this.serviceDeliveryPort = serviceDeliveryPort;
    }

    /**
     * Busca un servicio por ID (excluye los eliminados).
     */
    public ServiceDelivery findById(Long id) throws BusinessException {
        ServiceDelivery service = serviceDeliveryPort.findByIdActive(id);
        if (service == null) {
            logger.warn("Servicio no encontrado por ID.");
            throw new BusinessException("El servicio no existe o está en la papelera.");
        }
        return service;
    }

    /**
     * Busca un servicio por UUID (excluye los eliminados).
     */
    public ServiceDelivery findByUuid(String uuid) throws BusinessException {
        ServiceDelivery service = serviceDeliveryPort.findByUuidActive(uuid);
        if (service == null) {
            logger.warn("Servicio no encontrado por UUID.");
            throw new BusinessException("El servicio no existe o está en la papelera.");
        }
        return service;
    }

    /**
     * Busca un servicio por UUID incluyendo los eliminados.
     */
    public ServiceDelivery findByUuidIncludingDeleted(String uuid) throws BusinessException {
        ServiceDelivery service = serviceDeliveryPort.findByUuidIncludingDeleted(uuid);
        if (service == null) {
            logger.warn("Servicio no encontrado (incluyendo eliminados) por UUID.");
            throw new BusinessException("El servicio no existe.");
        }
        return service;
    }

    /**
     * Busca un servicio por ID incluyendo los eliminados.
     */
    public ServiceDelivery findByIdIncludingDeleted(Long id) throws BusinessException {
        ServiceDelivery service = serviceDeliveryPort.findById(id);
        if (service == null) {
            logger.warn("Servicio no encontrado (incluyendo eliminados) por ID.");
            throw new BusinessException("El servicio no existe.");
        }
        return service;
    }

    /**
     * Retorna todos los servicios con paginación y filtro de estado.
     */
    public Page<ServiceDelivery> findAllPaginated(String keyword, Boolean deleted,
            List<app.domain.model.enums.Status> statuses, Pageable pageable) {
        return serviceDeliveryPort.findAllPaginated(keyword, deleted, statuses, pageable);
    }

    /**
     * Retorna servicios de un mensajero específico con paginación y filtro de
     * estado.
     */
    public Page<ServiceDelivery> findByMessengerPaginated(Long messengerId, String keyword, Boolean deleted,
            List<app.domain.model.enums.Status> statuses, Pageable pageable) {
        return serviceDeliveryPort.findByMessengerPaginated(messengerId, keyword, deleted, statuses, pageable);
    }

    /**
     * Retorna todos los servicios en la papelera con paginación.
     */
    public Page<ServiceDelivery> findDeleted(Pageable pageable) {
        return serviceDeliveryPort.findDeleted(pageable);
    }


    /**
     * Busca servicios por número de chasis filtrado por concesionario con paginación.
     */
    public Page<ServiceDelivery> findByPlateAndDealershipPaginated(String plateNumber, Long dealershipId,
            Pageable pageable) {
        String normalized = plateNumber.trim().toUpperCase();
        return serviceDeliveryPort.findByPlateAndDealershipPaginated(normalized, dealershipId, pageable);
    }

    /**
     * Retorna todos los servicios de un concesionario filtrados por una lista de
     * estados con paginación.
     */
    public Page<ServiceDelivery> findByDealershipAndStatusesPaginated(Long dealershipId,
            List<app.domain.model.enums.Status> statuses, Pageable pageable) {
        return serviceDeliveryPort.findByDealershipIdAndStatusesPaginated(dealershipId, statuses, pageable);
    }
}
