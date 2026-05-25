package app.adapter.out.persistence.listener;

import app.domain.events.PlateStatusChangedEvent;
import app.domain.model.Employee;
import app.domain.model.ServiceDelivery;
import app.domain.model.StatusHistory;
import app.domain.model.TimelineEvent;
import app.domain.model.enums.Role;
import app.domain.model.enums.Status;
import app.domain.ports.TimelineEventPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

class TimelineEventProjectorTest {

    @Mock
    private TimelineEventPort timelineEventPort;

    @InjectMocks
    private TimelineEventProjector projector;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Debe proyectar el evento de línea de tiempo exitosamente")
    void shouldProjectTimelineEventSuccessfully() {
        Employee messenger = new Employee();
        messenger.setIdEmployee(1L);
        messenger.setFullName("John Doe");
        messenger.setRole(Role.MESSENGER);

        app.domain.model.Plate plate = new app.domain.model.Plate();
        plate.setPlateNumber("ABC-123");

        app.domain.model.Dealership dealership = new app.domain.model.Dealership();
        dealership.setName("Test Dealer");

        ServiceDelivery service = new ServiceDelivery();
        service.setIdServiceDelivery(10L);
        service.setMessenger(messenger);
        service.setPlate(plate);
        service.setDealership(dealership);

        StatusHistory history = new StatusHistory();
        history.setChangeDate(LocalDateTime.now());
        history.setChangedBy(messenger);
        history.setDeliveryLatitude(4.6097);
        history.setDeliveryLongitude(-74.0817);
        history.setNewStatus(Status.DELIVERED);
        service.addHistory(history);

        PlateStatusChangedEvent event = new PlateStatusChangedEvent(service, Status.ASSIGNED, Status.DELIVERED);

        projector.onPlateStatusChanged(event);

        ArgumentCaptor<TimelineEvent> captor = ArgumentCaptor.forClass(TimelineEvent.class);
        verify(timelineEventPort, times(1)).save(captor.capture());

        TimelineEvent savedEvent = captor.getValue();
        assertNotNull(savedEvent);
        assertEquals(1L, savedEvent.getMessengerId());
        assertEquals(Status.DELIVERED, savedEvent.getStatus());
        assertEquals("John Doe", savedEvent.getChangedByName());
        assertEquals("MESSENGER", savedEvent.getChangedByRole());
        assertEquals(4.6097, savedEvent.getLatitude());
        assertEquals(-74.0817, savedEvent.getLongitude());
    }
}
