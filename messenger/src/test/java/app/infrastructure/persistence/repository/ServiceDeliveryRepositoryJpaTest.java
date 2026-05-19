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

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestCacheConfig.class)
@DisplayName("Pruebas unitarias de ServiceDeliveryRepositoryJpa")
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
    @DisplayName("Debe buscar por palabra clave")
@org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    
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
        plate.setPlateType(PlateType.MOTORCYCLE);
        plateRepository.save(plate);

        ServiceDeliveryEntity service = new ServiceDeliveryEntity();
        service.setMessenger(messenger);
        service.setDealership(dealership);
        service.setPlate(plate);
        service.setCurrentStatus(Status.ASSIGNED);
        service.setDeleted(false);
        service.setCreatedAt(LocalDateTime.now());
        serviceDeliveryRepository.save(service);

        Page<ServiceDeliveryEntity> resultPlate = serviceDeliveryRepository.searchAll("%KJH%", "KJH*", false, null,
                PageRequest.of(0, 10));

        Page<ServiceDeliveryEntity> resultName = serviceDeliveryRepository.searchAll("%Juan%", "Juan*", false, null,
                PageRequest.of(0, 10));

        assertEquals(1, resultPlate.getTotalElements());
        assertEquals(service.getIdServiceDelivery(), resultPlate.getContent().get(0).getIdServiceDelivery());
        assertEquals(1, resultName.getTotalElements());
        assertEquals(service.getIdServiceDelivery(), resultName.getContent().get(0).getIdServiceDelivery());

        serviceDeliveryRepository.deleteAll();
        plateRepository.deleteAll();
        dealershipRepository.deleteAll();
        employeeRepository.deleteAll();
    }

}
