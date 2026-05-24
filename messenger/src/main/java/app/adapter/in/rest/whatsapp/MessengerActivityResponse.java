package app.adapter.in.rest.whatsapp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Respuesta con datos de actividad del mensajero para el panel de monitoreo.
 */
public class MessengerActivityResponse {

    private DailyStats dailyStats;
    private List<ActivityEvent> timeline;

    public MessengerActivityResponse() {
    }

    public MessengerActivityResponse(DailyStats dailyStats, List<ActivityEvent> timeline) {
        this.dailyStats = dailyStats;
        this.timeline = timeline;
    }

    public DailyStats getDailyStats() {
        return dailyStats;
    }

    public void setDailyStats(DailyStats dailyStats) {
        this.dailyStats = dailyStats;
    }

    public List<ActivityEvent> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<ActivityEvent> timeline) {
        this.timeline = timeline;
    }

    /**
     * Estadísticas diarias del mensajero.
     */
    public static class DailyStats {
        private int assigned;
        private int delivered;
        private int returned;
        private int canceled;
        private int pending;
        private int total;

        public DailyStats() {
        }

        public DailyStats(int assigned, int delivered, int returned, int canceled, int pending, int total) {
            this.assigned = assigned;
            this.delivered = delivered;
            this.returned = returned;
            this.canceled = canceled;
            this.pending = pending;
            this.total = total;
        }

        public int getAssigned() {
            return assigned;
        }

        public void setAssigned(int assigned) {
            this.assigned = assigned;
        }

        public int getDelivered() {
            return delivered;
        }

        public void setDelivered(int delivered) {
            this.delivered = delivered;
        }

        public int getReturned() {
            return returned;
        }

        public void setReturned(int returned) {
            this.returned = returned;
        }

        public int getCanceled() {
            return canceled;
        }

        public void setCanceled(int canceled) {
            this.canceled = canceled;
        }

        public int getPending() {
            return pending;
        }

        public void setPending(int pending) {
            this.pending = pending;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }
    }

    /**
     * Evento de actividad para la línea de tiempo.
     */
    public static class ActivityEvent {
        private Long id;
        private String status;
        private LocalDateTime timestamp;
        private String plateNumber;
        private String dealershipName;
        private Double latitude;
        private Double longitude;
        private String changedByName;
        private String changedByRole;

        public ActivityEvent() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public String getPlateNumber() {
            return plateNumber;
        }

        public void setPlateNumber(String plateNumber) {
            this.plateNumber = plateNumber;
        }

        public String getDealershipName() {
            return dealershipName;
        }

        public void setDealershipName(String dealershipName) {
            this.dealershipName = dealershipName;
        }

        public Double getLatitude() {
            return latitude;
        }

        public void setLatitude(Double latitude) {
            this.latitude = latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

        public void setLongitude(Double longitude) {
            this.longitude = longitude;
        }

        public String getChangedByName() {
            return changedByName;
        }

        public void setChangedByName(String changedByName) {
            this.changedByName = changedByName;
        }

        public String getChangedByRole() {
            return changedByRole;
        }

        public void setChangedByRole(String changedByRole) {
            this.changedByRole = changedByRole;
        }
    }
}
