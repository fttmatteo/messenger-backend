package app.adapter.in.rest.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.adapter.in.rest.request.EmployeeRequest;
import app.domain.model.enums.Role;
import app.infrastructure.persistence.entities.EmployeeEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import app.support.AbstractIntegrationTest;

@Transactional
@DisplayName("EmployeeController Integration Tests")
class EmployeeControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EntityManager entityManager;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("GET /employees/allEmployees should return 200 for ADMIN")
    @WithMockUser(roles = "ADMIN")
    /**
     * Verifica que un admin pueda listar todos los empleados.
     */
    void shouldReturnAllEmployeesForAdmin() throws Exception {
        EmployeeEntity emp = new EmployeeEntity();
        emp.setDocument(999L);
        emp.setFullName("Test Employee");
        emp.setRole(Role.MESSENGER);
        emp.setPassword("pass");
        entityManager.persist(emp);
        entityManager.flush();

        mockMvc.perform(get("/employees/allEmployees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(org.hamcrest.Matchers.greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /employees/allEmployees should return 403 for MESSENGER")
    @WithMockUser(roles = "MESSENGER")
    /**
     * Verifica control de acceso: un mensajero no debe listar empleados.
     */
    void shouldDenyAccessToMessenger() throws Exception {
        mockMvc.perform(get("/employees/allEmployees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /employees/createEmployee should create new employee")
    @WithMockUser(roles = "ADMIN")
    /**
     * Verifica creación de empleado por un admin.
     */
    void shouldCreateEmployee() throws Exception {
        EmployeeRequest request = new EmployeeRequest();
        request.setDocument("101010");
        request.setFullName("New Employee");
        request.setPhone("3001234567");
        request.setPassword("Secure@123");
        request.setRole("MESSENGER");

        mockMvc.perform(post("/employees/createEmployee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName", is("New Employee")));
    }

    @Test
    @DisplayName("DELETE /employees/deleteEmployee/{id} should delete employee")
    @WithMockUser(roles = "ADMIN")
    /**
     * Verifica eliminación de empleado por un admin.
     */
    void shouldDeleteEmployee() throws Exception {
        EmployeeEntity emp = new EmployeeEntity();
        emp.setDocument(202020L);
        emp.setFullName("Delete Me");
        emp.setRole(Role.MESSENGER);
        emp.setPassword("pass");
        entityManager.persist(emp);
        entityManager.flush();

        mockMvc.perform(delete("/employees/deleteEmployee/" + emp.getIdEmployee()))
                .andExpect(status().isNoContent());

        EmployeeEntity deleted = entityManager.find(EmployeeEntity.class, emp.getIdEmployee());
        org.junit.jupiter.api.Assertions.assertNull(deleted);
    }
}
