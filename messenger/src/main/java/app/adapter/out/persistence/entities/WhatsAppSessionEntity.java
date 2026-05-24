package app.adapter.out.persistence.entities;

import app.domain.model.enums.WhatsAppConversationState;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA para sesiones de WhatsApp.
 */
@Entity
@Table(name = "wa_sessions")
public class WhatsAppSessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealership_id", nullable = true)
    private DealershipEntity dealership;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "current_page")
    private Integer currentPage;

    @Column(name = "last_filter_statuses", length = 500)
    private String lastFilterStatuses;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "timeout_notified")
    private boolean timeoutNotified;

    @Enumerated(EnumType.STRING)
    @Column(name = "conversation_state")
    private WhatsAppConversationState conversationState = WhatsAppConversationState.MENU;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (lastActivityAt == null) {
            lastActivityAt = createdAt;
        }
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

    public DealershipEntity getDealership() {
        return dealership;
    }

    public void setDealership(DealershipEntity dealership) {
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
        return currentPage;
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
        return conversationState;
    }

    public void setConversationState(WhatsAppConversationState conversationState) {
        this.conversationState = conversationState;
    }
}
