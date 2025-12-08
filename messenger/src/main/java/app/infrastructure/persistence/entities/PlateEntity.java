package app.infrastructure.persistence.entities;

import app.domain.model.enums.PlateType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "plates")
@Data
public class PlateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPlate;

    @Column(nullable = false, unique = true)
    private String plateNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlateType plateType;

    @Column(nullable = false)
    private LocalDateTime uploadDate;
}