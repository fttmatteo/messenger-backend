package app.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración para WhatsApp Cloud API.
 */
@Configuration
@ConfigurationProperties(prefix = "whatsapp")
public class WhatsAppConfig {

    private String phoneNumberId;
    private String accessToken;
    private String verifyToken;
    private String appSecret;
    private String apiUrl = "https://graph.facebook.com/v21.0";
    private int sessionExpirationHours = 12;

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getPhoneNumberId() {
        return phoneNumberId;
    }

    public void setPhoneNumberId(String phoneNumberId) {
        this.phoneNumberId = phoneNumberId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getVerifyToken() {
        return verifyToken;
    }

    public void setVerifyToken(String verifyToken) {
        this.verifyToken = verifyToken;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public int getSessionExpirationHours() {
        return sessionExpirationHours;
    }

    public void setSessionExpirationHours(int sessionExpirationHours) {
        this.sessionExpirationHours = sessionExpirationHours;
    }

    /**
     * Construye la URL para enviar mensajes.
     */
    public String getMessagesUrl() {
        return apiUrl + "/" + phoneNumberId + "/messages";
    }
}
