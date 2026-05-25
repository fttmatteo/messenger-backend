package app.application.usecase;

import app.adapter.in.rest.system.MessengerActivityResponse;
import app.domain.model.Employee;
import app.domain.model.TimelineEvent;
import app.domain.model.enums.Status;
import app.domain.ports.TimelineEventPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class MonitoringUseCaseTest {

    @Mock
    private TimelineEventPort timelineEventPort;

    @Mock
    private EmployeeUseCase employeeUseCase;

    @InjectMocks
    private MonitoringUseCase monitoringUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Debe retornar la actividad del mensajero desde el modelo de lectura")
    void shouldReturnActivityFromReadModel() {
        String uuid = "uuid-123";
        LocalDate date = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);

        Employee messenger = new Employee();
        messenger.setIdEmployee(1L);
        when(employeeUseCase.findByUuid(uuid)).thenReturn(messenger);

        TimelineEvent event = new TimelineEvent();
        event.setId(100L);
        event.setStatus(Status.DELIVERED);
        event.setTimestamp(LocalDateTime.now());
        event.setPlateNumber("ABC-123");

        when(timelineEventPort.findByMessengerIdAndDate(1L, date)).thenReturn(List.of(event));

        MessengerActivityResponse response = monitoringUseCase.getDailyActivity(uuid, date, pageable);

        assertNotNull(response);
        assertNotNull(response.getTimeline());
        assertEquals(1, response.getTimeline().size());
        assertEquals(100L, response.getTimeline().get(0).getId());
        assertEquals("DELIVERED", response.getTimeline().get(0).getStatus());
        assertEquals("ABC-123", response.getTimeline().get(0).getPlateNumber());
    }
}
