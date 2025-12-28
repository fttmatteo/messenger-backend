package app.infrastructure.persistence.mapper;

import app.domain.model.SystemSetting;
import app.infrastructure.persistence.entities.SystemSettingEntity;
import org.springframework.stereotype.Component;

@Component
public class SystemSettingMapper {

    public SystemSetting toDomain(SystemSettingEntity entity) {
        if (entity == null)
            return null;
        SystemSetting domain = new SystemSetting();
        domain.setKey(entity.getKey());
        domain.setValue(entity.getValue());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }

    public SystemSettingEntity toEntity(SystemSetting domain) {
        if (domain == null)
            return null;
        SystemSettingEntity entity = new SystemSettingEntity();
        entity.setKey(domain.getKey());
        entity.setValue(domain.getValue());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
