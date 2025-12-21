package app.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.application.exceptions.BusinessException;
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
class UpdateServiceDeliveryTest {

    @Mock
    private ServiceDeliveryPort serviceDeliveryPort;
    @Mock
    private EmployeePort employeePort;

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
    void shouldUpdateStatusToPendingWhenEvidenceComplete() throws Exception {
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(1L)).thenReturn(messenger);
        when(serviceDeliveryPort.save(argThat(s -> s.getCurrentStatus() == Status.PENDING))).thenReturn(service);

        updateServiceDelivery.updateStatus(1L, Status.PENDING, "Observacion", signature, photos, 1L);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.PENDING &&
                s.getSignature() != null &&
                s.getPhotos().size() == 1 &&
                s.getHistory().size() == 1));
    }

    @Test
    @DisplayName("Debe lanzar excepción si servicio no existe o está en papelera")
    void shouldThrowExceptionIfServiceNotFound() {
        when(serviceDeliveryPort.findByIdActive(anyLong())).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(99L, Status.PENDING, "Obs", signature, photos, 1L));

        assertEquals("El servicio con ID 99 no existe o está en la papelera.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción si falta firma para estado PENDING")
    void shouldThrowExceptionIfSignatureMissingForPending() {
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(1L)).thenReturn(messenger);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(1L, Status.PENDING, "Obs", null, photos, 1L));

        assertEquals("Para el estado PENDING la firma es obligatoria.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción si falta foto para estado RETURNED")
    void shouldThrowExceptionIfPhotoMissingForReturned() {
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(1L)).thenReturn(messenger);

        BusinessException ex = assertThrows(BusinessException.class, () -> updateServiceDelivery.updateStatus(1L,
                Status.RETURNED, "Obs", signature, Collections.emptyList(), 1L));

        assertEquals("Para el estado RETURNED al menos una foto es obligatoria.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe permitir ADMIN cambiar a CANCELED desde ASSIGNED")
    void shouldAllowCanceledOnlyForAdminFromAssigned() throws Exception {
        service.setCurrentStatus(Status.ASSIGNED);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(2L)).thenReturn(admin);
        when(serviceDeliveryPort.save(argThat(s -> s.getCurrentStatus() == Status.CANCELED))).thenReturn(service);

        updateServiceDelivery.updateStatus(1L, Status.CANCELED, "Cancelado por admin", null, null, 2L);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.CANCELED));
    }

    @Test
    @DisplayName("Debe impedir MESSENGER cambiar a CANCELED")
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
    @DisplayName("Debe impedir MESSENGER actualizar desde PENDING (bloqueado)")
    void shouldBlockMessengerFromPending() {
        service.setCurrentStatus(Status.PENDING);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(1L)).thenReturn(messenger);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(1L, Status.DELIVERED, "Obs", signature, null, 1L));

        assertEquals("El servicio está en estado PENDING. Solo un administrador puede cambiarlo a CANCELED o RESOLVED.",
                ex.getMessage());
    }

    @Test
    @DisplayName("Debe impedir MESSENGER actualizar desde DELIVERED (bloqueado)")
    void shouldBlockMessengerFromDelivered() {
        service.setCurrentStatus(Status.DELIVERED);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(1L)).thenReturn(messenger);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(1L, Status.RETURNED, "Obs", signature, photos, 1L));

        assertEquals("El servicio ya fue marcado como ENTREGADO. Solo un administrador puede modificar su estado.",
                ex.getMessage());
    }

    @Test
    @DisplayName("Debe impedir cambio desde estado CANCELED (final)")
    void shouldForbidTransitionFromCanceled() {
        service.setCurrentStatus(Status.CANCELED);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(2L)).thenReturn(admin);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(1L, Status.RESOLVED, "Obs", null, null, 2L));

        assertEquals("El servicio está en estado final CANCELED y no se puede modificar.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe impedir cambio desde estado RESOLVED (final)")
    void shouldForbidTransitionFromResolved() {
        service.setCurrentStatus(Status.RESOLVED);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(2L)).thenReturn(admin);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(1L, Status.CANCELED, "Obs", null, null, 2L));

        assertEquals("El servicio está en estado final RESOLVED y no se puede modificar.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe permitir ADMIN cambiar a RESOLVED desde PENDING")
    void shouldAllowResolvedOnlyForAdminFromPending() throws Exception {
        service.setCurrentStatus(Status.PENDING);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(2L)).thenReturn(admin);
        when(serviceDeliveryPort.save(argThat(s -> s.getCurrentStatus() == Status.RESOLVED))).thenReturn(service);

        updateServiceDelivery.updateStatus(1L, Status.RESOLVED, "Resuelto", null, null, 2L);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.RESOLVED));
    }

    @Test
    @DisplayName("Debe impedir ADMIN usar estados de mensajero (PENDING, DELIVERED, RETURNED)")
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
    void shouldThrowExceptionIfSameStatus() {
        service.setCurrentStatus(Status.ASSIGNED);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(1L)).thenReturn(messenger);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(1L, Status.ASSIGNED, "Obs", signature, photos, 1L));

        assertEquals("El servicio ya se encuentra en estado ASSIGNED", ex.getMessage());
    }
}
