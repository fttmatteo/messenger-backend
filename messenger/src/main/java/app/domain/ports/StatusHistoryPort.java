package app.domain.ports;

import app.domain.model.StatusHistory;
import java.util.List;

public interface StatusHistoryPort {
    StatusHistory save(StatusHistory statusHistory);

    StatusHistory update(StatusHistory statusHistory);

    void deleteById(Long idStatusHistory);

    StatusHistory findById(Long idStatusHistory);

    List<StatusHistory> findAll();

    List<StatusHistory> findByServiceDeliveryId(Long serviceDeliveryId);
}