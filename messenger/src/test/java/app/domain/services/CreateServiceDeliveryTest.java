package app.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.domain.exception.BusinessException;
import app.domain.model.Dealership;
import app.domain.model.Employee;
import app.domain.model.Plate;
import app.domain.model.enums.PhotoType;
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
        plate.setPlateNumber("ABC123");
        plate.setPlateType(PlateType.CAR);
    }

    @Test
    @DisplayName("Debe lanzar excepción si la placa ya tiene un servicio registrado")
    /**
     * Verifica validación de servicio duplicado para una misma placa.
     */
    void shouldThrowExceptionIfPlateAlreadyExists() {
        when(employeePort.findById(12345678L)).thenReturn(messenger);
        when(dealershipPort.findById(1L)).thenReturn(dealership);
        // Simula que YA existe un servicio con esa placa
        when(serviceDeliveryPort.findByPlateNumber("ABC123"))
                .thenReturn(java.util.List.of(new app.domain.model.ServiceDelivery()));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> createServiceDelivery.create("ABC123", "path/to/photo.jpg", 1L, 12345678L));

        assertEquals("La placa ABC123 ya tiene un servicio registrado en el sistema.", exception.getMessage());
        verify(serviceDeliveryPort, never()).save(any());
    }

    @Test
    @DisplayName("Debe crear servicio y nueva placa cuando placa no existe")
    /**
     * Verifica que se cree una nueva placa y el servicio asociado si la placa es
     * nueva.
     */
    void shouldCreateServiceAndNewPlateWhenPlateDoesNotExist() throws Exception {
        when(employeePort.findById(12345678L)).thenReturn(messenger);
        when(dealershipPort.findById(1L)).thenReturn(dealership);
        when(platePort.findByPlateNumber("NNN999")).thenReturn(null);
        when(plateRecognition.determinePlateType("NNN999")).thenReturn(PlateType.MOTORCYCLE);

        createServiceDelivery.create("NNN999", null, 1L, 12345678L);

        verify(platePort).save(argThat(newPlate -> newPlate.getPlateNumber().equals("NNN999") &&
                newPlate.getPlateType() == PlateType.MOTORCYCLE));

        verify(serviceDeliveryPort).save(argThat(service -> service.getPlate().getPlateNumber().equals("NNN999") &&
                service.getCurrentStatus() == Status.ASSIGNED));
    }

    @Test
    @DisplayName("Debe lanzar excepción si mensajero no existe")
    /**
     * Verifica validación de existencia del mensajero.
     */
    void shouldThrowExceptionIfMessengerNotFound() {
        when(employeePort.findById(anyLong())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> createServiceDelivery.create("ABC123", null, 1L, 99999L));

        assertEquals("El mensajero no existe.", exception.getMessage());
        verify(serviceDeliveryPort, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si concesionario no existe")
    void shouldThrowExceptionIfDealershipNotFound() {
        when(employeePort.findById(12345678L)).thenReturn(messenger);
        when(dealershipPort.findById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> createServiceDelivery.create("ABC123", null, 999L, 12345678L));

        assertEquals("El concesionario indicado no existe.", exception.getMessage());
        verify(serviceDeliveryPort, never()).save(any());
    }

    @Test
    @DisplayName("Debe normalizar la placa a mayúsculas")
    void shouldNormalizePlateToUpperCase() throws Exception {
        when(employeePort.findById(12345678L)).thenReturn(messenger);
        when(dealershipPort.findById(1L)).thenReturn(dealership);
        // Stub for the NORMALIZED value that the service should produce
        when(platePort.findByPlateNumber("ABC123")).thenReturn(plate);

        createServiceDelivery.create("abc123", null, 1L, 12345678L);

        verify(platePort).findByPlateNumber("ABC123");
    }

    @Test
    @DisplayName("Debe agregar foto de detección si se proporciona path")
    /**
     * Verifica que si se provee path de foto, esta se adjunte al servicio inicial.
     */
    void shouldAddDetectionPhotoIfPathProvided() throws Exception {
        when(employeePort.findById(12345678L)).thenReturn(messenger);
        when(dealershipPort.findById(1L)).thenReturn(dealership);
        when(platePort.findByPlateNumber("ABC123")).thenReturn(plate);

        createServiceDelivery.create("ABC123", "uploads/plate.jpg", 1L, 12345678L);

        verify(serviceDeliveryPort)
                .save(argThat(service -> service.getPhotos().get(0).getPhotoPath().equals("uploads/plate.jpg") &&
                        service.getPhotos().get(0).getPhotoType() == PhotoType.PLATE_DETECTION));
    }
}
