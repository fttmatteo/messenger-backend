package app.application.usecase;

import app.domain.model.SystemSetting;
import app.domain.ports.SystemSettingPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Caso de uso para la gestión de configuraciones del sistema.
 */
@Service
public class SystemSettingUseCase {

    private static final Logger logger = LoggerFactory.getLogger(SystemSettingUseCase.class);

    private final SystemSettingPort systemSettingPort;

    public SystemSettingUseCase(SystemSettingPort systemSettingPort) {
        this.systemSettingPort = systemSettingPort;
    }

    private static final String STATUS_COLORS_KEY = "STATUS_COLORS";

    /**
     * Obtiene la configuración actual de colores de estado en formato JSON.
     * Retorna un JSON vacío por defecto si no existe configuración o hay error.
     */
    public String getStatusColors() {
        try {
            return systemSettingPort.findByKey(STATUS_COLORS_KEY)
                    .map(SystemSetting::getValue)
                    .orElse("{}");
        } catch (Exception e) {
            logger.error("Error al obtener colores de estados (posible tabla faltante).", e);
            return "{}";
        }
    }

    /**
     * Actualiza o crea la configuración de colores de estado.
     */
    public void updateStatusColors(String colorsJson) {
        SystemSetting setting = systemSettingPort.findByKey(STATUS_COLORS_KEY)
                .orElse(new SystemSetting(STATUS_COLORS_KEY, colorsJson));

        setting.setValue(colorsJson);
        setting.setUpdatedAt(LocalDateTime.now());
        systemSettingPort.save(setting);
    }
}
