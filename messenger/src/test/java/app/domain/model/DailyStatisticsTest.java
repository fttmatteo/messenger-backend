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
        java.sql.Date sqlDate = java.sql.Date.valueOf("2023-10-05");
        Object[] row = {
                sqlDate,
                10L,
                5L,
                2L,
                1L,
                18L
        };

        DailyStatistics stats = DailyStatistics.fromRaw(row);

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
        LocalDate localDate = LocalDate.of(2023, 10, 5);
        Object[] row = {
                localDate,
                10,
                5,
                2L,
                1.0,
                18L
        };

        DailyStatistics stats = DailyStatistics.fromRaw(row);

        assertEquals(LocalDate.of(2023, 10, 5), stats.date());
        assertEquals(10L, stats.assigned());
        assertEquals(5L, stats.delivered());
        assertEquals(2L, stats.returned());
        assertEquals(1L, stats.canceled());
        assertEquals(18L, stats.total());
    }
}
