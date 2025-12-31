package app.domain.model;

import java.time.LocalDateTime;

/**
 * Modelo que representa una configuración del sistema (par clave-valor).
 */
public class SystemSetting {
    private String key;
    private String value;
    private LocalDateTime updatedAt;

    public SystemSetting() {
    }

    /**
     * Constructor con clave y valor.
     */
    public SystemSetting(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
