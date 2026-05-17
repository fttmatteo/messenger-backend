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
@DisplayName("Pruebas unitarias de UpdateServiceDelivery")
class UpdateServiceDeliveryTest {

    @Mock
    private ServiceDeliveryPort serviceDeliveryPort;
    @Mock
    private EmployeePort employeePort;
    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;
    @Mock
    private app.domain.ports.TrackingPort trackingPort;

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
    @DisplayName("Debe actualizar estado a pendiente cuando la evidencia esté completa")

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
    @DisplayName("Debe lanzar excepción si el servicio no se encuentra")

    void shouldThrowExceptionIfServiceNotFound() {
        when(serviceDeliveryPort.findByIdActive(anyLong())).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(99L, Status.PENDING, "Obs", signature, photos, 1L));

        assertEquals("El servicio con ID 99 no existe o está en la papelera.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción si falta la firma para pendiente")

    void shouldThrowExceptionIfSignatureMissingForPending() {
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(1L)).thenReturn(messenger);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(1L, Status.PENDING, "Obs", null, photos, 1L));

        assertEquals("Para marcar como PENDIENTE, la firma es obligatoria.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción si falta la foto para devuelto")

    void shouldThrowExceptionIfPhotoMissingForReturned() {
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(1L)).thenReturn(messenger);

        BusinessException ex = assertThrows(BusinessException.class, () -> updateServiceDelivery.updateStatus(1L,
                Status.RETURNED, "Obs", signature, Collections.emptyList(), 1L));

        assertEquals("Para marcar como DEVUELTO, al menos una foto de evidencia es obligatoria.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe permitir cancelado para administrador desde cualquier estado")

    void shouldAllowCanceledForAdminFromAnyState() throws Exception {
        service.setCurrentStatus(Status.ASSIGNED);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(2L)).thenReturn(admin);
        when(serviceDeliveryPort.save(argThat(s -> s.getCurrentStatus() == Status.CANCELED))).thenReturn(service);

        updateServiceDelivery.updateStatus(1L, Status.CANCELED, "Cancelado por admin", null, null, 2L);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.CANCELED));
    }

    @Test
    @DisplayName("Debe prohibir cancelado para el mensajero")

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
    @DisplayName("Debe permitir al mensajero actualizar desde cualquier estado no final")

    void shouldAllowMessengerToUpdateFromAnyNonFinalState() throws Exception {
        service.setCurrentStatus(Status.PENDING);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(1L)).thenReturn(messenger);
        when(serviceDeliveryPort.save(argThat(s -> s.getCurrentStatus() == Status.DELIVERED))).thenReturn(service);

        updateServiceDelivery.updateStatus(1L, Status.DELIVERED, "Entrega completada", signature, null, 1L);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.DELIVERED));
    }

    @Test
    @DisplayName("Debe permitir resuelto para el administrador desde cualquier estado")

    void shouldAllowResolvedForAdminFromAnyState() throws Exception {
        service.setCurrentStatus(Status.PENDING);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(2L)).thenReturn(admin);
        when(serviceDeliveryPort.save(argThat(s -> s.getCurrentStatus() == Status.RESOLVED))).thenReturn(service);

        updateServiceDelivery.updateStatus(1L, Status.RESOLVED, "Resuelto", null, null, 2L);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.RESOLVED));
    }

    @Test
    @DisplayName("Debe permitir al administrador usar estados del mensajero")

    void shouldAllowAdminToUseMessengerStates() throws Exception {
        service.setCurrentStatus(Status.ASSIGNED);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(2L)).thenReturn(admin);
        when(serviceDeliveryPort.save(argThat(s -> s.getCurrentStatus() == Status.PENDING))).thenReturn(service);

        updateServiceDelivery.updateStatus(1L, Status.PENDING, "Obs", null, null, 2L);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.PENDING));
    }

    @Test
    @DisplayName("Debe lanzar excepción si es el mismo estado")

    void shouldThrowExceptionIfSameStatus() {
        service.setCurrentStatus(Status.ASSIGNED);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(1L)).thenReturn(messenger);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(1L, Status.ASSIGNED, "Obs", signature, photos, 1L));

        assertEquals("El servicio ya se encuentra en estado ASSIGNED", ex.getMessage());
    }

    @Test
    @DisplayName("Debe permitir actualización a entregado sin restricción de tiempo")

    void shouldAllowDeliveredUpdateWithoutTimeRestriction() throws Exception {
        service.setCurrentStatus(Status.DELIVERED);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(2L)).thenReturn(admin);
        when(serviceDeliveryPort.save(argThat(s -> s.getCurrentStatus() == Status.CANCELED))).thenReturn(service);

        updateServiceDelivery.updateStatus(1L, Status.CANCELED, "Cancelado sin límite de tiempo", null, null, 2L);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.CANCELED));
    }

    @Test
    @DisplayName("Debe reasignar mensajero desde cancelado")

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
    @DisplayName("Debe prohibir reasignación si no está cancelado")

    void shouldForbidReassignIfNotCanceled() {
        service.setCurrentStatus(Status.PENDING);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(2L)).thenReturn(admin);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.reassignMessenger(1L, 3L, 2L));

        assertEquals("Solo se pueden reasignar servicios en estado CANCELED. Estado actual: PENDING", ex.getMessage());
    }

    @Test
    @DisplayName("Debe prohibir reasignación si no es administrador")

    void shouldForbidReassignIfNotAdmin() {
        service.setCurrentStatus(Status.CANCELED);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(1L)).thenReturn(messenger);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.reassignMessenger(1L, 3L, 1L));

        assertEquals("Solo los administradores pueden reasignar servicios.", ex.getMessage());
    }

    // Tests de Caja Blanca para validateEvidence()

    @Test
    @DisplayName("testValidateEvidence con estado asignado debe retornar directamente")

    void testValidateEvidence_AssignedStatus_ShouldReturnDirectly() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
            updateServiceDelivery.validateEvidence(Status.ASSIGNED, null, null, null, Role.MESSENGER)
        );
    }

    @Test
    @DisplayName("testValidateEvidence entregado sin firma debe lanzar excepción")

    void testValidateEvidence_Delivered_MissingSignature_ThrowsException() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
            updateServiceDelivery.validateEvidence(Status.DELIVERED, null, null, null, Role.MESSENGER)
        );
        assertEquals("Para marcar como ENTREGADO, la firma de recibido es obligatoria.", ex.getMessage());
    }

    @Test
    @DisplayName("testValidateEvidence devuelto con fotos nulas debe lanzar excepción")

    void testValidateEvidence_Returned_MissingPhotosNull_ThrowsException() {
        assertThrows(BusinessException.class, () ->
            updateServiceDelivery.validateEvidence(Status.RETURNED, new Signature(), null, "Observación válida", Role.MESSENGER)
        );
    }

    @Test
    @DisplayName("testValidateEvidence devuelto con fotos vacías debe lanzar excepción")

    void testValidateEvidence_Returned_MissingPhotosEmpty_ThrowsException() {
        assertThrows(BusinessException.class, () ->
            updateServiceDelivery.validateEvidence(Status.RETURNED, new Signature(), new ArrayList<>(), "Observación válida", Role.MESSENGER)
        );
    }

    @Test
    @DisplayName("testValidateEvidence devuelto con observación nula debe lanzar excepción")

    void testValidateEvidence_Returned_MissingObservationNull_ThrowsException() {
        List<Photo> photos = List.of(new Photo());
        assertThrows(BusinessException.class, () ->
            updateServiceDelivery.validateEvidence(Status.RETURNED, new Signature(), photos, null, Role.MESSENGER)
        );
    }

    @Test
    @DisplayName("testValidateEvidence devuelto con observación en blanco debe lanzar excepción")

    void testValidateEvidence_Returned_MissingObservationBlank_ThrowsException() {
        List<Photo> photos = List.of(new Photo());
        assertThrows(BusinessException.class, () ->
            updateServiceDelivery.validateEvidence(Status.RETURNED, new Signature(), photos, "   ", Role.MESSENGER)
        );
    }

    @Test
    @DisplayName("testValidateEvidence devuelto con todas las evidencias debe pasar")

    void testValidateEvidence_Returned_AllEvidences_ShouldPass() {
        List<Photo> photos = List.of(new Photo());
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
            updateServiceDelivery.validateEvidence(Status.RETURNED, new Signature(), photos, "Motivo de devolución", Role.MESSENGER)
        );
    }

    @Test
    @DisplayName("testValidateEvidence pendiente sin fotos debe lanzar excepción")

    void testValidateEvidence_Pending_MissingPhotos_ThrowsException() {
        assertThrows(BusinessException.class, () ->
            updateServiceDelivery.validateEvidence(Status.PENDING, new Signature(), null, "Obs", Role.MESSENGER)
        );
    }

    @Test
    @DisplayName("testValidateEvidence pendiente con fotos vacías debe lanzar excepción")

    void testValidateEvidence_Pending_EmptyPhotos_ThrowsException() {
        assertThrows(BusinessException.class, () ->
            updateServiceDelivery.validateEvidence(Status.PENDING, new Signature(), new ArrayList<>(), "Obs", Role.MESSENGER)
        );
    }

    @Test
    @DisplayName("testValidateEvidence pendiente sin observación debe lanzar excepción")

    void testValidateEvidence_Pending_MissingObservation_ThrowsException() {
        List<Photo> photos = List.of(new Photo());
        assertThrows(BusinessException.class, () ->
            updateServiceDelivery.validateEvidence(Status.PENDING, new Signature(), photos, "", Role.MESSENGER)
        );
    }

    @Test
    @DisplayName("testValidateEvidence pendiente con observación en blanco debe lanzar excepción")

    void testValidateEvidence_Pending_BlankObservation_ThrowsException() {
        List<Photo> photos = List.of(new Photo());
        assertThrows(BusinessException.class, () ->
            updateServiceDelivery.validateEvidence(Status.PENDING, new Signature(), photos, "   ", Role.MESSENGER)
        );
    }

    @Test
    @DisplayName("testValidateEvidence pendiente con todas las evidencias debe pasar")

    void testValidateEvidence_Pending_AllEvidences_ShouldPass() {
        List<Photo> photos = List.of(new Photo());
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
            updateServiceDelivery.validateEvidence(Status.PENDING, new Signature(), photos, "Observación pendiente", Role.MESSENGER)
        );
    }

    @Test
    @DisplayName("testValidateEvidence entregado con firma debe pasar")

    void testValidateEvidence_Delivered_WithSignature_ShouldPass() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
            updateServiceDelivery.validateEvidence(Status.DELIVERED, new Signature(), null, null, Role.MESSENGER)
        );
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario no se encuentra")

    void shouldThrowExceptionIfUserNotFound() {
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.updateStatus(1L, Status.PENDING, "Obs", signature, photos, 99L));

        assertEquals("El usuario con ID 99 no existe.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción en reasignación si el servicio no se encuentra")

    void shouldThrowReassignIfServiceNotFound() {
        when(serviceDeliveryPort.findByIdActive(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.reassignMessenger(99L, 3L, 2L));

        assertEquals("El servicio con ID 99 no existe o está en la papelera.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción en reasignación si el nuevo mensajero no se encuentra")

    void shouldThrowReassignIfNewMessengerNotFound() {
        service.setCurrentStatus(Status.CANCELED);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(2L)).thenReturn(admin);
        when(employeePort.findById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.reassignMessenger(1L, 99L, 2L));

        assertEquals("El mensajero con ID 99 no existe.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción en reasignación si no tiene rol de mensajero")

    void shouldThrowReassignIfNotMessengerRole() {
        service.setCurrentStatus(Status.CANCELED);
        Employee notMessenger = new Employee();
        notMessenger.setIdEmployee(5L);
        notMessenger.setRole(Role.ADMIN);

        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(2L)).thenReturn(admin);
        when(employeePort.findById(5L)).thenReturn(notMessenger);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.reassignMessenger(1L, 5L, 2L));

        assertEquals("El empleado seleccionado no tiene rol de MENSAJERO.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe actualizar estado a devuelto cuando la evidencia esté completa")

    void shouldUpdateStatusToReturnedWhenEvidenceComplete() throws Exception {
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(1L)).thenReturn(messenger);
        when(serviceDeliveryPort.save(argThat(s -> s.getCurrentStatus() == Status.RETURNED))).thenReturn(service);

        List<Photo> returnPhotos = List.of(new Photo());
        updateServiceDelivery.updateStatus(1L, Status.RETURNED, "Devuelto por daño", signature, returnPhotos, 1L);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.RETURNED));
    }

    @Test
    @DisplayName("Debe guardar rastreo cuando se proporcionan coordenadas")

    void shouldSaveTrackingWhenCoordinatesProvided() throws Exception {
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(1L)).thenReturn(messenger);
        when(serviceDeliveryPort.save(argThat(s -> s.getCurrentStatus() == Status.PENDING))).thenReturn(service);

        updateServiceDelivery.updateStatus(1L, Status.PENDING, "Obs con ubicación", signature, photos, 1L, 4.6, -74.1);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.PENDING));
    }

    @Test
    @DisplayName("Debe actualizar con observación nula y fotos nulas")

    void shouldUpdateWithNullObservationAndNullPhotos() throws Exception {
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(2L)).thenReturn(admin);
        when(serviceDeliveryPort.save(argThat(s -> s.getCurrentStatus() == Status.CANCELED))).thenReturn(service);

        updateServiceDelivery.updateStatus(1L, Status.CANCELED, null, null, null, 2L);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.CANCELED));
    }

    @Test
    @DisplayName("Debe actualizar con observación vacía")

    void shouldUpdateWithEmptyObservation() throws Exception {
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(2L)).thenReturn(admin);
        when(serviceDeliveryPort.save(argThat(s -> s.getCurrentStatus() == Status.CANCELED))).thenReturn(service);

        updateServiceDelivery.updateStatus(1L, Status.CANCELED, "", null, null, 2L);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.CANCELED));
    }

    @Test
    @DisplayName("Debe lanzar excepción en reasignación si el administrador es nulo")

    void shouldThrowReassignIfAdminIsNull() {
        service.setCurrentStatus(Status.CANCELED);
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(99L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateServiceDelivery.reassignMessenger(1L, 3L, 99L));

        assertEquals("Solo los administradores pueden reasignar servicios.", ex.getMessage());
    }

    @Test
    @DisplayName("testValidateEvidence con estado fallido debe continuar")

    void testValidateEvidence_FailedStatus_FallsThrough() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
            updateServiceDelivery.validateEvidence(Status.FAILED, null, null, null, Role.MESSENGER)
        );
    }

    @Test
    @DisplayName("testValidateEvidence pendiente con observación nula debe lanzar excepción")

    void testValidateEvidence_Pending_NullObservation_ThrowsException() {
        List<Photo> validPhotos = List.of(new Photo());
        assertThrows(BusinessException.class, () ->
            updateServiceDelivery.validateEvidence(Status.PENDING, new Signature(), validPhotos, null, Role.MESSENGER)
        );
    }

    @Test
    @DisplayName("Debe manejar fotos no nulas vacías al actualizar")

    void shouldHandleNonNullEmptyPhotosOnUpdate() throws Exception {
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(2L)).thenReturn(admin);
        when(serviceDeliveryPort.save(argThat(s -> s.getCurrentStatus() == Status.RESOLVED))).thenReturn(service);

        service.setCurrentStatus(Status.PENDING);
        updateServiceDelivery.updateStatus(1L, Status.RESOLVED, "Resuelto", null, new ArrayList<>(), 2L);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.RESOLVED));
    }

    @Test
    @DisplayName("No debe guardar rastreo cuando solo se proporciona latitud")

    void shouldNotSaveTrackingWhenOnlyLatitudeProvided() throws Exception {
        when(serviceDeliveryPort.findByIdActive(1L)).thenReturn(service);
        when(employeePort.findById(1L)).thenReturn(messenger);
        when(serviceDeliveryPort.save(argThat(s -> s.getCurrentStatus() == Status.PENDING))).thenReturn(service);

        updateServiceDelivery.updateStatus(1L, Status.PENDING, "Obs parcial", signature, photos, 1L, 4.6, null);

        verify(serviceDeliveryPort).save(argThat(s -> s.getCurrentStatus() == Status.PENDING));
    }
}
