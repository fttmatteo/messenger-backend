package app.application.usecase;

import app.domain.model.SystemSetting;
import app.domain.ports.SystemSettingPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SystemSettingUseCase {

    @Autowired
    private SystemSettingPort systemSettingPort;

    private static final String STATUS_COLORS_KEY = "STATUS_COLORS";

    public String getStatusColors() {
        return systemSettingPort.findByKey(STATUS_COLORS_KEY)
                .map(SystemSetting::getValue)
                .orElse("{}");
    }

    public void updateStatusColors(String colorsJson) {
        SystemSetting setting = systemSettingPort.findByKey(STATUS_COLORS_KEY)
                .orElse(new SystemSetting(STATUS_COLORS_KEY, colorsJson));

        setting.setValue(colorsJson);
        setting.setUpdatedAt(LocalDateTime.now());
        systemSettingPort.save(setting);
    }
}
