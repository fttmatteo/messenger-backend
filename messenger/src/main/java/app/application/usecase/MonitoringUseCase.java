package app.application.usecase;

import app.adapter.in.rest.system.MessengerActivityResponse;
import app.adapter.in.rest.system.MessengerActivityResponse.ActivityEvent;
import app.domain.model.Employee;
import app.domain.model.TimelineEvent;
import app.domain.ports.TimelineEventPort;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Caso de uso para la obtención de línea de tiempo del panel de monitoreo usando CQRS.
 */
@Service
public class MonitoringUseCase {

    private final TimelineEventPort timelineEventPort;
    private final EmployeeUseCase employeeUseCase;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MonitoringUseCase.class);

    public MonitoringUseCase(TimelineEventPort timelineEventPort, EmployeeUseCase employeeUseCase) {
        this.timelineEventPort = timelineEventPort;
        this.employeeUseCase = employeeUseCase;
    }

    /**
     * Obtiene la actividad (timeline) de un mensajero en una fecha específica desde el Read Model.
     */
    public MessengerActivityResponse getDailyActivity(String messengerUuid, LocalDate date, Pageable pageable) {
        logger.info("Consultando línea de tiempo de monitoreo para la fecha {}", date);
        
        Employee messenger = employeeUseCase.findByUuid(messengerUuid);
        
        List<TimelineEvent> events = timelineEventPort.findByMessengerIdAndDate(messenger.getIdEmployee(), date);
        
        List<ActivityEvent> timeline = events.stream()
                .map(this::mapToActivityEvent)
                .collect(Collectors.toList());

        logger.info("Se encontraron {} eventos en la línea de tiempo", timeline.size());
        
        return new MessengerActivityResponse(timeline);
    }

    private ActivityEvent mapToActivityEvent(TimelineEvent event) {
        ActivityEvent activityEvent = new ActivityEvent();
        activityEvent.setId(event.getId());
        activityEvent.setStatus(event.getStatus() != null ? event.getStatus().name() : null);
        activityEvent.setTimestamp(event.getTimestamp());
        activityEvent.setPlateNumber(event.getPlateNumber());
        activityEvent.setDealershipName(event.getDealershipName());
        activityEvent.setLatitude(event.getLatitude());
        activityEvent.setLongitude(event.getLongitude());
        activityEvent.setChangedByName(event.getChangedByName());
        activityEvent.setChangedByRole(event.getChangedByRole());
        return activityEvent;
    }
}
