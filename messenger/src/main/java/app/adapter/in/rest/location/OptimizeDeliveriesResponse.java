package app.adapter.in.rest.location;

import java.util.List;

/**
 * DTO de respuesta con la lista ordenada de paradas y metadatos de ruta.
 */
public class OptimizeDeliveriesResponse {
    private List<DeliveryRouteStepResponse> steps;
    private Double distanceMeters;
    private Double distanceKilometers;
    private Long durationSeconds;
    private String durationFormatted;
    private String polyline;

    public OptimizeDeliveriesResponse() {
    }

    public List<DeliveryRouteStepResponse> getSteps() {
        return steps;
    }

    public void setSteps(List<DeliveryRouteStepResponse> steps) {
        this.steps = steps;
    }

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(Double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public Double getDistanceKilometers() {
        return distanceKilometers;
    }

    public void setDistanceKilometers(Double distanceKilometers) {
        this.distanceKilometers = distanceKilometers;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getDurationFormatted() {
        return durationFormatted;
    }

    public void setDurationFormatted(String durationFormatted) {
        this.durationFormatted = durationFormatted;
    }

    public String getPolyline() {
        return polyline;
    }

    public void setPolyline(String polyline) {
        this.polyline = polyline;
    }

    /**
     * DTO interno para representar una parada individual.
     */
    public static class DeliveryRouteStepResponse {
        private String serviceUuid;
        private String action; // "PICKUP" o "DELIVERY"
        private Long dealershipId;
        private String dealershipName;
        private Double latitude;
        private Double longitude;
        private Integer order;

        public DeliveryRouteStepResponse() {
        }

        public DeliveryRouteStepResponse(String serviceUuid, String action, Long dealershipId, String dealershipName, Double latitude, Double longitude, Integer order) {
            this.serviceUuid = serviceUuid;
            this.action = action;
            this.dealershipId = dealershipId;
            this.dealershipName = dealershipName;
            this.latitude = latitude;
            this.longitude = longitude;
            this.order = order;
        }

        public String getServiceUuid() {
            return serviceUuid;
        }

        public void setServiceUuid(String serviceUuid) {
            this.serviceUuid = serviceUuid;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public Long getDealershipId() {
            return dealershipId;
        }

        public void setDealershipId(Long dealershipId) {
            this.dealershipId = dealershipId;
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

        public Integer getOrder() {
            return order;
        }

        public void setOrder(Integer order) {
            this.order = order;
        }
    }
}
