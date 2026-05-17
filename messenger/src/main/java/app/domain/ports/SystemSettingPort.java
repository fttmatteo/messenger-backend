package app.domain.ports;

import app.domain.model.SystemSetting;
import java.util.Optional;

/**
 * Puerto para la persistencia de configuraciones del sistema.
 */
public interface SystemSettingPort {
    Optional<SystemSetting> findByKey(String key);

    SystemSetting save(SystemSetting setting);
}
