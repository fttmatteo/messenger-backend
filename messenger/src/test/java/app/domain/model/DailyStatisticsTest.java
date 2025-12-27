package app.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DailyStatisticsTest {

    @Test
    @DisplayName("Should create DailyStatistics from raw Object array with sql.Date")
    /**
     * Prueba la creación desde un arreglo de objetos (resultado de query nativa)
     * usando java.sql.Date.
     */
    void shouldCreateFromRawSqlDate() {
        // Given
        java.sql.Date sqlDate = java.sql.Date.valueOf("2023-10-05");
        Object[] row = {
                sqlDate,
                10L, // assigned
                5L, // delivered
                2L, // returned
                1L, // canceled
                18L // total
        };

        // When
        DailyStatistics stats = DailyStatistics.fromRaw(row);

        // Then
        assertEquals(LocalDate.of(2023, 10, 5), stats.date());
        assertEquals(10L, stats.assigned());
        assertEquals(5L, stats.delivered());
        assertEquals(2L, stats.returned());
        assertEquals(1L, stats.canceled());
        assertEquals(18L, stats.total());
    }

    @Test
    @DisplayName("Should create DailyStatistics from raw Object array with LocalDate")
    /**
     * Prueba la creación desde un arreglo con LocalDate y diferentes tipos
     * numéricos.
     */
    void shouldCreateFromRawLocalDate() {
        // Given
        LocalDate localDate = LocalDate.of(2023, 10, 5);
        Object[] row = {
                localDate,
                10, // Integer
                5, // Integer
                2L, // Long
                1.0, // Double - Number
                18L
        };

        // When
        DailyStatistics stats = DailyStatistics.fromRaw(row);

        // Then
        assertEquals(LocalDate.of(2023, 10, 5), stats.date());
        assertEquals(10L, stats.assigned()); // Checking conversion from Integer
        assertEquals(5L, stats.delivered());
        assertEquals(2L, stats.returned());
        assertEquals(1L, stats.canceled()); // Checking conversion from Double
        assertEquals(18L, stats.total());
    }
}
