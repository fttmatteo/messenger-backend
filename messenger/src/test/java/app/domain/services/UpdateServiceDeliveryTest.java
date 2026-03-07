package app.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import app.domain.exception.BusinessException;
import app.domain.model.Employee;
import app.domain.model.Photo;
import app.domain.model.ServiceDelivery;
import app.domain.model.Signature;
import app.domain.model.enums.Role;
import app.domain.model.enums.Status;
import app.domain.ports.EmployeePort;
import app.domain.ports.ServiceDeliveryPort;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateServiceDelivery Unit Tests")
/**
 * Clase de pruebas unitarias para UpdateServiceDelivery.
 */
class UpdateServiceDeliveryTest {

    @Mock
    private ServiceDeliveryPort serviceDeliveryPort;
    @Mock
    private EmployeePort employeePort;
    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UpdateServiceDelivery updateServiceDelivery;

    private ServiceDelivery service;
    private Employee messenger;
    private Employee admin;
    private Signature signature;
    private List<Photo> photos;

    @BeforeEach
    void setUp() {
        service = new ServiceDelivery();
        service.setIdServiceDelivery(1L);
        service.setCurrentStatus(Status.ASSIGNED);

        messenger = new Employee();
        messenger.setIdEmployee(1L);
        messenger.setDocument(12345L);
        messenger.setRole(Role.MESSENGER);

        admin = new Employee();
        admin.setIdEmployee(2L);
        admin.setDocument(999L);
        admin.setRole(Role.ADMIN);

        signature = new Signature();
        signature.setSignaturePath("sig.png");

        photos = new ArrayList<>();
        Photo p = new Photo();
        p.setPhotoPath("evidencia.jpg");
        photos.add(p);
    }

    @Test
    @DisplayName("Debe actualizar estado a PENDING con evidencias completas")
    /**
     * Verifica que el estado se actualice correctamente a PENDING cuando las evidencias están completas.
     */
    void shouldUpdateStatusToPendingWhenEvidenceComplete() throws Exception {
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(1L)).thenReturn(messenger);
        when(serviceDeliveryPort.save(argThat(s -> s.getCurrentStatus() == Status.PENDING))).thenReturn(service);

