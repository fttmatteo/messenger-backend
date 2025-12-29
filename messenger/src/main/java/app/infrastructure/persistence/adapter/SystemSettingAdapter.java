package app.infrastructure.persistence.adapter;

import app.domain.model.SystemSetting;
import app.domain.ports.SystemSettingPort;
import app.infrastructure.persistence.entities.SystemSettingEntity;
import app.infrastructure.persistence.mapper.SystemSettingMapper;
import app.infrastructure.persistence.repository.SystemSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SystemSettingAdapter implements SystemSettingPort {

    @Autowired
    private SystemSettingRepository repository;

    @Autowired
    private SystemSettingMapper mapper;

    @Override
    public Optional<SystemSetting> findByKey(String key) {
        return repository.findById(key).map(mapper::toDomain);
    }

    @Override
    public SystemSetting save(SystemSetting setting) {
        SystemSettingEntity entity = mapper.toEntity(setting);
        SystemSettingEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }
}
