package app.infrastructure.persistence.repository;

import app.infrastructure.persistence.entities.TrackingHistoryEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para operaciones de persistencia de historial de tracking.
 * 
 * Gestiona el almacenamiento y consultas del historial de ubicaciones GPS
 * de los mensajeros durante sus rutas de entrega. Permite:
 * - Rastrear rutas por rango de fechas
 * - Obtener histórico de ubicaciones por servicio
 * - Consultar últimas posiciones conocidas
 * - Generar estadísticas de tracking
 * 
 * Este repositorio soporta análisis de rutas, monitoreo en tiempo real
 * y auditoría de movimientos para el sistema de mensajería.
 */
@Repository
public interface TrackingHistoryRepository extends CrudRepository<TrackingHistoryEntity, Long> {

        /**
         * Busca el historial de ubicaciones de un mensajero en un rango de fechas.
         * 
         * Útil para:
         * - Obtener la ruta completa del día
         * - Generar reportes de actividad
         * - Análisis de rutas realizadas
         * - Auditoría de movimientos
         * 
         * @param messengerId ID del mensajero
         * @param start       Fecha y hora de inicio del rango (inclusive)
         * @param end         Fecha y hora de fin del rango (inclusive)
         * @return Lista ordenada cronológicamente de ubicaciones registradas
         */
        List<TrackingHistoryEntity> findByMessengerIdAndRecordedAtBetween(
                        Long messengerId,
                        LocalDateTime start,
                        LocalDateTime end);

        /**
         * Busca el historial de ubicaciones asociadas a un servicio de entrega
         * específico.
         * 
         * Permite reconstruir la ruta exacta que tomó el mensajero para completar
         * una entrega en particular, desde la asignación hasta la finalización.
         * 
         * Útil para:
         * - Reconstruir la ruta de una entrega
         * - Auditoría de servicios
         * - Resolución de reclamaciones
         * - Análisis de tiempos de entrega
         * 
         * @param serviceDeliveryId ID del servicio de entrega
         * @return Lista de ubicaciones registradas durante el servicio
         */
        List<TrackingHistoryEntity> findByServiceDeliveryId(Long serviceDeliveryId);

        /**
         * Busca las últimas 10 ubicaciones de un mensajero.
         * 
         * Devuelve las posiciones más recientes ordenadas de la más reciente
         * a la más antigua. Útil para mostrar el tracking en tiempo real.
         * 
         * Aplicaciones:
         * - Monitoreo en tiempo real
         * - Visualización de ruta reciente
         * - Estimación de tiempo de llegada
         * - Dashboard de mensajeros activos
         * 
         * @param messengerId ID del mensajero
         * @return Lista de hasta 10 ubicaciones más recientes (ordenadas desc)
         */
        List<TrackingHistoryEntity> findTop10ByMessengerIdOrderByRecordedAtDesc(Long messengerId);

        /**
         * Cuenta las ubicaciones registradas en un rango de tiempo para un mensajero.
         * 
         * Útil para generar estadísticas sobre:
         * - Actividad diaria del mensajero
         * - Frecuencia de actualizaciones GPS
         * - Métricas de desempeño
         * - Detección de anomalías (muy pocas o demasiadas actualizaciones)
         * 
         * @param messengerId ID del mensajero
         * @param start       Fecha y hora de inicio del rango (inclusive)
         * @param end         Fecha y hora de fin del rango (inclusive)
         * @return Número total de ubicaciones registradas en el período
         */
        long countByMessengerIdAndRecordedAtBetween(
                        Long messengerId,
                        LocalDateTime start,
                        LocalDateTime end);
}
