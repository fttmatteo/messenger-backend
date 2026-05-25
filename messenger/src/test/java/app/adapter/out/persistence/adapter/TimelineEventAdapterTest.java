package app.adapter.out.persistence.adapter;

import app.adapter.out.persistence.entities.TimelineEventEntity;
import app.adapter.out.persistence.repository.TimelineEventRepository;
import app.domain.model.TimelineEvent;
import app.domain.model.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

class TimelineEventAdapterTest {

    @Mock
    private TimelineEventRepository repository;

    @InjectMocks
    private TimelineEventAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Debe guardar el evento y retornar el modelo de dominio mapeado")
    void shouldSaveEventAndReturnMappedDomainModel() {
        TimelineEvent domain = new TimelineEvent();
        domain.setMessengerId(1L);
        domain.setStatus(Status.DELIVERED);
        domain.setEventDate(LocalDate.now());
        domain.setTimestamp(LocalDateTime.now());
        domain.setPlateNumber("ABC-123");

        TimelineEventEntity entityToReturn = new TimelineEventEntity();
        entityToReturn.setId(10L);
        entityToReturn.setMessengerId(1L);
        entityToReturn.setStatus("DELIVERED");
        entityToReturn.setEventDate(domain.getEventDate());
        entityToReturn.setTimestamp(domain.getTimestamp());
        entityToReturn.setPlateNumber("ABC-123");

        when(repository.save(any(TimelineEventEntity.class))).thenReturn(entityToReturn);

        TimelineEvent result = adapter.save(domain);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(Status.DELIVERED, result.getStatus());
        assertEquals("ABC-123", result.getPlateNumber());
        verify(repository, times(1)).save(any(TimelineEventEntity.class));
    }

    @Test
    @DisplayName("Debe encontrar eventos por mensajero y fecha")
    void shouldFindEventsByMessengerAndDate() {
        LocalDate date = LocalDate.now();
        TimelineEventEntity entity = new TimelineEventEntity();
        entity.setId(10L);
        entity.setMessengerId(1L);
        entity.setStatus("PENDING");
        entity.setEventDate(date);
        entity.setTimestamp(LocalDateTime.now());

        when(repository.findByMessengerIdAndEventDateOrderByTimestampDesc(1L, date))
                .thenReturn(List.of(entity));

        List<TimelineEvent> results = adapter.findByMessengerIdAndDate(1L, date);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(10L, results.get(0).getId());
        assertEquals(Status.PENDING, results.get(0).getStatus());
    }
}
