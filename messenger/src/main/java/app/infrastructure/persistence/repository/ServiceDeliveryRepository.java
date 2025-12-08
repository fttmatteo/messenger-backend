package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.ServiceDeliveryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceDeliveryRepository extends JpaRepository<ServiceDeliveryEntity, Long> {
    List<ServiceDeliveryEntity> findByMessenger_IdEmployee(Long idEmployee);

    List<ServiceDeliveryEntity> findByPlate_PlateNumber(String plateNumber);
}