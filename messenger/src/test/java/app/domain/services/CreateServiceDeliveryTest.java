package app.domain.services;
import app.application.usecase.delivery.CreateServiceDelivery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import app.domain.exception.BusinessException;
import app.domain.model.Dealership;
import app.domain.model.Employee;
import app.domain.model.Plate;
import app.domain.model.enums.PlateType;
import app.domain.model.enums.Status;
import app.domain.ports.DealershipPort;
import app.domain.ports.EmployeePort;
import app.domain.ports.PlatePort;
import app.domain.ports.ServiceDeliveryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de CreateServiceDelivery")
class CreateServiceDeliveryTest {

    @Mock
    private ServiceDeliveryPort serviceDeliveryPort;
    @Mock
    private PlatePort platePort;
    @Mock
    private DealershipPort dealershipPort;
    @Mock
    private EmployeePort employeePort;
    @Mock
    private PlateRecognition plateRecognition;
    @Mock
    private app.domain.ports.TrackingPort trackingPort;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private CreateServiceDelivery createServiceDelivery;

    private Employee messenger;
    private Dealership dealership;
    private Dealership dealershipOrigin;
    private Plate plate;

    @BeforeEach
    void setUp() {
        messenger = new Employee();
        messenger.setIdEmployee(1L);
        messenger.setDocument(12345678L);
        messenger.setFullName("Juan Perez");

        dealership = new Dealership();
        dealership.setIdDealership(1L);
        dealership.setName("Concesionario Central");

        dealershipOrigin = new Dealership();
        dealershipOrigin.setIdDealership(2L);
        dealershipOrigin.setName("Concesionario Origen");

        plate = new Plate();
        plate.setIdPlate(1L);
        plate.setPlateNumber("ABC1234567");
        plate.setPlateType(PlateType.MOTORCYCLE);

        lenient().when(serviceDeliveryPort.findAllPaginated(anyString(), anyBoolean(), isNull(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el chasis o placa ya existe")
    void shouldThrowExceptionIfPlateAlreadyExists() {
        when(employeePort.findById(12345678L)).thenReturn(messenger);
        when(dealershipPort.findById(1L)).thenReturn(dealership);
        when(dealershipPort.findById(2L)).thenReturn(dealershipOrigin);
        when(serviceDeliveryPort.findAllPaginated(eq("ABC1234567"), eq(false), isNull(), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of(new app.domain.model.ServiceDelivery())));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> createServiceDelivery.create("ABC1234567", 1L, 2L, 12345678L, null, null));

        assertEquals("El chasis ya tiene un servicio registrado en el sistema.", exception.getMessage());
        verify(serviceDeliveryPort, never()).save(any());
    }

    @Test
    @DisplayName("Debe crear servicio y chasis cuando no existe")
    void shouldCreateServiceAndNewPlateWhenPlateDoesNotExist() throws Exception {
        when(employeePort.findById(12345678L)).thenReturn(messenger);
        when(dealershipPort.findById(1L)).thenReturn(dealership);
        when(dealershipPort.findById(2L)).thenReturn(dealershipOrigin);
        when(platePort.findByPlateNumber("NNN999")).thenReturn(null);
        when(plateRecognition.determinePlateType("NNN999")).thenReturn(PlateType.MOTORCYCLE);

        app.domain.model.ServiceDelivery savedService = new app.domain.model.ServiceDelivery();
        savedService.setIdServiceDelivery(100L);
        savedService.setPlate(new Plate());
        savedService.getPlate().setPlateNumber("NNN999");
        savedService.setCurrentStatus(Status.ASSIGNED);

        when(serviceDeliveryPort.save(any())).thenReturn(savedService);

        createServiceDelivery.create("NNN999", 1L, 2L, 12345678L, null, null);

        verify(platePort).save(argThat(newPlate -> newPlate.getPlateNumber().equals("NNN999") &&
                newPlate.getPlateType() == PlateType.MOTORCYCLE));

        verify(serviceDeliveryPort).save(argThat(service -> service.getPlate().getPlateNumber().equals("NNN999") &&
                service.getCurrentStatus() == Status.ASSIGNED &&
                service.getOriginDealership().getIdDealership().equals(2L) &&
                service.getDealership().getIdDealership().equals(1L)));

        verify(eventPublisher).publishEvent(any(app.domain.events.PlateStatusChangedEvent.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el mensajero no se encuentra")
    void shouldThrowExceptionIfMessengerNotFound() {
        when(employeePort.findById(anyLong())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> createServiceDelivery.create("ABC1234567", 1L, 2L, 99999L, null, null));

        assertEquals("El mensajero no existe.", exception.getMessage());
        verify(serviceDeliveryPort, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el concesionario no se encuentra")
    void shouldThrowExceptionIfDealershipNotFound() {
        when(employeePort.findById(12345678L)).thenReturn(messenger);
        when(dealershipPort.findById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> createServiceDelivery.create("ABC1234567", 999L, 2L, 12345678L, null, null));

        assertEquals("El concesionario indicado no existe.", exception.getMessage());
        verify(serviceDeliveryPort, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el concesionario de origen es nulo")
    void shouldThrowExceptionIfOriginDealershipIsNull() {
        when(employeePort.findById(12345678L)).thenReturn(messenger);
        when(dealershipPort.findById(1L)).thenReturn(dealership);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> createServiceDelivery.create("ABC1234567", 1L, null, 12345678L, null, null));

        assertEquals("El concesionario de origen es obligatorio.", exception.getMessage());
        verify(serviceDeliveryPort, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el concesionario de origen no existe")
    void shouldThrowExceptionIfOriginDealershipNotFound() {
        when(employeePort.findById(12345678L)).thenReturn(messenger);
        when(dealershipPort.findById(1L)).thenReturn(dealership);
        when(dealershipPort.findById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> createServiceDelivery.create("ABC1234567", 1L, 999L, 12345678L, null, null));

        assertEquals("El concesionario de origen indicado no existe.", exception.getMessage());
        verify(serviceDeliveryPort, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el concesionario de origen es el mismo que el destino")
    void shouldThrowExceptionIfOriginDealershipEqualsDestination() {
        when(employeePort.findById(12345678L)).thenReturn(messenger);
        when(dealershipPort.findById(1L)).thenReturn(dealership);
        when(dealershipPort.findById(1L)).thenReturn(dealership); // same target

        BusinessException exception = assertThrows(BusinessException.class,
                () -> createServiceDelivery.create("ABC1234567", 1L, 1L, 12345678L, null, null));

        assertEquals("El concesionario de origen no puede ser el mismo que el destino.", exception.getMessage());
        verify(serviceDeliveryPort, never()).save(any());
    }

    @Test
    @DisplayName("Debe normalizar placa o chasis a mayúsculas")
    void shouldNormalizePlateToUpperCase() throws Exception {
        when(employeePort.findById(12345678L)).thenReturn(messenger);
        when(dealershipPort.findById(1L)).thenReturn(dealership);
        when(dealershipPort.findById(2L)).thenReturn(dealershipOrigin);
        when(platePort.findByPlateNumber("ABC1234567")).thenReturn(plate);

        app.domain.model.ServiceDelivery savedService = new app.domain.model.ServiceDelivery();
        savedService.setIdServiceDelivery(100L);
        when(serviceDeliveryPort.save(any())).thenReturn(savedService);

        createServiceDelivery.create("abc1234567", 1L, 2L, 12345678L, null, null);

        verify(platePort).findByPlateNumber("ABC1234567");
    }

    @Test
    @DisplayName("Debe guardar historial de rastreo cuando se proporciona la ubicación")
    void shouldSaveTrackingHistoryWhenLocationIsProvided() throws Exception {
        when(employeePort.findById(12345678L)).thenReturn(messenger);
        when(dealershipPort.findById(1L)).thenReturn(dealership);
        when(dealershipPort.findById(2L)).thenReturn(dealershipOrigin);
        when(platePort.findByPlateNumber("ABC1234567")).thenReturn(plate);

        app.domain.model.ServiceDelivery savedService = new app.domain.model.ServiceDelivery();
        savedService.setIdServiceDelivery(100L);
        when(serviceDeliveryPort.save(any())).thenReturn(savedService);

        createServiceDelivery.create("ABC1234567", 1L, 2L, 12345678L, 6.2442, -75.5812);

        verify(trackingPort).saveTrackingHistory(argThat(tracking -> tracking.getMessengerId().equals(1L) &&
                tracking.getServiceDeliveryId().equals(100L) &&
                tracking.getLocation().getLatitude().equals(6.2442) &&
                tracking.getLocation().getLongitude().equals(-75.5812) &&
                tracking.getSource() == app.domain.model.enums.TrackingSource.MANUAL));
    }
}
