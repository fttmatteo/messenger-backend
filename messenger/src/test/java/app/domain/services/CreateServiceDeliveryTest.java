package app.domain.services;

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
@DisplayName("CreateServiceDelivery Unit Tests")
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

        plate = new Plate();
        plate.setIdPlate(1L);
        plate.setPlateNumber("ABC1234567");
        plate.setPlateType(PlateType.MOTORCYCLE);

        lenient().when(serviceDeliveryPort.findAllPaginated(anyString(), anyBoolean(), isNull(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());
    }

    @Test
    @DisplayName("Debe lanzar excepción si la placa ya tiene un servicio registrado")
    void shouldThrowExceptionIfPlateAlreadyExists() {
        when(employeePort.findById(12345678L)).thenReturn(messenger);
        when(dealershipPort.findById(1L)).thenReturn(dealership);
        when(serviceDeliveryPort.findAllPaginated(eq("ABC1234567"), eq(false), isNull(), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(
                        java.util.List.of(new app.domain.model.ServiceDelivery())));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> createServiceDelivery.create("ABC1234567", 1L, 12345678L, null, null));

        assertEquals("El chasis ABC1234567 ya tiene un servicio registrado en el sistema.", exception.getMessage());
        verify(serviceDeliveryPort, never()).save(any());
    }

    @Test
    @DisplayName("Debe crear servicio y nueva placa cuando placa no existe")
    void shouldCreateServiceAndNewPlateWhenPlateDoesNotExist() throws Exception {
        when(employeePort.findById(12345678L)).thenReturn(messenger);
        when(dealershipPort.findById(1L)).thenReturn(dealership);
        when(platePort.findByPlateNumber("NNN999")).thenReturn(null);
        when(plateRecognition.determinePlateType("NNN999")).thenReturn(PlateType.MOTORCYCLE);

        app.domain.model.ServiceDelivery savedService = new app.domain.model.ServiceDelivery();
        savedService.setIdServiceDelivery(100L);
        savedService.setPlate(new Plate());
        savedService.getPlate().setPlateNumber("NNN999");
        savedService.setCurrentStatus(Status.ASSIGNED);

        when(serviceDeliveryPort.save(any())).thenReturn(savedService);

        createServiceDelivery.create("NNN999", 1L, 12345678L, null, null);

        verify(platePort).save(argThat(newPlate -> newPlate.getPlateNumber().equals("NNN999") &&
                newPlate.getPlateType() == PlateType.MOTORCYCLE));

        verify(serviceDeliveryPort).save(argThat(service -> service.getPlate().getPlateNumber().equals("NNN999") &&
                service.getCurrentStatus() == Status.ASSIGNED));

        verify(eventPublisher).publishEvent(any(app.domain.events.PlateStatusChangedEvent.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si mensajero no existe")
    void shouldThrowExceptionIfMessengerNotFound() {
        when(employeePort.findById(anyLong())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> createServiceDelivery.create("ABC1234567", 1L, 99999L, null, null));

        assertEquals("El mensajero no existe.", exception.getMessage());
        verify(serviceDeliveryPort, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si concesionario no existe")
    void shouldThrowExceptionIfDealershipNotFound() {
        when(employeePort.findById(12345678L)).thenReturn(messenger);
        when(dealershipPort.findById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> createServiceDelivery.create("ABC1234567", 999L, 12345678L, null, null));

        assertEquals("El concesionario indicado no existe.", exception.getMessage());
        verify(serviceDeliveryPort, never()).save(any());
    }

    @Test
    @DisplayName("Debe normalizar la placa a mayúsculas")
    void shouldNormalizePlateToUpperCase() throws Exception {
        when(employeePort.findById(12345678L)).thenReturn(messenger);
        when(dealershipPort.findById(1L)).thenReturn(dealership);
        when(platePort.findByPlateNumber("ABC1234567")).thenReturn(plate);

        app.domain.model.ServiceDelivery savedService = new app.domain.model.ServiceDelivery();
        savedService.setIdServiceDelivery(100L);
        when(serviceDeliveryPort.save(any())).thenReturn(savedService);

        createServiceDelivery.create("abc1234567", 1L, 12345678L, null, null);

        verify(platePort).findByPlateNumber("ABC1234567");
    }



    @Test
    @DisplayName("Debe guardar historial de rastreo inicial si se proporciona ubicación")
    void shouldSaveTrackingHistoryWhenLocationIsProvided() throws Exception {
        when(employeePort.findById(12345678L)).thenReturn(messenger);
        when(dealershipPort.findById(1L)).thenReturn(dealership);
        when(platePort.findByPlateNumber("ABC1234567")).thenReturn(plate);

        app.domain.model.ServiceDelivery savedService = new app.domain.model.ServiceDelivery();
        savedService.setIdServiceDelivery(100L);
        when(serviceDeliveryPort.save(any())).thenReturn(savedService);

        createServiceDelivery.create("ABC1234567", 1L, 12345678L, 6.2442, -75.5812);

        verify(trackingPort).saveTrackingHistory(argThat(tracking -> tracking.getMessengerId().equals(1L) &&
                tracking.getServiceDeliveryId().equals(100L) &&
                tracking.getLocation().getLatitude().equals(6.2442) &&
                tracking.getLocation().getLongitude().equals(-75.5812) &&
                tracking.getSource() == app.domain.model.enums.TrackingSource.MANUAL));
    }
}
