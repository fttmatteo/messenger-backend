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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WhatsAppWebhookController.class)
@AutoConfigureMockMvc(addFilters = false)
class WhatsAppWebhookControllerTest {

        private static final String TEST_APP_SECRET = "test-app-secret-for-unit-tests";

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
                when(config.getAppSecret()).thenReturn(TEST_APP_SECRET);
                valueOperations = mock(ValueOperations.class);
                when(redisTemplate.opsForValue()).thenReturn(valueOperations);
                when(valueOperations.setIfAbsent(anyString(), anyString(), any())).thenReturn(true);
        }

        private String computeSignature(String payload) throws Exception {
                SecretKeySpec signingKey = new SecretKeySpec(
                                TEST_APP_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(signingKey);
                byte[] rawHmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                for (byte b : rawHmac) {
                        sb.append(String.format("%02x", b));
                }
                return "sha256=" + sb;
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
                                .header("X-Hub-Signature-256", computeSignature(json))
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
                                .header("X-Hub-Signature-256", computeSignature(json))
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
                                .header("X-Hub-Signature-256", computeSignature(json))
                                .content(json))
                                .andExpect(status().isOk());

                verify(botService).processMessage("573001234567", "2");
        }

        @Test
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
                                .header("X-Hub-Signature-256", computeSignature(json))
                                .content(json))
                                .andExpect(status().isOk());

                verify(botService, never()).processMessage(anyString(), anyString());
        }

        @Test
        void shouldRejectWhenAppSecretNotConfigured() throws Exception {
                when(config.getAppSecret()).thenReturn("");

                String json = "{\"object\":\"whatsapp_business_account\",\"entry\":[]}";

                mockMvc.perform(post("/api/whatsapp/webhook")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isForbidden());

                verify(botService, never()).processMessage(anyString(), anyString());
        }

        @Test
        void shouldRejectInvalidSignature() throws Exception {
                String json = "{\"object\":\"whatsapp_business_account\",\"entry\":[]}";

                mockMvc.perform(post("/api/whatsapp/webhook")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-Hub-Signature-256", "sha256=invalidsignature000000000000000000000000000000000000000000000000")
                                .content(json))
                                .andExpect(status().isForbidden());

                verify(botService, never()).processMessage(anyString(), anyString());
        }

        @Test
        void shouldRejectMissingSignature() throws Exception {
                String json = "{\"object\":\"whatsapp_business_account\",\"entry\":[]}";

                mockMvc.perform(post("/api/whatsapp/webhook")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isForbidden());

                verify(botService, never()).processMessage(anyString(), anyString());
        }
}
