package app.application.usecase;

import app.domain.model.SystemSetting;
import app.domain.ports.SystemSettingPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de SystemSettingUseCase")
class SystemSettingUseCaseTest {

    @Mock
    private SystemSettingPort systemSettingPort;

    @InjectMocks
    private SystemSettingUseCase systemSettingUseCase;

    @Test
    @DisplayName("Debe retornar colores de estado")
    void shouldGetStatusColors() {
        String json = "{\"ASSIGNED\":\"#000000\"}";
        when(systemSettingPort.findByKey("STATUS_COLORS"))
                .thenReturn(Optional.of(new SystemSetting("STATUS_COLORS", json)));

        String result = systemSettingUseCase.getStatusColors();

        assertEquals(json, result);
        verify(systemSettingPort).findByKey("STATUS_COLORS");
    }

    @Test
    @DisplayName("Debe retornar JSON vacío si falta la configuración")

    void shouldReturnEmptyJsonIfSettingMissing() {
        when(systemSettingPort.findByKey("STATUS_COLORS")).thenReturn(Optional.empty());

        String result = systemSettingUseCase.getStatusColors();

        assertEquals("{}", result);
    }

    @Test
    @DisplayName("Debe actualizar los colores de estado")

    void shouldUpdateStatusColors() {
        String newJson = "{\"DELIVERED\":\"#FFFFFF\"}";
        when(systemSettingPort.findByKey("STATUS_COLORS")).thenReturn(Optional.empty());

        systemSettingUseCase.updateStatusColors(newJson);

        verify(systemSettingPort).save(argThat(setting -> 
            setting.getKey().equals("STATUS_COLORS") && 
            setting.getValue().equals(newJson)
        ));
    }
}
