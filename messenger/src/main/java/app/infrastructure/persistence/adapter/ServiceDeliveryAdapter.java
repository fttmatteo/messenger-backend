package app.infrastructure.persistence.adapter;

import app.domain.model.ServiceDelivery;
import app.domain.ports.ServiceDeliveryPort;
import app.infrastructure.persistence.entities.ServiceDeliveryEntity;
import app.infrastructure.persistence.mapper.ServiceDeliveryMapper;
import app.infrastructure.persistence.repository.ServiceDeliveryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * Elimina un servicio por su ID delegando al repositorio JPA.
     */
    @Override
    public void deleteById(Long idServiceDelivery) {
        repository.deleteById(idServiceDelivery);
    }

    /**
     * Busca un servicio por su ID.
     */
    @Override
    public ServiceDelivery findById(Long idServiceDelivery) {
        Optional<ServiceDeliveryEntity> entity = repository.findById(idServiceDelivery);
        if (entity.isPresent()) {
            return mapper.toDomain(entity.get());
        }
        return null;
    }

    /**
     * Busca todos los servicios de entrega por placa.
     */
    @Override
    public List<ServiceDelivery> findByPlateNumber(String plateNumber) {
        return repository.findByPlate_PlateNumber(plateNumber).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Busca todos los servicios de entrega por mensajero.
     */
    @Override
    public List<ServiceDelivery> findByMessengerId(Long messengerId) {
        return repository.findByMessenger_IdEmployee(messengerId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Busca servicio activo (no eliminado).
     */
    @Override
    public ServiceDelivery findByIdActive(Long idServiceDelivery) {
        Optional<ServiceDeliveryEntity> entity = repository.findByIdServiceDeliveryAndDeletedFalse(idServiceDelivery);
        return entity.map(mapper::toDomain).orElse(null);
    }

    /**
     * Busca servicio activo por UUID público.
     */
    @Override
    public ServiceDelivery findByUuidActive(String uuid) {
        Optional<ServiceDeliveryEntity> entity = repository.findByUuidAndDeletedFalse(uuid);
        return entity.map(mapper::toDomain).orElse(null);
    }

    /**
     * Busca todos los servicios de entrega eliminados.
     */
    @Override
    public List<ServiceDelivery> findDeleted() {
        return repository.findByDeletedTrue().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Busca todos los servicios de entrega paginados.
     */
    @Override
    public Page<ServiceDelivery> findAllPaginated(String keyword, Boolean deleted,
            List<app.domain.model.enums.Status> statuses, Pageable pageable) {
        Page<ServiceDeliveryEntity> entityPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            entityPage = repository.searchAll(keyword, deleted, statuses, pageable);
        } else if (statuses != null && !statuses.isEmpty()) {
            entityPage = repository.findByDeletedAndCurrentStatusIn(deleted, statuses, pageable);
        } else {
            entityPage = repository.findByDeleted(deleted, pageable);
        }
        return entityPage.map(mapper::toDomain);
    }

    /**
     * Busca todos los servicios de entrega paginados por mensajero.
     */
    @Override
    public Page<ServiceDelivery> findByMessengerPaginated(Long messengerId, String keyword, Boolean deleted,
            List<app.domain.model.enums.Status> statuses, Pageable pageable) {
        Page<ServiceDeliveryEntity> entityPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            entityPage = repository.searchByMessenger(messengerId, keyword, deleted, statuses, pageable);
        } else if (statuses != null && !statuses.isEmpty()) {
            entityPage = repository.findByMessenger_IdEmployeeAndDeletedAndCurrentStatusIn(messengerId, deleted,
                    statuses,
                    pageable);
        } else {
            entityPage = repository.findByMessenger_IdEmployeeAndDeleted(messengerId, deleted,
                    pageable);
        }
        return entityPage.map(mapper::toDomain);
    }

    /**
     * Busca servicios en la papelera que hayan expirado antes de la fecha dada.
     */
    @Override
    public List<ServiceDelivery> findDeletedExpiredBefore(LocalDateTime date) {
        return repository.findByDeletedTrueAndDeletedAtBefore(date).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Elimina un servicio por su ID.
     */
    @Override
    public void hardDeleteById(Long idServiceDelivery) {
        repository.deleteById(idServiceDelivery);
    }

    /**
     * Elimina todos los servicios eliminados.
     */
    @Override
    public int hardDeleteAllDeleted() {
        List<ServiceDeliveryEntity> deletedEntities = repository.findByDeletedTrue();
        int count = deletedEntities.size();
        repository.deleteAll(deletedEntities);
        return count;
    }

    /**
     * Obtiene estadísticas diarias delegando a la consulta nativa del repositorio.
     */
    @Override
    public List<app.domain.model.DailyStatistics> findDailyStatsByMessenger(
            Long messengerId,
            java.time.LocalDate from,
            java.time.LocalDate to) {
        /**
         * Obtiene estadísticas diarias delegando a la consulta nativa del repositorio.
         */
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.plusDays(1).atStartOfDay();

        List<Object[]> rawResults = repository.findDailyStatsByMessenger(
                messengerId, fromDateTime, toDateTime);

        return rawResults.stream()
                .map(app.domain.model.DailyStatistics::fromRaw)
                .collect(Collectors.toList());
    }

    /**
     * Busca servicios de un mensajero que tengan actividad en una fecha específica.
     */
    @Override
    public List<ServiceDelivery> findByMessengerAndDate(Long messengerId, java.time.LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        return repository.findByMessengerAndDate(messengerId, startOfDay, endOfDay).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Busca servicios por número de placa filtrado por concesionario.
     */
    @Override
    public List<ServiceDelivery> findByPlateNumberAndDealershipId(String plateNumber, Long dealershipId) {
        return repository.findByPlate_PlateNumberAndDealership_IdDealershipAndDeletedFalse(plateNumber, dealershipId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Busca servicios por concesionario y una lista de estados específicos (no
     * eliminados).
     */
    @Override
    public List<ServiceDelivery> findByDealershipIdAndStatuses(Long dealershipId,
            List<app.domain.model.enums.Status> statuses) {
        return repository.findByDealership_IdDealershipAndCurrentStatusInAndDeletedFalse(dealershipId, statuses)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Busca servicios pendientes (no entregados) de un concesionario.
     */
    @Override
    public List<ServiceDelivery> findPendingByDealershipId(Long dealershipId) {
        List<app.domain.model.enums.Status> pendingStatuses = List.of(
                app.domain.model.enums.Status.ASSIGNED,
                app.domain.model.enums.Status.PENDING);
        return findByDealershipIdAndStatuses(dealershipId, pendingStatuses);
    }

    /**
     * Busca servicios por concesionario y una lista de estados específicos con
     * paginación.
     */
    @Override
    public Page<ServiceDelivery> findByDealershipIdAndStatusesPaginated(Long dealershipId,
            List<app.domain.model.enums.Status> statuses, Pageable pageable) {
        return repository
                .findByDealership_IdDealershipAndCurrentStatusInAndDeletedFalse(dealershipId, statuses, pageable)
                .map(mapper::toDomain);
    }
}