        updateServiceDelivery.updateStatus(1L, Status.PENDING, "Observacion", signature, photos, 1L);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.PENDING &&
                s.getSignature() != null &&
                s.getPhotos().size() == 1 &&
                s.getHistory().size() == 1 &&
                s.getHistory().get(0).getSignature() != null &&
                s.getHistory().get(0).getObservation().equals("Observacion")));
    }

    @Test
    @DisplayName("Debe lanzar excepción si servicio no existe o está en papelera")
    /**
     * Verifica que se lance excepción cuando el servicio no existe o está en la papelera.
     */
    void shouldThrowExceptionIfServiceNotFound() {
        when(serviceDeliveryPort.findByIdActive(anyLong())).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(99L, Status.PENDING, "Obs", signature, photos, 1L));

        assertEquals("El servicio con ID 99 no existe o está en la papelera.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción si falta firma para estado PENDING")
    /**
     * Verifica que se lance excepción cuando falta la firma para el estado PENDING.
     */
    void shouldThrowExceptionIfSignatureMissingForPending() {
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(1L)).thenReturn(messenger);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(1L, Status.PENDING, "Obs", null, photos, 1L));

        assertEquals("Para marcar como PENDIENTE, la firma es obligatoria.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción si falta foto para estado RETURNED")
    /**
     * Verifica que se lance excepción cuando falta la foto para el estado RETURNED.
     */
    void shouldThrowExceptionIfPhotoMissingForReturned() {
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(1L)).thenReturn(messenger);

        BusinessException ex = assertThrows(BusinessException.class, () -> updateServiceDelivery.updateStatus(1L,
                Status.RETURNED, "Obs", signature, Collections.emptyList(), 1L));

        assertEquals("Para marcar como DEVUELTO, al menos una foto de evidencia es obligatoria.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe permitir ADMIN cambiar a CANCELED desde cualquier estado")
    /**
     * Verifica que el ADMIN pueda cambiar el estado a CANCELED desde cualquier estado.
     */
    void shouldAllowCanceledForAdminFromAnyState() throws Exception {
        service.setCurrentStatus(Status.ASSIGNED);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(2L)).thenReturn(admin);
        when(serviceDeliveryPort.save(argThat(s -> s.getCurrentStatus() == Status.CANCELED))).thenReturn(service);

        updateServiceDelivery.updateStatus(1L, Status.CANCELED, "Cancelado por admin", null, null, 2L);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.CANCELED));
    }

    @Test
    @DisplayName("Debe impedir MESSENGER cambiar a CANCELED")
    /**
     * Verifica que el MESSENGER no pueda cambiar el estado a CANCELED.
     */
    void shouldForbidCanceledForMessenger() {
        service.setCurrentStatus(Status.ASSIGNED);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(1L)).thenReturn(messenger);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(1L, Status.CANCELED, "Obs", null, null, 1L));

        assertEquals(
                "Como mensajero solo puedes cambiar el estado a: PENDING, DELIVERED o RETURNED. No tienes permiso para usar el estado CANCELED.",
                ex.getMessage());
    }

    @Test
    @DisplayName("Debe permitir MESSENGER actualizar desde cualquier estado no final")
    /**
     * Verifica que el MESSENGER pueda actualizar el estado desde cualquier estado no final.
     */
    void shouldAllowMessengerToUpdateFromAnyNonFinalState() throws Exception {
        service.setCurrentStatus(Status.PENDING);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(1L)).thenReturn(messenger);
        when(serviceDeliveryPort.save(argThat(s -> s.getCurrentStatus() == Status.DELIVERED))).thenReturn(service);

        updateServiceDelivery.updateStatus(1L, Status.DELIVERED, "Entrega completada", signature, null, 1L);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.DELIVERED));
    }

    @Test
    @DisplayName("Debe permitir ADMIN cambiar a RESOLVED desde cualquier estado")
    /**
     * Verifica que el ADMIN pueda cambiar el estado a RESOLVED desde cualquier estado.
     */
    void shouldAllowResolvedForAdminFromAnyState() throws Exception {
        service.setCurrentStatus(Status.PENDING);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(2L)).thenReturn(admin);
        when(serviceDeliveryPort.save(argThat(s -> s.getCurrentStatus() == Status.RESOLVED))).thenReturn(service);

        updateServiceDelivery.updateStatus(1L, Status.RESOLVED, "Resuelto", null, null, 2L);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.RESOLVED));
    }

    @Test
    @DisplayName("Debe impedir ADMIN usar estados de mensajero (PENDING, DELIVERED, RETURNED)")
    /**
     * Verifica que el ADMIN no pueda usar los estados de mensajero (PENDING, DELIVERED, RETURNED).
     */
    void shouldForbidAdminFromUsingMessengerStates() {
        service.setCurrentStatus(Status.ASSIGNED);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(2L)).thenReturn(admin);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(1L, Status.PENDING, "Obs", signature, photos, 2L));

        assertEquals(
                "Como administrador solo puedes cambiar el estado a: CANCELED o RESOLVED. Para otros estados, el mensajero asignado debe realizar el cambio.",
                ex.getMessage());
    }

    @Test
    @DisplayName("Debe validar que no se actualice al mismo estado")
    /**
     * Verifica que no se pueda actualizar al mismo estado.
     */
    void shouldThrowExceptionIfSameStatus() {
        service.setCurrentStatus(Status.ASSIGNED);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(1L)).thenReturn(messenger);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(1L, Status.ASSIGNED, "Obs", signature, photos, 1L));

        assertEquals("El servicio ya se encuentra en estado ASSIGNED", ex.getMessage());
    }

    @Test
    @DisplayName("Debe permitir cambiar estado DELIVERED sin restricción de tiempo")
    /**
     * Verifica que el estado DELIVERED se pueda actualizar sin restricción de tiempo.
     */
    void shouldAllowDeliveredUpdateWithoutTimeRestriction() throws Exception {
        service.setCurrentStatus(Status.DELIVERED);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(2L)).thenReturn(admin);
        when(serviceDeliveryPort.save(argThat(s -> s.getCurrentStatus() == Status.CANCELED))).thenReturn(service);

        updateServiceDelivery.updateStatus(1L, Status.CANCELED, "Cancelado sin límite de tiempo", null, null, 2L);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.CANCELED));
    }

    @Test
    @DisplayName("Debe reasignar mensajero cuando servicio está en CANCELED")
    /**
     * Verifica que el mensajero se pueda reasignar cuando el servicio está en CANCELED.
     */
    void shouldReassignMessengerFromCanceled() throws Exception {
        service.setCurrentStatus(Status.CANCELED);

        Employee newMessenger = new Employee();
        newMessenger.setIdEmployee(3L);
        newMessenger.setRole(Role.MESSENGER);

        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(2L)).thenReturn(admin);
        when(employeePort.findById(3L)).thenReturn(newMessenger);
        when(serviceDeliveryPort.save(argThat(s -> s.getCurrentStatus() == Status.ASSIGNED))).thenReturn(service);

        updateServiceDelivery.reassignMessenger(1L, 3L, 2L);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.ASSIGNED &&
                s.getMessenger().equals(newMessenger) &&
                s.getLockedAt() == null));
    }

    @Test
    @DisplayName("Debe impedir reasignación si no está en CANCELED")
    /**
     * Verifica que el mensajero no se pueda reasignar si el servicio no está en CANCELED.
     */
    void shouldForbidReassignIfNotCanceled() {
        service.setCurrentStatus(Status.PENDING);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(2L)).thenReturn(admin);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.reassignMessenger(1L, 3L, 2L));

        assertEquals("Solo se pueden reasignar servicios en estado CANCELED. Estado actual: PENDING", ex.getMessage());
    }

    @Test
    @DisplayName("Debe impedir reasignación si no es ADMIN")
    /**
     * Verifica que el mensajero no se pueda reasignar si no es ADMIN.
     */
    void shouldForbidReassignIfNotAdmin() {
        service.setCurrentStatus(Status.CANCELED);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(1L)).thenReturn(messenger);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.reassignMessenger(1L, 3L, 1L));

        assertEquals("Solo los administradores pueden reasignar servicios.", ex.getMessage());
    }
}
