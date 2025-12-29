package app.application.usecase;

import app.domain.model.SystemSetting;
import app.domain.ports.SystemSettingPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SystemSettingUseCase {

    private static final Logger logger = LoggerFactory.getLogger(SystemSettingUseCase.class);

    @Autowired
    private SystemSettingPort systemSettingPort;

    private static final String STATUS_COLORS_KEY = "STATUS_COLORS";

    public String getStatusColors() {
        try {
            return systemSettingPort.findByKey(STATUS_COLORS_KEY)
                    .map(SystemSetting::getValue)
                    .orElse("{}");
        } catch (Exception e) {
            logger.error("Error al obtener colores de estados (posible tabla faltante): {}", e.getMessage());
            return "{}"; // Fallback para evitar 500
        }
    }

    public void updateStatusColors(String colorsJson) {
        SystemSetting setting = systemSettingPort.findByKey(STATUS_COLORS_KEY)
                .orElse(new SystemSetting(STATUS_COLORS_KEY, colorsJson));

        setting.setValue(colorsJson);
        setting.setUpdatedAt(LocalDateTime.now());
        systemSettingPort.save(setting);
    }
}
