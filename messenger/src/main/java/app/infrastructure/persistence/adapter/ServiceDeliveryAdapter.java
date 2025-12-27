package app.infrastructure.persistence.adapter;

import app.domain.model.ServiceDelivery;
import app.domain.ports.ServiceDeliveryPort;
import app.infrastructure.persistence.entities.ServiceDeliveryEntity;
import app.infrastructure.persistence.mapper.ServiceDeliveryMapper;
import app.infrastructure.persistence.repository.ServiceDeliveryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter de persistencia para servicios de entrega.
 */
@Component
public class ServiceDeliveryAdapter implements ServiceDeliveryPort {

    @Autowired
    private ServiceDeliveryRepository repository;
    @Autowired
    private ServiceDeliveryMapper mapper;

    @Override
    /**
     * Guarda el servicio convirtiéndolo a entidad.
     */
    public ServiceDelivery save(ServiceDelivery serviceDelivery) {
        ServiceDeliveryEntity entity = mapper.toEntity(serviceDelivery);
        ServiceDeliveryEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
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
    public List<ServiceDelivery> findByPlateNumber(String plateNumber) {
        return repository.findByPlate_PlateNumber(plateNumber).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceDelivery> findByMessengerId(Long messengerId) {
        return repository.findByMessenger_IdEmployee(messengerId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceDelivery> findAllActive() {
        return repository.findByDeletedFalse().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    /**
     * Busca servicio activo (no eliminado).
     */
    public ServiceDelivery findByIdActive(Long idServiceDelivery) {
        Optional<ServiceDeliveryEntity> entity = repository.findByIdServiceDeliveryAndDeletedFalse(idServiceDelivery);
        return entity.map(mapper::toDomain).orElse(null);
    }

    @Override
    public List<ServiceDelivery> findDeleted() {
        return repository.findByDeletedTrue().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    /**
     * Busca servicios en la papelera que hayan expirado antes de la fecha dada.
     */
    public List<ServiceDelivery> findDeletedExpiredBefore(LocalDateTime date) {
        return repository.findByDeletedTrueAndDeletedAtBefore(date).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void hardDeleteById(Long idServiceDelivery) {
        repository.deleteById(idServiceDelivery);
    }

    @Override
    public int hardDeleteAllDeleted() {
        List<ServiceDeliveryEntity> deletedEntities = repository.findByDeletedTrue();
        int count = deletedEntities.size();
        repository.deleteAll(deletedEntities);
        return count;
    }

    @Override
    public List<app.domain.model.DailyStatistics> findDailyStatsByMessenger(
            Long messengerId,
            java.time.LocalDate from,
            java.time.LocalDate to) {
        /**
         * Obtiene estadísticas diarias delegando a la consulta nativa del repositorio.
         */
        // Convert LocalDate to LocalDateTime for the query
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.plusDays(1).atStartOfDay(); // End of 'to' day

        List<Object[]> rawResults = repository.findDailyStatsByMessenger(
                messengerId, fromDateTime, toDateTime);

        return rawResults.stream()
                .map(app.domain.model.DailyStatistics::fromRaw)
                .collect(Collectors.toList());
    }
}
