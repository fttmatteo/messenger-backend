package app.domain.model;

import app.domain.model.enums.WhatsAppConversationState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

/**
 * Representa una sesión activa de WhatsApp.
 * Vincula un número de teléfono con un concesionario autenticado.
 */
public class WhatsAppSession {
    private Long id;
    private String phoneNumber;
    private Dealership dealership;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private Integer currentPage;
    private String lastFilterStatuses;
    private LocalDateTime lastActivityAt;
    private boolean timeoutNotified;
    private WhatsAppConversationState conversationState;

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

    public Dealership getDealership() {
        return dealership;
    }

    public void setDealership(Dealership dealership) {
        this.dealership = dealership;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getCurrentPage() {
        return currentPage != null ? currentPage : 0;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public String getLastFilterStatuses() {
        return lastFilterStatuses;
    }

    public void setLastFilterStatuses(String lastFilterStatuses) {
        this.lastFilterStatuses = lastFilterStatuses;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public boolean isTimeoutNotified() {
        return timeoutNotified;
    }

    public void setTimeoutNotified(boolean timeoutNotified) {
        this.timeoutNotified = timeoutNotified;
    }

    public WhatsAppConversationState getConversationState() {
        return conversationState != null ? conversationState : WhatsAppConversationState.MENU;
    }

    public void setConversationState(WhatsAppConversationState conversationState) {
        this.conversationState = conversationState;
    }

    @JsonIgnore
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
