package app.adapter.in.rest.response;

import java.time.LocalDate;

public record DailyStatsResponse(
        LocalDate date,
        long assigned,
        long delivered,
        long returned,
        long canceled,
        long total) {
}
