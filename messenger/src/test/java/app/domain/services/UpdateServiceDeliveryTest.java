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
    @DisplayName("Debe lanzar excepción si falta foto para estado FAILED")
    void shouldThrowExceptionIfPhotoMissingForFailed() {
        when(serviceDeliveryPort.findById(1L)).thenReturn(service);
        when(employeePort.findById(12345L)).thenReturn(employee);

        BusinessException ex = assertThrows(BusinessException.class, () -> updateServiceDelivery.updateStatus(1L,
                Status.FAILED, "Obs", signature, Collections.emptyList(), 12345L));

        assertEquals("Para el estado FAILED al menos una foto es obligatoria.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe permitir cambio a CANCELED solo si es ADMIN")
    void shouldAllowCanceledOnlyForAdmin() throws Exception {
        service.setCurrentStatus(Status.PENDING);
        when(serviceDeliveryPort.findById(1L)).thenReturn(service);

        Employee admin = new Employee();
        admin.setDocument(999L);
        admin.setRole(Role.ADMIN);
        when(employeePort.findById(999L)).thenReturn(admin);

        updateServiceDelivery.updateStatus(1L, Status.CANCELED, "Cancelado por admin", null, null, 999L);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.CANCELED));
    }

    @Test
    @DisplayName("Debe impedir cambio a CANCELED si es MESSENGER")
    void shouldForbidCanceledForMessenger() {
        service.setCurrentStatus(Status.PENDING);
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
    @DisplayName("Debe validar que no se actualice al mismo estado")
    void shouldThrowExceptionIfSameStatus() {
        service.setCurrentStatus(Status.PENDING);
        when(serviceDeliveryPort.findById(1L)).thenReturn(service);
        when(employeePort.findById(12345L)).thenReturn(employee);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(1L, Status.PENDING, "Obs", signature, photos, 12345L));

        assertEquals("El servicio ya se encuentra en estado PENDING", ex.getMessage());
    }
}
