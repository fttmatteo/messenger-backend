package app.adapter.out.persistence;

import app.domain.model.ServiceDelivery;
import app.domain.model.enums.Status;
import app.domain.ports.ServiceDeliveryPort;
import app.infrastructure.persistence.entities.ServiceDeliveryEntity;
import app.infrastructure.persistence.mapper.ServiceDeliveryMapper;
import app.infrastructure.persistence.repository.ServiceDeliveryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de salida para persistencia de servicios de entrega.
 * 
 * Este adaptador implementa ServiceDeliveryPort y actúa como puente entre la
 * capa de dominio
 * y la capa de infraestructura (JPA), manejando la conversión entre objetos de
 * dominio
 * (ServiceDelivery) y entidades de persistencia (ServiceDeliveryEntity).
 * 
 * Responsabilidades:
 * - Convertir objetos de dominio a entidades JPA y viceversa usando
 * ServiceDeliveryMapper
 * - Delegar operaciones de persistencia al ServiceDeliveryRepository
 * - Mantener la independencia del dominio respecto a detalles de persistencia
 * 
 * Operaciones soportadas:
 * - save: Guardar o actualizar un servicio de entrega
 * - findById: Buscar por ID
 * - findByStatus: Buscar por estado actual (PENDING, DELIVERED, etc.)
 * - findByMessengerDocument: Buscar servicios asignados a un mensajero
 * - findByPlateNumber: Buscar servicios asociados a una placa
 * - findByDealershipId: Buscar servicios de un concesionario
 * - findAll: Obtener todos los servicios
 * - deleteById: Eliminar un servicio
 * 
 * Este adaptador es central para el sistema ya que los servicios de entrega
 * son la entidad principal del negocio.
 * 
 * @see app.domain.ports.ServiceDeliveryPort
 * @see app.infrastructure.persistence.repository.ServiceDeliveryRepository
 * @see app.infrastructure.persistence.mapper.ServiceDeliveryMapper
 */
@Component
public class ServiceDeliveryAdapter implements ServiceDeliveryPort {

    @Autowired
    private ServiceDeliveryRepository repository;
    @Autowired
    private ServiceDeliveryMapper mapper;

    @Override
    public void save(ServiceDelivery serviceDelivery) {
        ServiceDeliveryEntity entity = mapper.toEntity(serviceDelivery);
        ServiceDeliveryEntity savedEntity = repository.save(entity);
        serviceDelivery.setIdServiceDelivery(savedEntity.getIdServiceDelivery());
    }

    @Override
    public void deleteById(Long idServiceDelivery) {
        repository.deleteById(idServiceDelivery);
    }

    @Override
    public ServiceDelivery findById(Long idServiceDelivery) {
        Optional<ServiceDeliveryEntity> entity = repository.findById(idServiceDelivery);
        if (entity.isPresent()) {
            return mapper.toDomain(entity.get());
        }
        return null;
    }

    @Override
    public List<ServiceDelivery> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceDelivery> findByStatus(Status status) {
        return repository.findByCurrentStatus(status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceDelivery> findByMessengerDocument(Long messengerDocument) {
        return repository.findByMessenger_Document(messengerDocument).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceDelivery> findByPlateNumber(String plateNumber) {
        return repository.findByPlate_PlateNumber(plateNumber).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceDelivery> findByDealershipId(Long dealershipId) {
        return repository.findByDealership_IdDealership(dealershipId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}