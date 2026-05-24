package app.messenger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import app.adapter.in.rest.dealership.DealershipRequest;
import app.adapter.in.rest.employee.EmployeeRequest;
import app.domain.model.enums.Role;
import app.adapter.out.persistence.entities.EmployeeEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import app.support.AbstractIntegrationTest;

@Transactional
@DisplayName("Pruebas unitarias de FullBusinessFlow Integration")
class FullBusinessFlowIntegrationTest extends AbstractIntegrationTest {

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

        private byte[] createValidPngImage(int width, int height) throws IOException {
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                return baos.toByteArray();
        }

        @Test
        @DisplayName("Debe completar el ciclo de negocio completo")

        void shouldCompleteFullBusinessCycle() throws Exception {
                EmployeeEntity adminEntity = new EmployeeEntity();
                adminEntity.setDocument(999999L);
                adminEntity.setFullName("Master Admin");
                adminEntity.setPassword(passwordEncoder.encode("admin123"));
                adminEntity.setRole(Role.ADMIN);
                entityManager.persist(adminEntity);
                entityManager.flush();

                EmployeeRequest messengerRequest = new EmployeeRequest();
                messengerRequest.setDocument("888888");
                messengerRequest.setFullName("John Doe Messenger");
                messengerRequest.setPhone("3000000000");
                messengerRequest.setRole("MESSENGER");
                messengerRequest.setPassword("Secure@123");

                MvcResult messengerResult = mockMvc.perform(post("/employees/createEmployee")
                                .with(user("999999").roles("ADMIN"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(messengerRequest)))
                                .andExpect(status().isCreated())
                                .andReturn();

                Long messengerId = objectMapper.readTree(messengerResult.getResponse().getContentAsString())
                                .get("idEmployee")
                                .asLong();

                DealershipRequest dealershipRequest = new DealershipRequest();
                dealershipRequest.setName("Central Motors " + System.currentTimeMillis());
                dealershipRequest.setAddress("Main St 456");
                dealershipRequest.setPhone("3001234567");
                dealershipRequest.setZone("Centro");

                MvcResult dealershipResult = mockMvc.perform(post("/dealerships/createDealership")
                                .with(user("999999").roles("ADMIN"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dealershipRequest)))
                                .andExpect(status().isCreated())
                                .andReturn();

                Long dealershipId = objectMapper.readTree(dealershipResult.getResponse().getContentAsString())
                                .get("idDealership").asLong();

                DealershipRequest originDealershipRequest = new DealershipRequest();
                originDealershipRequest.setName("Origin Motors " + System.currentTimeMillis());
                originDealershipRequest.setAddress("Origin St 123");
                originDealershipRequest.setPhone("3009876543");
                originDealershipRequest.setZone("Norte");

                MvcResult originDealershipResult = mockMvc.perform(post("/dealerships/createDealership")
                                .with(user("999999").roles("ADMIN"))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(originDealershipRequest)))
                                .andExpect(status().isCreated())
                                .andReturn();

                Long originDealershipId = objectMapper.readTree(originDealershipResult.getResponse().getContentAsString())
                                .get("idDealership").asLong();

                byte[] validImageBytes = createValidPngImage(800, 600);
                MockMultipartFile imageFile = new MockMultipartFile("image", "plate.png", "image/png",
                                validImageBytes);

                MvcResult serviceResult = mockMvc.perform(multipart("/services/createService")
                                .file(imageFile)
                                .param("dealershipId", dealershipId.toString())
                                .param("originDealershipId", originDealershipId.toString())
                                .param("messengerId", messengerId.toString())
                                .param("manualPlateNumber", "XYZ7890123")
                                .with(user("999999").roles("ADMIN"))
                                .with(csrf()))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.plate.plateNumber", org.hamcrest.Matchers.is("XYZ7890123")))
                                .andReturn();

                String serviceUuid = objectMapper.readTree(serviceResult.getResponse().getContentAsString())
                                .get("uuid").asText();

                byte[] validSignatureBytes = createValidPngImage(200, 100);
                byte[] validPhotoBytes = createValidPngImage(1024, 768);
                MockMultipartFile signatureFile = new MockMultipartFile("signature", "sign.png", "image/png",
                                validSignatureBytes);
                MockMultipartFile photo1 = new MockMultipartFile("photos", "photo1.png", "image/png",
                                validPhotoBytes);

                mockMvc.perform(multipart("/services/updateService/" + serviceUuid)
                                .file(signatureFile)
                                .file(photo1)
                                .param("status", "DELIVERED")
                                .param("observation", "Everything clear")
                                .with(request -> {
                                        request.setMethod("PUT");
                                        return request;
                                })
                                .with(user("888888").roles("MESSENGER"))
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.currentStatus", org.hamcrest.Matchers.is("DELIVERED")));

                mockMvc.perform(get("/services/findByServiceId/" + serviceUuid)
                                .with(user("999999").roles("ADMIN")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.history",
                                                org.hamcrest.Matchers.hasSize(org.hamcrest.Matchers.greaterThan(0))));
        }
}
