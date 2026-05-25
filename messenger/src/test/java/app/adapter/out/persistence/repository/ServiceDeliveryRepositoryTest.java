package app.adapter.out.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import app.domain.model.enums.PlateType;
import app.domain.model.enums.Role;
import app.domain.model.enums.Status;
import app.adapter.out.persistence.entities.DealershipEntity;
import app.adapter.out.persistence.entities.EmployeeEntity;
import app.adapter.out.persistence.entities.PlateEntity;
import app.adapter.out.persistence.entities.ServiceDeliveryEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import app.support.AbstractIntegrationTest;

@Transactional
@DisplayName("Pruebas unitarias de ServiceDeliveryRepository")
class ServiceDeliveryRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ServiceDeliveryRepository repository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DealershipRepository dealershipRepository;

    @Autowired
    private PlateRepository plateRepository;




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
        p.setPlateType(PlateType.MOTORCYCLE);
        return p;
    }


    @Test
    @DisplayName("Debe buscar por palabras clave usando texto completo")
@org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    
    void shouldSearchByKeywordsUsingFullText() {
        EmployeeEntity messenger = createEmployee("111222", "Carlos Santana");
        employeeRepository.save(messenger);

        DealershipEntity dealership = createDealership("Premium Cars Bogota");
        dealershipRepository.save(dealership);

        DealershipEntity originDealer = createDealership("Origin Cars");
        dealershipRepository.save(originDealer);

        PlateEntity plate = createPlate("CHASIS0003");
        plateRepository.save(plate);

        ServiceDeliveryEntity service = new ServiceDeliveryEntity();
        service.setMessenger(messenger);
        service.setDealership(dealership);
        service.setOriginDealership(originDealer);
        service.setPlate(plate);
        service.setCurrentStatus(Status.ASSIGNED);
        service.setCreatedAt(LocalDateTime.now());
        service.setDeleted(false);
        repository.save(service);

        org.springframework.data.domain.Page<ServiceDeliveryEntity> search1 = repository.searchAll(
                "%Carlos%", "Carlos*", false, null, org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(search1.getContent()).hasSize(1);

        org.springframework.data.domain.Page<ServiceDeliveryEntity> search2 = repository.searchAll(
                "%Premium%", "Premium*", false, null, org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(search2.getContent()).hasSize(1);

        org.springframework.data.domain.Page<ServiceDeliveryEntity> search3 = repository.searchAll(
                "%CHASIS0003%", "CHASIS0003*", false, null, org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(search3.getContent()).hasSize(1);

        repository.deleteAll();
        plateRepository.deleteAll();
        dealershipRepository.deleteAll();
        employeeRepository.deleteAll();
    }
}
