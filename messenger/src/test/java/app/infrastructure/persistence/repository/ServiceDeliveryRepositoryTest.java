package app.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import app.domain.model.enums.PlateType;
import app.domain.model.enums.Role;
import app.domain.model.enums.Status;
import app.infrastructure.persistence.entities.DealershipEntity;
import app.infrastructure.persistence.entities.EmployeeEntity;
import app.infrastructure.persistence.entities.PlateEntity;
import app.infrastructure.persistence.entities.ServiceDeliveryEntity;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import app.support.AbstractIntegrationTest;

@Transactional
@DisplayName("ServiceDeliveryRepository Integration Tests")
class ServiceDeliveryRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ServiceDeliveryRepository repository;

    @Test
    @DisplayName("Should correctly calculate daily statistics")
    /**
     * Verifica que la query de estadísticas diarias agrupe y cuente correctamente
     * por estado.
     */
    void shouldCalculateDailyStatsCorrectly() {
        EmployeeEntity messenger = createEmployee("999999", "Test Messenger");
        entityManager.persist(messenger);

        DealershipEntity dealership = createDealership("Test Dealer");
        entityManager.persist(dealership);

        PlateEntity plate = createPlate("TEST001");
        entityManager.persist(plate);

        LocalDateTime fixedDate = LocalDateTime.of(2025, 12, 29, 12, 0);

        createAndPersistService(messenger, dealership, plate, Status.ASSIGNED, fixedDate);
        createAndPersistService(messenger, dealership, plate, Status.DELIVERED, fixedDate);
        createAndPersistService(messenger, dealership, plate, Status.RETURNED, fixedDate);
        createAndPersistService(messenger, dealership, plate, Status.CANCELED, fixedDate);

        entityManager.flush();

        List<Object[]> stats = repository.findDailyStatsByMessenger(
                messenger.getIdEmployee(),
                fixedDate.toLocalDate().atStartOfDay(),
                fixedDate.toLocalDate().plusDays(1).atStartOfDay());

        assertThat(stats).hasSize(1);
        Object[] dayStats = stats.get(0);

        assertThat(((Number) dayStats[1]).longValue()).isEqualTo(1);
        assertThat(((Number) dayStats[2]).longValue()).isEqualTo(1);
        assertThat(((Number) dayStats[3]).longValue()).isEqualTo(1);
        assertThat(((Number) dayStats[4]).longValue()).isEqualTo(1);
        assertThat(((Number) dayStats[5]).longValue()).isEqualTo(4);
    }

    private void createAndPersistService(EmployeeEntity messenger, DealershipEntity dealership, PlateEntity plate,
            Status status, LocalDateTime createdAt) {
        ServiceDeliveryEntity service = new ServiceDeliveryEntity();
        service.setMessenger(messenger);
        service.setDealership(dealership);
        service.setPlate(plate);
        service.setCurrentStatus(status);
        service.setCreatedAt(createdAt);
        service.setDeleted(false);
        entityManager.persist(service);
    }

    private EmployeeEntity createEmployee(String document, String name) {
        EmployeeEntity e = new EmployeeEntity();
        e.setDocument(Long.parseLong(document));
        e.setFullName(name);
        e.setPassword("pass");
        e.setRole(Role.MESSENGER);
        return e;
    }

    private DealershipEntity createDealership(String name) {
        DealershipEntity d = new DealershipEntity();
        d.setName(name);
        d.setAddress("Address");
        d.setPhone("1234567890");
        d.setZone("Zone");
        return d;
    }

    private PlateEntity createPlate(String number) {
        PlateEntity p = new PlateEntity();
        p.setPlateNumber(number);
        p.setPlateType(PlateType.CAR);
        return p;
    }
}
