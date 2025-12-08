package app.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "signatures")
@Data
public class SignatureEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSignature;

    @Column(nullable = false)
    private String signaturePath;

    @Column(nullable = false)
    private LocalDateTime uploadDate;
}