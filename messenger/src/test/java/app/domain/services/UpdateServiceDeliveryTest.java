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
    private Employee employee;
    private Signature signature;
    private List<Photo> photos;

    @BeforeEach
    void setUp() {
        service = new ServiceDelivery();
        service.setIdServiceDelivery(1L);
        service.setCurrentStatus(Status.ASSIGNED);

        employee = new Employee();
        employee.setIdEmployee(1L);
        employee.setDocument(12345L);
        employee.setUserName("testUser");
        employee.setRole(Role.MESSENGER);

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
        when(serviceDeliveryPort.findById(1L)).thenReturn(service);
        when(employeePort.findById(12345L)).thenReturn(employee);

        updateServiceDelivery.updateStatus(1L, Status.PENDING, "Observacion", signature, photos, 12345L);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.PENDING &&
                s.getSignature() != null &&
                s.getPhotos().size() == 1 &&
                s.getHistory().size() == 1));
    }

    @Test
    @DisplayName("Debe lanzar excepción si servicio no existe")
    void shouldThrowExceptionIfServiceNotFound() {
        when(serviceDeliveryPort.findById(anyLong())).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(99L, Status.PENDING, "Obs", signature, photos, 12345L));

        assertEquals("El servicio con ID 99 no existe.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción si falta firma para estado PENDING")
    void shouldThrowExceptionIfSignatureMissingForPending() {
        when(serviceDeliveryPort.findById(1L)).thenReturn(service);
        when(employeePort.findById(12345L)).thenReturn(employee);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(1L, Status.PENDING, "Obs", null, photos, 12345L));

        assertEquals("Para el estado PENDING la firma es obligatoria.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción si falta foto para estado RETURNED")
    void shouldThrowExceptionIfPhotoMissingForReturned() {
        when(serviceDeliveryPort.findById(1L)).thenReturn(service);
        when(employeePort.findById(12345L)).thenReturn(employee);

        BusinessException ex = assertThrows(BusinessException.class, () -> updateServiceDelivery.updateStatus(1L,
                Status.RETURNED, "Obs", signature, Collections.emptyList(), 12345L));

        assertEquals("Para el estado RETURNED al menos una foto es obligatoria.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe permitir cambio a CANCELED solo si es ADMIN desde ASSIGNED")
    void shouldAllowCanceledOnlyForAdminFromAssigned() throws Exception {
        service.setCurrentStatus(Status.ASSIGNED);
        when(serviceDeliveryPort.findById(1L)).thenReturn(service);

        Employee admin = new Employee();
        admin.setDocument(999L);
        admin.setRole(Role.ADMIN);
        when(employeePort.findById(999L)).thenReturn(admin);
        when(serviceDeliveryPort.save(argThat(s -> s.getCurrentStatus() == Status.CANCELED))).thenReturn(service);

        updateServiceDelivery.updateStatus(1L, Status.CANCELED, "Cancelado por admin", null, null, 999L);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.CANCELED));
    }

    @Test
    @DisplayName("Debe impedir cambio a CANCELED si es MESSENGER")
    void shouldForbidCanceledForMessenger() {
        service.setCurrentStatus(Status.ASSIGNED);
        when(serviceDeliveryPort.findById(1L)).thenReturn(service);
        when(employeePort.findById(12345L)).thenReturn(employee); // Role MESSENGER

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(1L, Status.CANCELED, "Obs", null, null, 12345L));

        assertEquals("Solo el administrador puede cambiar el estado a CANCELED", ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción si servicio ya está entregado")
    void shouldThrowExceptionIfAlreadyDelivered() {
        service.setCurrentStatus(Status.DELIVERED);
        when(serviceDeliveryPort.findById(1L)).thenReturn(service);
        when(employeePort.findById(12345L)).thenReturn(employee);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(1L, Status.RETURNED, "Obs", signature, photos, 12345L));

        assertEquals("El servicio ya fue entregado y no se puede modificar su estado.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción si servicio ya está resuelto")
    void shouldThrowExceptionIfAlreadyResolved() {
        service.setCurrentStatus(Status.RESOLVED);
        when(serviceDeliveryPort.findById(1L)).thenReturn(service);
        when(employeePort.findById(12345L)).thenReturn(employee);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(1L, Status.PENDING, "Obs", signature, photos, 12345L));

        assertEquals("El servicio ya fue resuelto y no se puede modificar su estado.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe permitir cambio a RESOLVED solo por ADMIN desde PENDING")
    void shouldAllowResolvedOnlyForAdminFromPending() throws Exception {
        service.setCurrentStatus(Status.PENDING);
        when(serviceDeliveryPort.findById(1L)).thenReturn(service);

        Employee admin = new Employee();
        admin.setDocument(999L);
        admin.setRole(Role.ADMIN);
        when(employeePort.findById(999L)).thenReturn(admin);
        when(serviceDeliveryPort.save(argThat(s -> s.getCurrentStatus() == Status.RESOLVED))).thenReturn(service);

        updateServiceDelivery.updateStatus(1L, Status.RESOLVED, "Resuelto", null, null, 999L);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.RESOLVED));
    }

    @Test
    @DisplayName("Debe impedir cambio de PENDING a otro estado diferente de RESOLVED")
    void shouldForbidNonResolvedTransitionFromPending() {
        service.setCurrentStatus(Status.PENDING);
        when(serviceDeliveryPort.findById(1L)).thenReturn(service);
        when(employeePort.findById(12345L)).thenReturn(employee);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(1L, Status.DELIVERED, "Obs", signature, null, 12345L));

        assertEquals("Desde estado PENDING solo se puede cambiar a RESOLVED.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe validar que no se actualice al mismo estado")
    void shouldThrowExceptionIfSameStatus() {
        service.setCurrentStatus(Status.ASSIGNED);
        when(serviceDeliveryPort.findById(1L)).thenReturn(service);
        when(employeePort.findById(12345L)).thenReturn(employee);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(1L, Status.ASSIGNED, "Obs", signature, photos, 12345L));

        assertEquals("El servicio ya se encuentra en estado ASSIGNED", ex.getMessage());
    }
}
