package app.application.usecase.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import java.util.Optional;
import app.domain.model.Employee;
import app.domain.model.LiveTracking;
import app.domain.model.Location;
import app.domain.model.TrackingHistory;
import app.domain.model.enums.TrackingStatus;
import app.domain.ports.EmployeePort;
import app.domain.ports.TrackingPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de UpdateLiveTrackingUseCase")
class UpdateLiveTrackingUseCaseTest {

    @Mock
    private TrackingPort trackingPort;

    @Mock
    private EmployeePort employeePort;

    @InjectMocks
    private UpdateLiveTrackingUseCase useCase;

    @Test
    @DisplayName("Debe guardar ubicación en vivo e historial con buena precisión")
    void shouldSaveLiveLocationAndHistoryWithGoodAccuracy() {
        LiveTracking incoming = new LiveTracking();
        incoming.setMessengerId(10L);
        incoming.setCurrentLocation(new Location(4.60, -74.08, null, 50.0));
        incoming.setSpeed(50.0);

        LiveTracking result = useCase.execute(incoming);
        assertNotNull(result.getLastUpdate());
        assertEquals(TrackingStatus.ACTIVE, result.getStatus());

        verify(trackingPort, times(1)).saveLiveLocation(incoming);
        verify(trackingPort, times(1)).saveTrackingHistory(any(TrackingHistory.class));
    }

    @Test
    @DisplayName("No debe guardar historial si la precisión es demasiado baja")

    void shouldNotSaveHistoryIfAccuracyTooLow() {
        LiveTracking incoming = new LiveTracking();
        incoming.setMessengerId(10L);
        incoming.setCurrentLocation(new Location(4.60, -74.08, null, 150.0));
        incoming.setSpeed(50.0);
        useCase.execute(incoming);
        verify(trackingPort, times(1)).saveLiveLocation(incoming);
        verify(trackingPort, never()).saveTrackingHistory(any(TrackingHistory.class));
    }

    @Test
    @DisplayName("Debe guardar solo ubicación en vivo si falta la ubicación")

    void shouldSaveOnlyLiveLocationIfLocationMissing() {
        LiveTracking incoming = new LiveTracking();
        incoming.setMessengerId(10L);
        incoming.setCurrentLocation(null);

        useCase.execute(incoming);

        verify(trackingPort, times(1)).saveLiveLocation(incoming);
        verify(trackingPort, never()).saveTrackingHistory(any(TrackingHistory.class));
    }

    @Test
    @DisplayName("Debe actualizar solo el latido con executeHeartbeat")

    void shouldUpdateOnlyHeartbeatWithExecuteHeartbeat() {
        LiveTracking heartbeat = new LiveTracking();
        heartbeat.setMessengerId(10L);

        when(trackingPort.getLastLocation(10L)).thenReturn(Optional.empty());

        LiveTracking result = useCase.executeHeartbeat(heartbeat);

        assertNotNull(result.getLastHeartbeat());
        assertEquals(TrackingStatus.ACTIVE, result.getStatus());

        verify(trackingPort, times(1)).saveLiveLocation(any(LiveTracking.class));
        verify(trackingPort, never()).saveTrackingHistory(any(TrackingHistory.class));
    }

    @Test
    @DisplayName("No debe guardar historial si la precisión está en el límite")

    void shouldNotSaveHistoryOnAccuracyBorderline() {
        LiveTracking incoming = new LiveTracking();
        incoming.setMessengerId(10L);
        incoming.setCurrentLocation(new Location(4.60, -74.08, null, 100.1));
        incoming.setSpeed(50.0);

        useCase.execute(incoming);

        verify(trackingPort, times(1)).saveLiveLocation(incoming);
        verify(trackingPort, never()).saveTrackingHistory(any(TrackingHistory.class));
    }

    @Test
    @DisplayName("Debe manejar el latido sin ubicación previa")

    void shouldHandleHeartbeatWithNoPriorLocation() {
        LiveTracking heartbeat = new LiveTracking();
        heartbeat.setMessengerId(10L);

        when(trackingPort.getLastLocation(10L)).thenReturn(Optional.empty());

        LiveTracking result = useCase.executeHeartbeat(heartbeat);

        assertNotNull(result.getLastHeartbeat());
        verify(trackingPort).saveLiveLocation(any(LiveTracking.class));
    }

    @Test
    @DisplayName("Debe preservar la ubicación existente en el latido")

    void shouldPreserveExistingLocationOnHeartbeat() {
        LiveTracking existing = new LiveTracking();
        existing.setMessengerId(10L);
        existing.setCurrentLocation(new Location(4.60, -74.08));
        existing.setMessengerName("Test User");

        LiveTracking heartbeat = new LiveTracking();
        heartbeat.setMessengerId(10L);

        when(trackingPort.getLastLocation(10L)).thenReturn(Optional.of(existing));

        LiveTracking result = useCase.executeHeartbeat(heartbeat);

        assertNotNull(result.getCurrentLocation());
        assertEquals("Test User", result.getMessengerName());
        assertNotNull(result.getLastHeartbeat());
    }

    @Test
    @DisplayName("Debe obtener nombre del caché y evitar base de datos")

    void shouldFetchNameFromCacheAndAvoidDb() {
        LiveTracking incoming = new LiveTracking();
        incoming.setMessengerId(10L);
        incoming.setCurrentLocation(new Location(4.60, -74.08));

        when(trackingPort.getMessengerName(10L)).thenReturn(Optional.of("Cached Name"));

        LiveTracking result = useCase.execute(incoming);

        assertEquals("Cached Name", result.getMessengerName());
        verify(employeePort, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Debe guardar nombre en el caché al obtener de la base de datos")

    void shouldSaveNameInCacheWhenFetchedFromDb() {
        LiveTracking incoming = new LiveTracking();
        incoming.setMessengerId(10L);
        incoming.setCurrentLocation(new Location(4.60, -74.08));

        when(trackingPort.getMessengerName(10L)).thenReturn(Optional.empty());
        Employee employee = new Employee();
        employee.setFullName("DB Name");
        when(employeePort.findById(10L)).thenReturn(employee);

        LiveTracking result = useCase.execute(incoming);

        assertEquals("DB Name", result.getMessengerName());
        verify(trackingPort).saveMessengerName(10L, "DB Name");
    }
}
