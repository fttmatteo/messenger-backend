package app.domain.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.domain.exception.BusinessException;
import app.domain.model.Employee;
import app.domain.model.Plate;
import app.domain.model.ServiceDelivery;
import app.domain.model.enums.Role;
import app.domain.model.enums.Status;
import app.domain.ports.EmployeePort;
import app.domain.ports.ServiceDeliveryPort;
import app.domain.ports.ArchivePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteServiceDelivery Unit Tests (Soft Delete)")
class DeleteServiceDeliveryTest {

    @Mock
    private ServiceDeliveryPort serviceDeliveryPort;

    @Mock
    private EmployeePort employeePort;

    @Mock
    private ArchivePort archivePort;

    @InjectMocks
    private DeleteServiceDelivery deleteServiceDelivery;

    @Test
    @DisplayName("Debe mover servicio a papelera (soft delete)")
    /**
     * Verifica que el servicio se marque como eliminado (soft delete) y se registre
     * la fecha.
     */
    void shouldSoftDeleteService() throws Exception {
        ServiceDelivery service = new ServiceDelivery();
        service.setIdServiceDelivery(1L);
        service.setCurrentStatus(Status.PENDING);
        Plate plate = new Plate();
        plate.setPlateNumber("ABC-123");
        service.setPlate(plate);

        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(serviceDeliveryPort.save(argThat(s -> s.isDeleted() && s.getDeletedAt() != null))).thenReturn(service);

        deleteServiceDelivery.deleteById(1L);

        verify(serviceDeliveryPort).save(argThat(s -> s.isDeleted() && s.getDeletedAt() != null));
    }

    @Test
    @DisplayName("Debe lanzar excepción si servicio no existe o ya está en papelera")
    void shouldThrowExceptionIfServiceNotFoundOrAlreadyDeleted() {
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> deleteServiceDelivery.deleteById(1L));

        assertEquals("El servicio de entrega que intenta eliminar no existe o ya está en la papelera.",
                ex.getMessage());
    }

    @Test
    @DisplayName("Debe restaurar servicio desde papelera solo por ADMIN")
    /**
     * Verifica que un administrador pueda restaurar un servicio eliminado.
     */
    void shouldRestoreServiceFromTrashByAdmin() throws Exception {
        ServiceDelivery service = new ServiceDelivery();
        service.setIdServiceDelivery(1L);
        service.setCurrentStatus(Status.PENDING);
        service.setDeleted(true);

        Employee admin = new Employee();
        admin.setIdEmployee(1L);
        admin.setRole(Role.ADMIN);

        when(serviceDeliveryPort.findById(1L)).thenReturn(service);
        when(employeePort.findById(1L)).thenReturn(admin);
        when(serviceDeliveryPort.save(argThat(s -> !s.isDeleted() && s.getDeletedAt() == null))).thenReturn(service);

        ServiceDelivery restored = deleteServiceDelivery.restore(1L, 1L);

        assertFalse(restored.isDeleted());
        verify(serviceDeliveryPort).save(argThat(s -> !s.isDeleted()));
    }

    @Test
    @DisplayName("Debe impedir restaurar servicio si no es ADMIN")
    void shouldForbidRestoreByNonAdmin() {
        ServiceDelivery service = new ServiceDelivery();
        service.setIdServiceDelivery(1L);
        service.setDeleted(true);

        Employee messenger = new Employee();
        messenger.setIdEmployee(1L);
        messenger.setRole(Role.MESSENGER);

        when(serviceDeliveryPort.findById(1L)).thenReturn(service);
        when(employeePort.findById(1L)).thenReturn(messenger);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> deleteServiceDelivery.restore(1L, 1L));

        assertEquals("Solo los administradores pueden restaurar servicios de la papelera.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción si servicio no está en papelera al restaurar")
    void shouldThrowExceptionIfServiceNotInTrash() {
        ServiceDelivery service = new ServiceDelivery();
        service.setIdServiceDelivery(1L);
        service.setDeleted(false);

        when(serviceDeliveryPort.findById(1L)).thenReturn(service);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> deleteServiceDelivery.restore(1L, 1L));

        assertEquals("El servicio no está en la papelera.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe archivar permanentemente servicio en papelera")
    /**
     * Verifica que se pueda archivar físicamente un servicio que ya está en la
     * papelera.
     */
    void shouldArchiveServiceInTrash() throws Exception {
        ServiceDelivery service = new ServiceDelivery();
        service.setIdServiceDelivery(1L);
        service.setDeleted(true);

        when(serviceDeliveryPort.findById(1L)).thenReturn(service);

        deleteServiceDelivery.archiveService(1L);

        // El test pasará si no se lanza excepción
    }

    @Test
    @DisplayName("Debe impedir archivado si servicio no está en papelera")
    void shouldForbidArchiveIfNotInTrash() {
        ServiceDelivery service = new ServiceDelivery();
        service.setIdServiceDelivery(1L);
        service.setDeleted(false);

        when(serviceDeliveryPort.findById(1L)).thenReturn(service);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> deleteServiceDelivery.archiveService(1L));

        assertEquals("Solo se pueden archivar servicios que estén en la papelera.", ex.getMessage());
    }
}
