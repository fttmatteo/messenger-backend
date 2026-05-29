package app.domain.model;

import java.time.LocalDateTime;

public class WhatsAppUserTerms {
    private Long id;
    private String phoneNumber;
    private LocalDateTime acceptedAt;

    public WhatsAppUserTerms(Long id, String phoneNumber, LocalDateTime acceptedAt) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.acceptedAt = acceptedAt;
    }

    public WhatsAppUserTerms() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }
}
