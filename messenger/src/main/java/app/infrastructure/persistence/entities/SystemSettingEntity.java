package app.infrastructure.persistence.entities;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_settings")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "system_settings")
public class SystemSettingEntity {

    @Id
    @Column(name = "setting_key")
    private String key;

    @Column(name = "setting_value", columnDefinition = "TEXT", nullable = false)
    private String value;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
