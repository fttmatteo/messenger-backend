package app.adapter.out.whatsapp;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.junit.jupiter.api.Assertions.*;

import app.infrastructure.config.WhatsAppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

@DisplayName("Pruebas unitarias de WhatsAppCloudClient")
class WhatsAppCloudClientTest {

    private WhatsAppCloudClient whatsAppCloudClient;
    private MockRestServiceServer mockServer;
    private WhatsAppConfig config;

    @BeforeEach
    void setUp() {
        config = new WhatsAppConfig();
        config.setAccessToken("test-token");
        config.setPhoneNumberId("test-phone-id");
        config.setApiUrl("https://graph.facebook.com/v21.0");

        whatsAppCloudClient = new WhatsAppCloudClient(config);

        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(whatsAppCloudClient, "restTemplate");
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    @DisplayName("Debe enviar mensaje de texto exitosamente")
    void shouldSendTextMessageSuccessfully() {
        mockServer.expect(requestTo("https://graph.facebook.com/v21.0/test-phone-id/messages"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.messaging_product").value("whatsapp"))
                .andExpect(jsonPath("$.to").value("5730000000"))
                .andExpect(jsonPath("$.type").value("text"))
                .andExpect(jsonPath("$.text.body").value("Hello World"))
                .andRespond(withSuccess(
                        "{\"messaging_product\": \"whatsapp\", \"contacts\": [{\"input\": \"5730000000\", \"wa_id\": \"5730000000\"}], \"messages\": [{\"id\": \"msg_123\"}]}",
                        MediaType.APPLICATION_JSON));

        boolean result = whatsAppCloudClient.sendTextMessage("5730000000", "Hello World");

        assertTrue(result);
        mockServer.verify();
    }

    @Test
    @DisplayName("Debe enviar ubicación exitosamente")
    void shouldSendLocationSuccessfully() {
        mockServer.expect(requestTo("https://graph.facebook.com/v21.0/test-phone-id/messages"))
                .andExpect(jsonPath("$.type").value("location"))
                .andExpect(jsonPath("$.location.latitude").value(4.6))
                .andRespond(withSuccess());

        boolean result = whatsAppCloudClient.sendLocation("5730000000", 4.6, -74.0, "Place", "Address");

        assertTrue(result);
        mockServer.verify();
    }

    @Test
    @DisplayName("Debe enviar botones de respuesta exitosamente")
    void shouldSendReplyButtonsSuccessfully() {
        mockServer.expect(requestTo("https://graph.facebook.com/v21.0/test-phone-id/messages"))
                .andExpect(jsonPath("$.type").value("interactive"))
                .andExpect(jsonPath("$.interactive.type").value("button"))
                .andExpect(jsonPath("$.interactive.action.buttons[0].reply.title").value("Yes"))
                .andRespond(withSuccess());

        boolean result = whatsAppCloudClient.sendReplyButtons("5730000000", "Body", List.of("Yes", "No"),
                List.of("id1", "id2"));

        assertTrue(result);
        mockServer.verify();
    }

    @Test
    @DisplayName("Debe retornar falso ante un error de API")
    void shouldReturnFalseOnApiError() {
        mockServer.expect(requestTo("https://graph.facebook.com/v21.0/test-phone-id/messages"))
                .andRespond(withBadRequest());

        boolean result = whatsAppCloudClient.sendTextMessage("5730000000", "Fail");

        assertFalse(result);
        mockServer.verify();
    }
}
