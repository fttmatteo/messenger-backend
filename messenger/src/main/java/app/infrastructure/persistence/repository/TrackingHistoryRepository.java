package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.TrackingHistoryEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para operaciones de persistencia de historial de tracking.
 */
@Repository
public interface TrackingHistoryRepository extends CrudRepository<TrackingHistoryEntity, Long> {

    /**
     * Busca el historial de ubicaciones de un mensajero en un rango de fechas.
     * Usado para obtener la ruta del día.
     */
    List<TrackingHistoryEntity> findByMessengerIdAndRecordedAtBetween(
            Long messengerId,
            LocalDateTime start,
            LocalDateTime end);

    /**
     * Busca el historial de ubicaciones asociadas a un servicio de entrega
     * específico.
     * Útil para reconstruir la ruta de una entrega.
     */
    List<TrackingHistoryEntity> findByServiceDeliveryId(Long serviceDeliveryId);

    /**
     * Busca las últimas N ubicaciones de un mensajero.
     * Ordenadas por fecha descendente.
     */
    List<TrackingHistoryEntity> findTop10ByMessengerIdOrderByRecordedAtDesc(Long messengerId);

    /**
     * Cuenta las ubicaciones registradas en un día para un mensajero.
     * Útil para estadísticas.
     */
    long countByMessengerIdAndRecordedAtBetween(
            Long messengerId,
            LocalDateTime start,
            LocalDateTime end);
}
