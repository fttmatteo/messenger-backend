package app.adapter.out.tracking;

import app.domain.model.LiveTracking;
import app.domain.model.TrackingHistory;
import app.domain.model.enums.TrackingStatus;
import app.domain.ports.TrackingPort;
import app.infrastructure.persistence.entities.TrackingHistoryEntity;
import app.infrastructure.persistence.repository.TrackingHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Adaptador de salida para rastreo de mensajeros en tiempo real e histórico.
 * 
 * Este adaptador implementa TrackingPort usando una arquitectura híbrida que
 * combina
 * Redis para datos en tiempo real y JPA para persistencia histórica,
 * optimizando
 * rendimiento y almacenamiento.
 * 
 * Arquitectura de almacenamiento:
 * - Redis: Ubicaciones en tiempo real con TTL automático (5 minutos)
 * - Ventajas: Acceso ultra-rápido, expiración automática, bajo overhead
 * - Uso: Tracking activo de mensajeros en servicio
 * 
 * - JPA/PostgreSQL: Historial de ubicaciones para reportes y auditoría
 * - Ventajas: Persistencia permanente, consultas complejas, integridad
 * referencial
 * - Uso: Análisis de rutas, reportes, evidencia de entregas
 * 
 * Funcionalidades implementadas:
 * - saveLiveLocation: Guarda ubicación actual en Redis con TTL
 * - getLastLocation: Obtiene última ubicación conocida de un mensajero
 * - getAllActiveMessengers: Lista todos los mensajeros activos (con ubicación
 * reciente)
 * - saveTrackingHistory: Persiste punto de tracking en historial
 * - getHistoryByMessenger: Obtiene historial de un mensajero en una fecha
 * - getHistoryByService: Obtiene historial asociado a un servicio específico
 * 
 * Configuración de Redis:
 * - Prefijo de keys: "tracking:messenger:{messengerId}"
 * - TTL: 5 minutos (configurable)
 * - Serialización: JSON para objetos LiveTracking
 * 
 * @see app.domain.ports.TrackingPort
 * @see app.infrastructure.persistence.repository.TrackingHistoryRepository
 * @see TrackingMapper
 * @see app.adapter.out.tracking.config.RedisConfig
 */
@Component
public class TrackingAdapter implements TrackingPort {

    private static final Logger logger = LoggerFactory.getLogger(TrackingAdapter.class);
    private static final String TRACKING_KEY_PREFIX = "tracking:messenger:";
    private static final long TRACKING_TTL_MINUTES = 5; // Expira después de 5 minutos sin actualizar

    @Autowired
    private RedisTemplate<String, LiveTracking> redisTemplate;
    @Autowired
    private TrackingHistoryRepository historyRepository;
    @Autowired
    private TrackingMapper mapper;

    /**
     * Guarda la ubicación actual de un mensajero en Redis con TTL.
     * 
     * La ubicación se almacena con expiración automática de 5 minutos.
     * Si el mensajero no envía actualizaciones, su ubicación desaparece
     * automáticamente.
     * 
     * @param tracking Datos de ubicación en tiempo real del mensajero
     */
    @Override
    public void saveLiveLocation(LiveTracking tracking) {
        if (tracking == null || tracking.getMessengerId() == null) {
            logger.warn("Intento de guardar tracking nulo o sin messengerId");
            return;
        }

        String key = TRACKING_KEY_PREFIX + tracking.getMessengerId();
        tracking.setLastUpdate(LocalDateTime.now());

        // Si el estado es OFFLINE, eliminar de Redis inmediatamente para que no
        // aparezca como activo
        if (tracking.getStatus() == TrackingStatus.OFFLINE) {
            redisTemplate.delete(key);
            logger.info("Mensajero {} se ha desconectado (OFFLINE), eliminando de Redis", tracking.getMessengerId());
            return;
        }

        logger.debug("Guardando ubicación en vivo para mensajero {}: ({}, {})",
                tracking.getMessengerId(),
                tracking.getCurrentLocation().getLatitude(),
                tracking.getCurrentLocation().getLongitude());

        // Guardar en Redis con TTL
        redisTemplate.opsForValue().set(key, tracking, TRACKING_TTL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * Obtiene la última ubicación conocida de un mensajero desde Redis.
     * 
     * @param messengerId ID del mensajero
     * @return Última ubicación si existe y no ha expirado, null en caso contrario
     */
    @Override
    public LiveTracking getLastLocation(Long messengerId) {
        if (messengerId == null) {
            return null;
        }

        String key = TRACKING_KEY_PREFIX + messengerId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Obtiene lista de todos los mensajeros activos con ubicación reciente.
     * 
     * Un mensajero se considera activo si:
     * - Tiene una ubicación en Redis (no ha expirado el TTL)
     * - Su estado es ACTIVE
     * 
     * @return Lista de ubicaciones de mensajeros activos
     */
    @Override
    public List<LiveTracking> getAllActiveMessengers() {
        Set<String> keys = redisTemplate.keys(TRACKING_KEY_PREFIX + "*");

        if (keys == null || keys.isEmpty()) {
            logger.debug("No hay mensajeros activos en Redis");
            return new ArrayList<>();
        }

        logger.debug("Obteniendo {} mensajeros activos de Redis", keys.size());

        List<LiveTracking> activeMessengers = new ArrayList<>();
        for (String key : keys) {
            LiveTracking tracking = redisTemplate.opsForValue().get(key);
            if (tracking != null && tracking.getStatus() == TrackingStatus.ACTIVE) {
                activeMessengers.add(tracking);
            }
        }

        return activeMessengers;
    }

    /**
     * Persiste un punto de tracking en el historial (base de datos).
     * 
     * Usado para mantener registro permanente de rutas y ubicaciones
     * para reportes, análisis y evidencia de entregas.
     * 
     * @param history Punto de tracking a guardar
     * @return Historial guardado con ID generado
     */
    @Override
    public TrackingHistory saveTrackingHistory(TrackingHistory history) {
        if (history == null) {
            return null;
        }

        logger.debug("Guardando punto de historial para mensajero {} en servicio {}",
                history.getMessengerId(), history.getServiceDeliveryId());

        TrackingHistoryEntity entity = mapper.toEntity(history);
        TrackingHistoryEntity saved = historyRepository.save(entity);
        return mapper.toDomain(saved);
    }

    /**
     * Obtiene el historial de ubicaciones de un mensajero en una fecha específica.
     * 
     * Útil para generar reportes de rutas diarias, análisis de desempeño
     * y verificación de entregas.
     * 
     * @param messengerId ID del mensajero
     * @param date        Fecha a consultar
     * @return Lista de puntos de tracking del día ordenados por tiempo
     */
    @Override
    public List<TrackingHistory> getHistoryByMessenger(Long messengerId, LocalDate date) {
        if (messengerId == null || date == null) {
            return new ArrayList<>();
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<TrackingHistoryEntity> entities = historyRepository
                .findByMessengerIdAndRecordedAtBetween(messengerId, startOfDay, endOfDay);

        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el historial de ubicaciones asociado a un servicio de entrega.
     * 
     * Permite rastrear la ruta completa del mensajero durante una entrega
     * específica,
     * útil para evidencia y resolución de disputas.
     * 
     * @param serviceDeliveryId ID del servicio de entrega
     * @return Lista de puntos de tracking del servicio
     */
    @Override
    public List<TrackingHistory> getHistoryByService(Long serviceDeliveryId) {
        if (serviceDeliveryId == null) {
            return new ArrayList<>();
        }

        List<TrackingHistoryEntity> entities = historyRepository
                .findByServiceDeliveryId(serviceDeliveryId);

        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
