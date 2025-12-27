package app.infrastructure.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import app.domain.model.ServiceDelivery;
import app.domain.ports.ServiceDeliveryPort;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrashCleanupScheduler Unit Tests")
class TrashCleanupSchedulerTest {

    @Mock
    private ServiceDeliveryPort serviceDeliveryPort;

    @InjectMocks
    private TrashCleanupScheduler trashCleanupScheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(trashCleanupScheduler, "retentionDays", 60);
    }

    @Test
    @DisplayName("Debe eliminar permanentemente servicios expirados en papelera")
    /**
     * Verifica que el scheduler llame al hardDelete para items caducados en la
     * papelera.
     */
    void shouldHardDeleteExpiredServices() {
        ServiceDelivery expiredService1 = new ServiceDelivery();
        expiredService1.setIdServiceDelivery(1L);
        expiredService1.setDeleted(true);
        expiredService1.setDeletedAt(LocalDateTime.now().minusDays(70));

        ServiceDelivery expiredService2 = new ServiceDelivery();
        expiredService2.setIdServiceDelivery(2L);
        expiredService2.setDeleted(true);
        expiredService2.setDeletedAt(LocalDateTime.now().minusDays(65));

        when(serviceDeliveryPort.findDeletedExpiredBefore(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(expiredService1, expiredService2));

        trashCleanupScheduler.cleanupExpiredTrash();

        verify(serviceDeliveryPort).hardDeleteById(1L);
        verify(serviceDeliveryPort).hardDeleteById(2L);
        verify(serviceDeliveryPort, times(2)).hardDeleteById(anyLong());
    }

    @Test
    @DisplayName("No debe hacer nada si no hay servicios expirados")
    void shouldDoNothingIfNoExpiredServices() {
        when(serviceDeliveryPort.findDeletedExpiredBefore(any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        trashCleanupScheduler.cleanupExpiredTrash();

        verify(serviceDeliveryPort, never()).hardDeleteById(anyLong());
    }

    @Test
    @DisplayName("Debe continuar con otros servicios si uno falla")
    /**
     * Verifica robustez: el fallo en eliminar un item no debe detener el procesado
     * de los demás.
     */
    void shouldContinueIfOneFails() {
        ServiceDelivery service1 = new ServiceDelivery();
        service1.setIdServiceDelivery(1L);
        service1.setDeleted(true);

        ServiceDelivery service2 = new ServiceDelivery();
        service2.setIdServiceDelivery(2L);
        service2.setDeleted(true);

        when(serviceDeliveryPort.findDeletedExpiredBefore(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(service1, service2));

        doThrow(new RuntimeException("Error de BD")).when(serviceDeliveryPort).hardDeleteById(1L);
        doNothing().when(serviceDeliveryPort).hardDeleteById(2L);

        trashCleanupScheduler.cleanupExpiredTrash();

        verify(serviceDeliveryPort).hardDeleteById(1L);
        verify(serviceDeliveryPort).hardDeleteById(2L);
    }
}
