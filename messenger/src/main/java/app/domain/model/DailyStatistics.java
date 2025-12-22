package app.domain.model;

import java.time.LocalDate;

/**
 * Estadísticas diarias de servicios para un mensajero.
 * Modelo de dominio inmutable para reportes.
 */
public record DailyStatistics(
        LocalDate date,
        long assigned,
        long delivered,
        long returned,
        long canceled,
        long total) {
    /**
     * Crea una instancia desde valores raw de la consulta.
     */
    public static DailyStatistics fromRaw(Object[] row) {
        return new DailyStatistics(
                row[0] instanceof java.sql.Date d ? d.toLocalDate() : (LocalDate) row[0],
                ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue(),
                ((Number) row[3]).longValue(),
                ((Number) row[4]).longValue(),
                ((Number) row[5]).longValue());
    }
}
