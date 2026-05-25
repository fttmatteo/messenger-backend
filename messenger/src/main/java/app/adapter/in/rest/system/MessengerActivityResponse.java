package app.adapter.in.rest.system;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Respuesta con datos de actividad del mensajero para el panel de monitoreo.
 */
public class MessengerActivityResponse {

    private List<ActivityEvent> timeline;

    public MessengerActivityResponse() {
    }

    public MessengerActivityResponse(List<ActivityEvent> timeline) {
        this.timeline = timeline;
    }

    public List<ActivityEvent> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<ActivityEvent> timeline) {
        this.timeline = timeline;
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
