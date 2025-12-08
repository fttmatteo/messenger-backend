package app.infrastructure.persistence.entities;

import app.domain.model.enums.Status;
import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "service_deliveries")
@Data
public class ServiceDeliveryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idServiceDelivery;

    @ManyToOne
    @JoinColumn(name = "plate_id", nullable = false)
    private PlateEntity plate;

    @ManyToOne
    @JoinColumn(name = "dealership_id", nullable = false)
    private DealershipEntity dealership;

    @ManyToOne
    @JoinColumn(name = "messenger_id", nullable = false)
    private EmployeeEntity messenger;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status currentStatus;

    private String observation;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "signature_id")
    private SignatureEntity signature;

    @OneToMany(mappedBy = "serviceDelivery", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhotoEntity> photos = new ArrayList<>();

    @OneToMany(mappedBy = "serviceDelivery", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StatusHistoryEntity> history = new ArrayList<>();
}