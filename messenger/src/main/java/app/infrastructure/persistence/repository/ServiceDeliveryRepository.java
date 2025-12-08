package app.infrastructure.persistence.repository;

import app.domain.model.ServiceDelivery;
import app.domain.model.enums.Status;
import app.infrastructure.persistence.entities.ServiceDeliveryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceDeliveryRepository extends JpaRepository<ServiceDeliveryEntity, Long> {
    List<ServiceDelivery> findByStatus(Status status);

    List<ServiceDelivery> findByMessengerDocument(Long messengerDocument);

    List<ServiceDelivery> findByPlateNumber(String plateNumber);

    List<ServiceDelivery> findByDealershipId(Long dealershipId);
}