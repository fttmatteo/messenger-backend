package app.domain.services;

import java.util.List;
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
public class SearchServiceDelivery {

    private final ServiceDeliveryPort serviceDeliveryPort;

    public SearchServiceDelivery(ServiceDeliveryPort serviceDeliveryPort) {
        this.serviceDeliveryPort = serviceDeliveryPort;
    }

    /**
     * Busca un servicio por ID (excluye los eliminados).
     */
    public ServiceDelivery findById(Long id) throws BusinessException {
        ServiceDelivery service = serviceDeliveryPort.findByIdActive(id);
        if (service == null) {
            throw new BusinessException("El servicio con ID " + id + " no existe o está en la papelera.");
        }
        return service;
    }

    /**
     * Busca un servicio por UUID (excluye los eliminados).
     */
    public ServiceDelivery findByUuid(String uuid) throws BusinessException {
        ServiceDelivery service = serviceDeliveryPort.findByUuidActive(uuid);
        if (service == null) {
            throw new BusinessException("El servicio con UUID " + uuid + " no existe o está en la papelera.");
        }
        return service;
    }

    /**
     * Busca un servicio por UUID incluyendo los eliminados.
     */
    public ServiceDelivery findByUuidIncludingDeleted(String uuid) throws BusinessException {
        ServiceDelivery service = serviceDeliveryPort.findByUuidIncludingDeleted(uuid);
        if (service == null) {
            throw new BusinessException("El servicio con UUID " + uuid + " no existe.");
        }
        return service;
    }

    /**
     * Busca un servicio por ID incluyendo los eliminados.
     */
    public ServiceDelivery findByIdIncludingDeleted(Long id) throws BusinessException {
        ServiceDelivery service = serviceDeliveryPort.findById(id);
        if (service == null) {
            throw new BusinessException("El servicio con ID " + id + " no existe.");
        }
        return service;
    }

    /**
     * Busca servicios por número de placa (excluye los eliminados).
     */
    public List<ServiceDelivery> findByPlate(String plateNumber) {
        String normalized = plateNumber.trim().toUpperCase();
        return serviceDeliveryPort.findByPlateNumber(normalized);
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
     * Retorna estadísticas diarias de servicios para un mensajero.
     */
    public List<app.domain.model.DailyStatistics> findDailyStatsByMessenger(
            Long messengerId,
            java.time.LocalDate from,
            java.time.LocalDate to) {
        return serviceDeliveryPort.findDailyStatsByMessenger(messengerId, from, to);
    }

    /**
     * Busca servicios por número de placa filtrado por concesionario.
     */
    public List<ServiceDelivery> findByPlateAndDealership(String plateNumber, Long dealershipId) {
        String normalized = plateNumber.trim().toUpperCase();
        return serviceDeliveryPort.findByPlateNumberAndDealershipId(normalized, dealershipId);
    }

    /**
     * Retorna todos los servicios pendientes de un concesionario.
     */
    public List<ServiceDelivery> findPendingByDealership(Long dealershipId) {
        return serviceDeliveryPort.findPendingByDealershipId(dealershipId);
    }

    /**
     * Retorna todos los servicios de un concesionario filtrados por una lista de
     * estados.
     */
    public List<ServiceDelivery> findByDealershipAndStatuses(Long dealershipId,
            List<app.domain.model.enums.Status> statuses) {
        return serviceDeliveryPort.findByDealershipIdAndStatuses(dealershipId, statuses);
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