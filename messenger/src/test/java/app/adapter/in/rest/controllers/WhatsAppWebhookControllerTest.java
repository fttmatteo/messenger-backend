package app.adapter.in.rest.controllers;

import app.domain.services.WhatsAppBotService;
import app.infrastructure.config.WhatsAppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
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

        @MockitoBean
        private RedisTemplate<String, String> redisTemplate;

        private ValueOperations<String, String> valueOperations;

        @SuppressWarnings("unchecked")
        @BeforeEach
        void setUp() {
                when(config.getVerifyToken()).thenReturn("test-token");
                when(config.getAppSecret()).thenReturn("");
                valueOperations = mock(ValueOperations.class);
                when(redisTemplate.opsForValue()).thenReturn(valueOperations);
                when(valueOperations.setIfAbsent(anyString(), anyString(), any())).thenReturn(true);
        }

        @Test
        /**
         * Verifica que el webhook procese mensajes de texto.
         */
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
        /**
         * Verifica que el webhook procese respuestas de botón.
         */
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
        /**
         * Verifica que el webhook procese respuestas de lista.
         */
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

        @Test
        /**
         * Verifica que el webhook ignore mensajes duplicados.
         */
        void shouldIgnoreDuplicateMessage() throws Exception {
                when(valueOperations.setIfAbsent(anyString(), anyString(), any())).thenReturn(false);

                String json = "{" +
                                "\"object\":\"whatsapp_business_account\"," +
                                "\"entry\":[{" +
                                "\"id\":\"123\"," +
                                "\"changes\":[{" +
                                "\"value\":{" +
                                "\"messaging_product\":\"whatsapp\"," +
                                "\"messages\":[{" +
                                "\"from\":\"573001234567\"," +
                                "\"id\":\"msg_duplicate\"," +
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

                verify(botService, never()).processMessage(anyString(), anyString());
        }
}
