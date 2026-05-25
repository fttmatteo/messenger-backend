package app.domain.ports;

import app.domain.model.TimelineEvent;
import java.time.LocalDate;
import java.util.List;

public interface TimelineEventPort {
    TimelineEvent save(TimelineEvent event);
    List<TimelineEvent> findByMessengerIdAndDate(Long messengerId, LocalDate date);
}
