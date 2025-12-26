package app.adapter.out.ocr;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoogleVisionAdapter Unit Tests")
class GoogleVisionAdapterTest {

    @InjectMocks
    private GoogleVisionAdapter googleVisionAdapter;

    @Test
    @DisplayName("Debe existir el adapter")
    /**
     * Prueba básica de existencia e inyección del componente de visión.
     */
    void shouldExist() {
        assertNotNull(googleVisionAdapter);
    }
}
