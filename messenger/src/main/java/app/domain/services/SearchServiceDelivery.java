package app.domain.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;

    /**
     * Retorna todos los servicios activos (excluye los eliminados).
     */
    public List<ServiceDelivery> findAll() {
        return serviceDeliveryPort.findAllActive();
    }

    /**
     * Retorna todos los servicios incluyendo los eliminados.
     */
    public List<ServiceDelivery> findAllIncludingDeleted() {
        return serviceDeliveryPort.findAll();
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
     * Retorna todos los servicios con paginación.
     */
    public Page<ServiceDelivery> findAllPaginated(String keyword, Boolean deleted, Pageable pageable) {
        return serviceDeliveryPort.findAllPaginated(keyword, deleted, pageable);
    }

    /**
     * Retorna servicios de un mensajero específico con paginación.
     */
    public Page<ServiceDelivery> findByMessengerPaginated(Long messengerId, String keyword, Boolean deleted,
            Pageable pageable) {
        return serviceDeliveryPort.findByMessengerPaginated(messengerId, keyword, deleted, pageable);
    }

    /**
     * Retorna todos los servicios en la papelera.
     */
    public List<ServiceDelivery> findDeleted() {
        return serviceDeliveryPort.findDeleted();
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
}