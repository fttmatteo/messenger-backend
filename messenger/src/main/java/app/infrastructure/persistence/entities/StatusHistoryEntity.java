package app.infrastructure.persistence.entities;

import app.domain.model.enums.Status;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "status_history")
@Data
public class StatusHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHistory;

    @Enumerated(EnumType.STRING)
    private Status previousStatus;

    @Enumerated(EnumType.STRING)
    private Status newStatus;

    private LocalDateTime changeDate;

    @ManyToOne
    @JoinColumn(name = "changed_by_employee_id")
    private EmployeeEntity changedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_delivery_id")
    private ServiceDeliveryEntity serviceDelivery;
}