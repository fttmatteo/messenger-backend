package app.application.usecase.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import app.domain.model.TrackingHistory;
import app.domain.ports.TrackingPort;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetTrackingHistoryUseCase Unit Tests")
class GetTrackingHistoryUseCaseTest {

    @Mock
    private TrackingPort trackingPort;

    @InjectMocks
    private GetTrackingHistoryUseCase getTrackingHistory;

    @Test
    @DisplayName("Debe consultar por mensajero y fecha")
    /**
     * Verifica recuperación de historial filtrado por mensajero y fecha.
     */
    void shouldGetByMessengerAndDate() {
        Long messengerId = 1L;
        LocalDate date = LocalDate.now();
        List<TrackingHistory> expected = Collections.emptyList();

        when(trackingPort.getHistoryByMessenger(messengerId, date)).thenReturn(expected);

        List<TrackingHistory> result = getTrackingHistory.byMessengerAndDate(messengerId, date);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Debe consultar por servicio")
    /**
     * Verifica recuperación de historial asociado a un servicio específico.
     */
    void shouldGetByService() {
        Long serviceId = 100L;
        List<TrackingHistory> expected = Collections.emptyList();

        when(trackingPort.getHistoryByService(serviceId)).thenReturn(expected);

        List<TrackingHistory> result = getTrackingHistory.byService(serviceId);

        assertEquals(expected, result);
    }
}
