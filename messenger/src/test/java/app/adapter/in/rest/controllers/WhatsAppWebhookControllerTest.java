package app.adapter.in.rest.controllers;

import app.domain.services.WhatsAppBotService;
import app.infrastructure.config.WhatsAppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WhatsAppWebhookController.class)
@AutoConfigureMockMvc(addFilters = false)
class WhatsAppWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WhatsAppBotService botService;

    @MockitoBean
    private WhatsAppConfig config;

    @MockitoBean
    private app.domain.ports.AuthenticationPort authenticationPort;

    @BeforeEach
    void setUp() {
        when(config.getVerifyToken()).thenReturn("test-token");
        when(config.getAppSecret()).thenReturn(""); // Deshabilitar validación de firma para simplicidad en test
    }

    @Test
    void shouldProcessTextMessage() throws Exception {
        String json = "{" +
                "\"object\":\"whatsapp_business_account\"," +
                "\"entry\":[{" +
                "\"id\":\"123\"," +
                "\"changes\":[{" +
                "\"value\":{" +
                "\"messaging_product\":\"whatsapp\"," +
                "\"metadata\":{\"display_phone_number\":\"123\",\"phone_number_id\":\"456\"}," +
                "\"contacts\":[{\"profile\":{\"name\":\"Test\"},\"wa_id\":\"573001234567\"}]," +
                "\"messages\":[{" +
                "\"from\":\"573001234567\"," +
                "\"id\":\"msg123\"," +
                "\"timestamp\":\"123456789\"," +
                "\"type\":\"text\"," +
                "\"text\":{\"body\":\"Hola\"}" +
                "}]" +
                "}," +
                "\"field\":\"messages\"" +
                "}]" +
                "}]" +
                "}";

        mockMvc.perform(post("/api/whatsapp/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        verify(botService).processMessage("573001234567", "Hola");
    }

    @Test
    void shouldProcessButtonReply() throws Exception {
        String json = "{" +
                "\"object\":\"whatsapp_business_account\"," +
                "\"entry\":[{" +
                "\"id\":\"123\"," +
                "\"changes\":[{" +
                "\"value\":{" +
                "\"messaging_product\":\"whatsapp\"," +
                "\"messages\":[{" +
                "\"from\":\"573001234567\"," +
                "\"type\":\"interactive\"," +
                "\"interactive\":{" +
                "\"type\":\"button_reply\"," +
                "\"button_reply\":{\"id\":\"MENU_BACK\",\"title\":\"Atrás\"}" +
                "}" +
                "}]" +
                "}," +
                "\"field\":\"messages\"" +
                "}]" +
                "}]" +
                "}";

        mockMvc.perform(post("/api/whatsapp/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        verify(botService).processMessage("573001234567", "MENU_BACK");
    }

    @Test
    void shouldProcessListReply() throws Exception {
        String json = "{" +
                "\"object\":\"whatsapp_business_account\"," +
                "\"entry\":[{" +
                "\"id\":\"123\"," +
                "\"changes\":[{" +
                "\"value\":{" +
                "\"messaging_product\":\"whatsapp\"," +
                "\"messages\":[{" +
                "\"from\":\"573001234567\"," +
                "\"type\":\"interactive\"," +
                "\"interactive\":{" +
                "\"type\":\"list_reply\"," +
                "\"list_reply\":{\"id\":\"2\",\"title\":\"Placas Asignadas\"}" +
                "}" +
                "}]" +
                "}," +
                "\"field\":\"messages\"" +
                "}]" +
                "}]" +
                "}";

        mockMvc.perform(post("/api/whatsapp/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        verify(botService).processMessage("573001234567", "2");
    }
}
