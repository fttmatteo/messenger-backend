package app.application.usecase.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de GetTrackingHistoryUseCase")
class GetTrackingHistoryUseCaseTest {

    @Mock
    private TrackingPort trackingPort;

    @InjectMocks
    private GetTrackingHistoryUseCase getTrackingHistory;

    @Test
    @DisplayName("Debe consultar por mensajero y fecha con paginación")
    void shouldGetByMessengerAndDatePaginated() {
        Long messengerId = 1L;
        LocalDate date = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);
        Page<TrackingHistory> expected = new PageImpl<>(Collections.emptyList());

        when(trackingPort.getHistoryByMessengerPaginated(eq(messengerId), eq(date), any(Pageable.class)))
                .thenReturn(expected);

        Page<TrackingHistory> result = getTrackingHistory.byMessengerAndDatePaginated(messengerId, date, pageable);

        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Debe obtener por servicio")

    void shouldGetByService() {
        Long serviceId = 100L;
        List<TrackingHistory> expected = Collections.emptyList();

        when(trackingPort.getHistoryByService(serviceId)).thenReturn(expected);

        List<TrackingHistory> result = getTrackingHistory.byService(serviceId);

        assertEquals(expected, result);
    }
}
