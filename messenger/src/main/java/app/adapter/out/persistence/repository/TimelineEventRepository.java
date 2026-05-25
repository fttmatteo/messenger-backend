package app.adapter.out.persistence.repository;

import app.adapter.out.persistence.entities.TimelineEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimelineEventRepository extends JpaRepository<TimelineEventEntity, Long> {
    
    List<TimelineEventEntity> findByMessengerIdAndEventDateOrderByTimestampDesc(Long messengerId, LocalDate eventDate);
}
