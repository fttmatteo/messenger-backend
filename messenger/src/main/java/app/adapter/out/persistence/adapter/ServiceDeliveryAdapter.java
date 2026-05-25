package app.adapter.out.persistence.adapter;

import app.domain.model.ServiceDelivery;
import app.domain.ports.ServiceDeliveryPort;
import app.adapter.out.persistence.entities.ServiceDeliveryEntity;
import app.adapter.out.persistence.mapper.ServiceDeliveryMapper;
import app.adapter.out.persistence.repository.ServiceDeliveryRepository;
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
     * Busca servicio por UUID público incluyendo eliminados.
     */
    @Override
    public ServiceDelivery findByUuidIncludingDeleted(String uuid) {
        Optional<ServiceDeliveryEntity> entity = repository.findByUuid(uuid);
        return entity.map(mapper::toDomain).orElse(null);
    }

    /**
     * Busca todos los servicios de entrega eliminados con paginación.
     */
    @Override
    public Page<ServiceDelivery> findDeleted(Pageable pageable) {
        return repository.findByDeleted(true, pageable)
                .map(mapper::toDomain);
    }

    /**
     * Busca todos los servicios de entrega paginados.
     */
    @Override
    public Page<ServiceDelivery> findAllPaginated(String keyword, Boolean deleted,
            List<app.domain.model.enums.Status> statuses, Pageable pageable) {
        Page<ServiceDeliveryEntity> entityPage;
        List<String> statusStrings = (statuses != null && !statuses.isEmpty())
                ? statuses.stream().map(Enum::name).collect(Collectors.toList())
                : null;

        if (keyword != null && !keyword.trim().isEmpty()) {
            String keywordLike = "%" + keyword + "%";
            String keywordBoolean = keyword.trim() + "*";
            entityPage = repository.searchAll(keywordLike, keywordBoolean, deleted, statusStrings, pageable);
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
        List<String> statusStrings = (statuses != null && !statuses.isEmpty())
                ? statuses.stream().map(Enum::name).collect(Collectors.toList())
                : null;

        if (keyword != null && !keyword.trim().isEmpty()) {
            String keywordLike = "%" + keyword + "%";
            String keywordBoolean = keyword.trim() + "*";
            entityPage = repository.searchByMessenger(messengerId, keywordLike, keywordBoolean, deleted, statusStrings,
                    pageable);
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
     * Busca servicios por número de placa filtrado por concesionario con paginación.
     */
    @Override
    public Page<ServiceDelivery> findByPlateAndDealershipPaginated(String plateNumber, Long dealershipId,
            Pageable pageable) {
        if (dealershipId == null) {
            return repository.findByPlate_PlateNumberAndDeletedFalse(plateNumber, pageable)
                    .map(mapper::toDomain);
        }
        return repository.findByPlate_PlateNumberAndDealership_IdDealershipAndDeletedFalse(plateNumber, dealershipId,
                pageable).map(mapper::toDomain);
    }

    /**
     * Busca servicios por concesionario y una lista de estados específicos con
     * paginación.
     */
    @Override
    public Page<ServiceDelivery> findByDealershipIdAndStatusesPaginated(Long dealershipId,
            List<app.domain.model.enums.Status> statuses, Pageable pageable) {
        if (dealershipId == null) {
            return repository.findByCurrentStatusInAndDeletedFalse(statuses, pageable)
                    .map(mapper::toDomain);
        }
        return repository
            .findByDealership_IdDealershipAndCurrentStatusInAndDeletedFalse(dealershipId, statuses, pageable)
            .map(mapper::toDomain);
    }
}
