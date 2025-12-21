package app.infrastructure.persistence.repository;

import app.domain.model.enums.Status;
import app.infrastructure.persistence.entities.ServiceDeliveryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para servicios de entrega.
 */
@Repository
public interface ServiceDeliveryRepository extends JpaRepository<ServiceDeliveryEntity, Long> {

    List<ServiceDeliveryEntity> findByCurrentStatus(Status currentStatus);

    List<ServiceDeliveryEntity> findByMessenger_Document(Long messengerDocument);

    List<ServiceDeliveryEntity> findByPlate_PlateNumber(String plateNumber);

    List<ServiceDeliveryEntity> findByDealership_IdDealership(Long dealershipId);

    // Métodos para soft delete (papelera)
    List<ServiceDeliveryEntity> findByDeletedFalse();

    Optional<ServiceDeliveryEntity> findByIdServiceDeliveryAndDeletedFalse(Long id);

    List<ServiceDeliveryEntity> findByDeletedTrue();

    List<ServiceDeliveryEntity> findByDeletedTrueAndDeletedAtBefore(LocalDateTime date);

    List<ServiceDeliveryEntity> findByPlate_PlateNumberAndDeletedFalse(String plateNumber);
}