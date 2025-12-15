package app.application.usecase.tracking;

import app.domain.model.TrackingHistory;
import app.domain.ports.TrackingPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Caso de uso para consultar el historial de rastreo de mensajeros.
 * 
 * Permite recuperar datos históricos de ubicación para auditoría, análisis de
 * rutas
 * o verificación de recorridos realizados en fechas específicas.
 */
@Service
public class GetTrackingHistory {

    @Autowired
    private TrackingPort trackingPort;

    /**
     * Consulta el historial de ubicaciones de un mensajero específico en una fecha
     * dada.
     * 
     * @param messengerId ID del mensajero a consultar.
     * @param date        Fecha del recorrido.
     * @return Lista de puntos de rastreo registrados ese día.
     */
    public List<TrackingHistory> byMessengerAndDate(Long messengerId, LocalDate date) {
        return trackingPort.getHistoryByMessenger(messengerId, date);
    }

    /**
     * Consulta el historial de ubicaciones asociado a un servicio de entrega
     * particular.
     * 
     * @param serviceDeliveryId ID del servicio de entrega.
     * @return Lista de puntos de rastreo vinculados a ese servicio.
     */
    public List<TrackingHistory> byService(Long serviceDeliveryId) {
        return trackingPort.getHistoryByService(serviceDeliveryId);
    }
}
