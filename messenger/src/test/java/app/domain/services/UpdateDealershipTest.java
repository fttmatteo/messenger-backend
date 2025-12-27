package app.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import app.domain.exception.BusinessException;
import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateDealership Unit Tests")
class UpdateDealershipTest {

    @Mock
    private DealershipPort dealershipPort;

    @InjectMocks
    private UpdateDealership updateDealership;

    private Dealership existingDealership;

    @BeforeEach
    void setUp() {
        existingDealership = new Dealership();
        existingDealership.setIdDealership(1L);
        existingDealership.setName("Original Name");
        existingDealership.setAddress("Calle 1");
        existingDealership.setPhone("111");
        existingDealership.setZone("Sur");
    }

    @Test
    @DisplayName("Debe actualizar campos exitosamente")
    /**
     * Verifica que los campos del concesionario se actualicen correctamente si las
     * validaciones pasan.
     */
    void shouldUpdateFieldsSuccessfully() throws Exception {
        Dealership newData = new Dealership();
        newData.setName("New Name");
        newData.setAddress("Calle 2");
        newData.setPhone("222");
        newData.setZone("Norte");

        when(dealershipPort.findById(1L)).thenReturn(existingDealership);
        when(dealershipPort.findByName("New Name")).thenReturn(null);
        when(dealershipPort.save(any())).thenReturn(existingDealership);

        updateDealership.update(1L, newData);

        verify(dealershipPort).save(argThat(d -> d.getName().equals("New Name") &&
                d.getAddress().equals("Calle 2") &&
                d.getPhone().equals("222") &&
                d.getZone().equals("Norte")));
    }

    @Test
    @DisplayName("Debe lanzar excepción si ID no existe")
    /**
     * Verifica que no se pueda actualizar un concesionario inexistente.
     */
    void shouldThrowExceptionIfIdNotFound() {
        when(dealershipPort.findById(1L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> updateDealership.update(1L, new Dealership()));

        assertEquals("El concesionario con ID 1 no existe.", ex.getMessage());
    }

    @Test
    @DisplayName("Debe lanzar excepción si nuevo nombre ya existe")
    /**
     * Verifica la validación de nombre único durante la actualización.
     */
    void shouldThrowExceptionIfNewNameExists() {
        Dealership newData = new Dealership();
        newData.setName("Existing Name");

        when(dealershipPort.findById(1L)).thenReturn(existingDealership);
        when(dealershipPort.findByName("Existing Name")).thenReturn(new Dealership());

        BusinessException ex = assertThrows(BusinessException.class, () -> updateDealership.update(1L, newData));

        assertEquals("Ya existe otro concesionario con el nombre Existing Name", ex.getMessage());
    }

    @Test
    @DisplayName("No debe validar nombre si no cambia")
    void shouldNotValidateNameIfSame() throws Exception {
        Dealership newData = new Dealership();
        newData.setName("Original Name"); // Same name
        newData.setAddress("Calle 2");

        when(dealershipPort.findById(1L)).thenReturn(existingDealership);
        when(dealershipPort.save(any())).thenReturn(existingDealership);

        updateDealership.update(1L, newData);

        verify(dealershipPort).save(any());
    }
}
