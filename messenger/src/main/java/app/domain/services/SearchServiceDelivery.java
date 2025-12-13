package app.domain.services;

import java.util.List;
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

    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;

    public List<ServiceDelivery> findAll() {
        return serviceDeliveryPort.findAll();
    }

    public ServiceDelivery findById(Long id) throws BusinessException {
        ServiceDelivery service = serviceDeliveryPort.findById(id);
        if (service == null) {
            throw new BusinessException("El servicio con ID " + id + " no existe.");
        }
        return service;
    }

    public List<ServiceDelivery> findByStatus(Status status) {
        return serviceDeliveryPort.findByStatus(status);
    }

    public List<ServiceDelivery> findByMessenger(Long messengerDocument) {
        return serviceDeliveryPort.findByMessengerDocument(messengerDocument);
    }

    public List<ServiceDelivery> findByDealership(Long dealershipId) {
        return serviceDeliveryPort.findByDealershipId(dealershipId);
    }

    public List<ServiceDelivery> findByPlate(String plateNumber) {
        String normalized = plateNumber.trim().toUpperCase();
        return serviceDeliveryPort.findByPlateNumber(normalized);
    }
}