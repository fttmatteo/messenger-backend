package app.domain.ports;

public interface WhatsAppUserTermsPort {
    boolean hasAccepted(String phoneNumber);
    void saveAcceptance(String phoneNumber);
}
