package app.adapter.in.rest.response;

import java.time.LocalDate;

/**
 * Respuesta que encapsula las estadísticas diarias de los servicios de entrega.
 */
public record DailyStatsResponse(
                LocalDate date,
                long assigned,
                long delivered,
                long returned,
                long canceled,
                long total) {
}
