package app.adapter.out.persistence.adapter;

import app.adapter.out.persistence.entities.TimelineEventEntity;
import app.adapter.out.persistence.repository.TimelineEventRepository;
import app.domain.model.TimelineEvent;
import app.domain.model.enums.Status;
import app.domain.ports.TimelineEventPort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TimelineEventAdapter implements TimelineEventPort {

    private final TimelineEventRepository repository;

    public TimelineEventAdapter(TimelineEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public TimelineEvent save(TimelineEvent event) {
        TimelineEventEntity entity = mapToEntity(event);
        TimelineEventEntity savedEntity = repository.save(entity);
        return mapToDomain(savedEntity);
    }

    @Override
    public List<TimelineEvent> findByMessengerIdAndDate(Long messengerId, LocalDate date) {
        return repository.findByMessengerIdAndEventDateOrderByTimestampDesc(messengerId, date)
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    private TimelineEventEntity mapToEntity(TimelineEvent domain) {
        TimelineEventEntity entity = new TimelineEventEntity();
        entity.setId(domain.getId());
        entity.setMessengerId(domain.getMessengerId());
        entity.setEventDate(domain.getEventDate());
        entity.setTimestamp(domain.getTimestamp());
        entity.setStatus(domain.getStatus() != null ? domain.getStatus().name() : null);
        entity.setPlateNumber(domain.getPlateNumber());
        entity.setDealershipName(domain.getDealershipName());
        entity.setLatitude(domain.getLatitude());
        entity.setLongitude(domain.getLongitude());
        entity.setChangedByName(domain.getChangedByName());
        entity.setChangedByRole(domain.getChangedByRole());
        return entity;
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TimelineEventAdapter.class);

    private TimelineEvent mapToDomain(TimelineEventEntity entity) {
        TimelineEvent domain = new TimelineEvent();
        domain.setId(entity.getId());
        domain.setMessengerId(entity.getMessengerId());
        domain.setEventDate(entity.getEventDate());
        domain.setTimestamp(entity.getTimestamp());
        
        try {
            if (entity.getStatus() != null) {
                domain.setStatus(Status.valueOf(entity.getStatus()));
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Estado inválido '{}' encontrado en TimelineEvent con ID {}", entity.getStatus(), entity.getId());
        }
        
        domain.setPlateNumber(entity.getPlateNumber());
        domain.setDealershipName(entity.getDealershipName());
        domain.setLatitude(entity.getLatitude());
        domain.setLongitude(entity.getLongitude());
        domain.setChangedByName(entity.getChangedByName());
        domain.setChangedByRole(entity.getChangedByRole());
        return domain;
    }
}
