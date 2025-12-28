package app.infrastructure.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.domain.model.ServiceDelivery;
import app.domain.ports.ServiceDeliveryPort;
import app.infrastructure.service.ArchiveServiceService;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrashCleanupScheduler Unit Tests")
class TrashCleanupSchedulerTest {

        @Mock
        private ServiceDeliveryPort serviceDeliveryPort;

        @Mock
        private ArchiveServiceService archiveServiceService;

        @InjectMocks
        private TrashCleanupScheduler scheduler;

        @Test
        @DisplayName("Should archive expired services from trash")
        void shouldArchiveExpiredServices() {
                // Given
                ServiceDelivery service1 = new ServiceDelivery();
                service1.setIdServiceDelivery(1L);
                service1.setDeleted(true);
                service1.setDeletedAt(LocalDateTime.now().minusDays(65));

                ServiceDelivery service2 = new ServiceDelivery();
                service2.setIdServiceDelivery(2L);
                service2.setDeleted(true);
                service2.setDeletedAt(LocalDateTime.now().minusDays(70));

                when(serviceDeliveryPort.findDeletedExpiredBefore(any(LocalDateTime.class)))
                                .thenReturn(Arrays.asList(service1, service2));

                // When
                scheduler.cleanupExpiredTrash();

                // Then
                verify(archiveServiceService, times(2)).archiveService(
                                any(ServiceDelivery.class),
                                eq(null),
                                anyString());
        }

        @Test
        @DisplayName("Should do nothing when no expired services")
        void shouldDoNothingWhenNoExpiredServices() {
                // Given
                when(serviceDeliveryPort.findDeletedExpiredBefore(any(LocalDateTime.class)))
                                .thenReturn(Collections.emptyList());

                // When
                scheduler.cleanupExpiredTrash();

                // Then
                verify(archiveServiceService, never()).archiveService(any(), any(), any());
        }

        @Test
        @DisplayName("Should continue archiving if one fails")
        void shouldContinueIfOneFails() {
                // Given
                ServiceDelivery service1 = new ServiceDelivery();
                service1.setIdServiceDelivery(1L);
                service1.setDeleted(true);

                ServiceDelivery service2 = new ServiceDelivery();
                service2.setIdServiceDelivery(2L);
                service2.setDeleted(true);

                when(serviceDeliveryPort.findDeletedExpiredBefore(any(LocalDateTime.class)))
                                .thenReturn(Arrays.asList(service1, service2));

                // Simular que el primero falla
                doThrow(new RuntimeException("Archive error"))
                                .when(archiveServiceService).archiveService(eq(service1), any(), any());

                // When
                scheduler.cleanupExpiredTrash();

                // Then - El segundo debe procesarse a pesar del error en el primero
                verify(archiveServiceService).archiveService(eq(service1), any(), any());
                verify(archiveServiceService).archiveService(eq(service2), any(), any());
        }
}
