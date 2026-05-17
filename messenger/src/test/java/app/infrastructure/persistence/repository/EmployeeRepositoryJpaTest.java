package app.infrastructure.persistence.repository;

import app.domain.model.enums.Role;
import app.infrastructure.persistence.entities.EmployeeEntity;
import app.support.BaseContainerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import app.support.TestCacheConfig;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestCacheConfig.class)
@DisplayName("EmployeeRepository DataJpaTest")
class EmployeeRepositoryJpaTest extends BaseContainerTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("Debe encontrar empleado por documento")
    void shouldFindByDocument() {
        EmployeeEntity employee = new EmployeeEntity();
        employee.setDocument(987654321L);
        employee.setFullName("Test User");
        employee.setRole(Role.ADMIN);
        employee.setPassword("password");
        employeeRepository.save(employee);

        EmployeeEntity found = employeeRepository.findByDocument(987654321L);

        assertNotNull(found);
        assertEquals("Test User", found.getFullName());
    }
}
