package app.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "dealerships")
@Data
public class DealershipEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDealership;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, length = 100)
    private String address;

    @Column(nullable = false, length = 10)
    private String phone;

    @Column(nullable = false, length = 10)
    private String zone;
}