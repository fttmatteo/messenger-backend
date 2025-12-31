package app.domain.ports;

import app.domain.model.SystemSetting;
import java.util.Optional;

/**
 * Puerto para la persistencia de configuraciones del sistema.
 */
public interface SystemSettingPort {
    /**
     * Busca una configuración por su clave única.
     */
    Optional<SystemSetting> findByKey(String key);

    /**
     * Guarda o actualiza una configuración del sistema.
     */
    SystemSetting save(SystemSetting setting);
}
