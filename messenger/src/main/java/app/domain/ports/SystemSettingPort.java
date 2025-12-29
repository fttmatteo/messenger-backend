package app.domain.ports;

import app.domain.model.SystemSetting;
import java.util.Optional;

public interface SystemSettingPort {
    Optional<SystemSetting> findByKey(String key);

    SystemSetting save(SystemSetting setting);
}
