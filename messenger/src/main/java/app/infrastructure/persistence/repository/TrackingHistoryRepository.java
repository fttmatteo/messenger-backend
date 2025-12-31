package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.TrackingHistoryEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio Spring Data para historial de tracking GPS.
 */
@Repository
public interface TrackingHistoryRepository extends CrudRepository<TrackingHistoryEntity, Long> {

        /**
         * Busca el historial de tracking por mensajero y rango de fechas
         */
        List<TrackingHistoryEntity> findByMessengerIdAndRecordedAtBetween(
                        Long messengerId,
                        LocalDateTime start,
                        LocalDateTime end);

        /**
         * Busca el historial de tracking por ID de servicio de entrega
         */
        List<TrackingHistoryEntity> findByServiceDeliveryId(Long serviceDeliveryId);

        /**
         * Busca los 10 últimos registros de tracking por mensajero, ordenados por fecha
         */
        List<TrackingHistoryEntity> findTop10ByMessengerIdOrderByRecordedAtDesc(Long messengerId);

        /**
         * Busca el primer registro de tracking por mensajero, ordenado por fecha
         */
        TrackingHistoryEntity findFirstByMessengerIdOrderByRecordedAtDesc(Long messengerId);

        /**
         * Cuenta el número de registros de tracking por mensajero y rango de fechas
         */
        long countByMessengerIdAndRecordedAtBetween(
                        Long messengerId,
                        LocalDateTime start,
                        LocalDateTime end);
}
