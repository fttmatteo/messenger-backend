package app.messenger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.adapter.in.rest.request.DealershipRequest;
import app.adapter.in.rest.request.EmployeeRequest;
import app.domain.model.enums.Role;
import app.infrastructure.persistence.entities.EmployeeEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Full Business Flow E2E Integration Test")
class FullBusinessFlowIntegrationTest {

        @Autowired
        private WebApplicationContext context;

        @Autowired
        private EntityManager entityManager;

        private ObjectMapper objectMapper = new ObjectMapper();

        @Autowired
        private PasswordEncoder passwordEncoder;

        private MockMvc mockMvc;

        @BeforeEach
        public void setup() {
                mockMvc = MockMvcBuilders
                                .webAppContextSetup(context)
                                .apply(springSecurity())
                                .build();
        }

        @Test
        @DisplayName("Complete happy path for a service delivery cycle")
        void shouldCompleteFullBusinessCycle() throws Exception {
                // 1. Setup Admin User directly in DB to start the process
                EmployeeEntity adminEntity = new EmployeeEntity();
                adminEntity.setDocument(999999L);
                adminEntity.setFullName("Master Admin");
                adminEntity.setPassword(passwordEncoder.encode("admin123"));
                adminEntity.setRole(Role.ADMIN);
                entityManager.persist(adminEntity);
                entityManager.flush();

                // 2. Admin creates a Messenger
                EmployeeRequest messengerRequest = new EmployeeRequest();
                messengerRequest.setDocument("888888");
                messengerRequest.setFullName("John Doe Messenger");
                messengerRequest.setPhone("3000000000");
                messengerRequest.setRole("MESSENGER");
                messengerRequest.setPassword("Secure@123");

                MvcResult messengerResult = mockMvc.perform(post("/employees/createEmployee")
                                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .user("999999").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(messengerRequest)))
                                .andExpect(status().isCreated())
                                .andReturn();

                Long messengerId = objectMapper.readTree(messengerResult.getResponse().getContentAsString())
                                .get("idEmployee")
                                .asLong();

                // 3. Admin creates a Dealership
                DealershipRequest dealershipRequest = new DealershipRequest();
                dealershipRequest.setName("Central Motors");
                dealershipRequest.setAddress("Main St 456");
                dealershipRequest.setPhone("3001234567");
                dealershipRequest.setZone("Centro");

                MvcResult dealershipResult = mockMvc.perform(post("/dealerships/createDealership")
                                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .user("999999").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dealershipRequest)))
                                .andExpect(status().isCreated())
                                .andReturn();

                Long dealershipId = objectMapper.readTree(dealershipResult.getResponse().getContentAsString())
                                .get("idDealership").asLong();

                // 4. Admin creates a Service (simulating manual entry)
                MockMultipartFile imageFile = new MockMultipartFile("image", "plate.png", "image/png",
                                "fake-image-content".getBytes());

                MvcResult serviceResult = mockMvc.perform(multipart("/services/createService")
                                .file(imageFile)
                                .param("dealershipId", dealershipId.toString())
                                .param("messengerId", messengerId.toString())
                                .param("manualPlateNumber", "XYZ789")
                                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .user("999999").roles("ADMIN")))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.plate.plateNumber", org.hamcrest.Matchers.is("XYZ789")))
                                .andReturn();

                Long serviceId = objectMapper.readTree(serviceResult.getResponse().getContentAsString())
                                .get("idServiceDelivery").asLong();

                // 5. Messenger updates status to DELIVERED
                MockMultipartFile signatureFile = new MockMultipartFile("signature", "sign.png", "image/png",
                                "fake-signature".getBytes());
                MockMultipartFile photo1 = new MockMultipartFile("photos", "photo1.png", "image/png",
                                "fake-photo1".getBytes());

                mockMvc.perform(multipart("/services/updateService/" + serviceId)
                                .file(signatureFile)
                                .file(photo1)
                                .param("status", "DELIVERED")
                                .param("observation", "Everything clear")
                                .with(request -> {
                                        request.setMethod("PUT");
                                        return request;
                                })
                                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .user("888888").roles("MESSENGER")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.currentStatus", org.hamcrest.Matchers.is("DELIVERED")));

                // 6. Verify result
                mockMvc.perform(get("/services/findByServiceId/" + serviceId)
                                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                                                .user("999999").roles("ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.history",
                                                org.hamcrest.Matchers.hasSize(org.hamcrest.Matchers.greaterThan(0))));
        }
}
