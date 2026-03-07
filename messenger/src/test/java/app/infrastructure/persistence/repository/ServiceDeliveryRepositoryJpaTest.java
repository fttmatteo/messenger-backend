package app.infrastructure.persistence.repository;

import app.domain.model.enums.PlateType;
import app.domain.model.enums.Role;
import app.domain.model.enums.Status;
import app.infrastructure.persistence.entities.DealershipEntity;
import app.infrastructure.persistence.entities.EmployeeEntity;
import app.infrastructure.persistence.entities.PlateEntity;
import app.infrastructure.persistence.entities.ServiceDeliveryEntity;
import app.support.BaseContainerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.context.annotation.Import;
import app.support.TestCacheConfig;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestCacheConfig.class)
@DisplayName("ServiceDeliveryRepository DataJpaTest")
/**
 * Clase de pruebas integración para el repositorio de entregas de servicios.
 */
class ServiceDeliveryRepositoryJpaTest extends BaseContainerTest {

    @Autowired
    private ServiceDeliveryRepository serviceDeliveryRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DealershipRepository dealershipRepository;

    @Autowired
    private PlateRepository plateRepository;

    @Test
    @DisplayName("Debe buscar servicios por keyword complejas")
    /**
     * Verifica que el repositorio pueda buscar servicios por keyword complejas.
     */
    void shouldSearchByKeyword() {
        EmployeeEntity messenger = new EmployeeEntity();
        messenger.setDocument(123L);
        messenger.setFullName("Juan Messenger");
        messenger.setRole(Role.MESSENGER);
        messenger.setPassword("pass");
        employeeRepository.save(messenger);

        DealershipEntity dealership = new DealershipEntity();
        dealership.setName("Concesionario Central");
        dealership.setAddress("Calle 123");
        dealership.setPhone("3001234567");
        dealership.setZone("Norte");
        dealershipRepository.save(dealership);

        PlateEntity plate = new PlateEntity();
        plate.setPlateNumber("KJH987");
        plate.setPlateType(PlateType.CAR);
        plateRepository.save(plate);

        ServiceDeliveryEntity service = new ServiceDeliveryEntity();
        service.setMessenger(messenger);
        service.setDealership(dealership);
        service.setPlate(plate);
        service.setCurrentStatus(Status.ASSIGNED);
        service.setDeleted(false);
        service.setCreatedAt(LocalDateTime.now());
        serviceDeliveryRepository.save(service);

        Page<ServiceDeliveryEntity> resultPlate = serviceDeliveryRepository.searchAll("KJH", false, null,
                PageRequest.of(0, 10));

        Page<ServiceDeliveryEntity> resultName = serviceDeliveryRepository.searchAll("Juan", false, null,
                PageRequest.of(0, 10));

        assertEquals(1, resultPlate.getTotalElements());
        assertEquals("KJH987", resultPlate.getContent().get(0).getPlate().getPlateNumber());
        assertEquals(1, resultName.getTotalElements());
        assertEquals("Juan Messenger", resultName.getContent().get(0).getMessenger().getFullName());
    }

    @Test
    @DisplayName("Debe obtener estadísticas diarias correctamente")
    /**
     * Verifica que el repositorio pueda obtener estadísticas diarias correctamente.
     */
    void shouldGetDailyStats() {
        EmployeeEntity messenger = new EmployeeEntity();
        messenger.setDocument(456L);
        messenger.setFullName("Pedro Messenger");
        messenger.setRole(Role.MESSENGER);
        messenger.setPassword("pass");
        employeeRepository.save(messenger);

        DealershipEntity dealership = new DealershipEntity();
        dealership.setName("Dealership Stats");
        dealership.setAddress("Av Siempre Viva");
        dealership.setPhone("3007654321");
        dealership.setZone("Sur");
        dealershipRepository.save(dealership);

        PlateEntity plate = new PlateEntity();
        plate.setPlateNumber("XYZ000");
        plate.setPlateType(PlateType.MOTORCYCLE);
        plateRepository.save(plate);

        ServiceDeliveryEntity s1 = new ServiceDeliveryEntity();
        s1.setMessenger(messenger);
        s1.setDealership(dealership);
        s1.setPlate(plate);
        s1.setCurrentStatus(Status.DELIVERED);
        s1.setDeleted(false);
        s1.setCreatedAt(LocalDateTime.now());
        serviceDeliveryRepository.save(s1);

        ServiceDeliveryEntity s2 = new ServiceDeliveryEntity();
        s2.setMessenger(messenger);
        s2.setDealership(dealership);
        s2.setPlate(plate);
        s2.setCurrentStatus(Status.CANCELED);
        s2.setDeleted(false);
        s2.setCreatedAt(LocalDateTime.now());
        serviceDeliveryRepository.save(s2);

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        List<Object[]> stats = serviceDeliveryRepository.findDailyStatsByMessenger(messenger.getIdEmployee(), start,
                end);

        assertFalse(stats.isEmpty());
        Object[] dayStat = stats.get(0);
        assertEquals(1L, ((Number) dayStat[2]).longValue());
        assertEquals(1L, ((Number) dayStat[4]).longValue());
        assertEquals(2L, ((Number) dayStat[5]).longValue());
    }
}
