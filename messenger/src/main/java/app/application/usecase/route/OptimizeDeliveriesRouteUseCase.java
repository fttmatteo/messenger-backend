package app.application.usecase.route;

import app.domain.model.DeliveryRoute;
import app.domain.model.DeliveryRouteStep;
import app.domain.model.DeliveryRouteStep.StepAction;
import app.domain.model.Location;
import app.domain.model.Route;
import app.domain.model.ServiceDelivery;
import app.domain.ports.LocationPort;
import app.domain.ports.ServiceDeliveryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Caso de uso encargado de calcular la ruta óptima para realizar múltiples entregas
 * con orígenes y destinos independientes (problema de recogida y entrega / PDP).
 */
@Service
public class OptimizeDeliveriesRouteUseCase {

    private static final Logger logger = LoggerFactory.getLogger(OptimizeDeliveriesRouteUseCase.class);

    private final LocationPort locationPort;
    private final ServiceDeliveryPort serviceDeliveryPort;

    public OptimizeDeliveriesRouteUseCase(LocationPort locationPort, ServiceDeliveryPort serviceDeliveryPort) {
        this.locationPort = locationPort;
        this.serviceDeliveryPort = serviceDeliveryPort;
    }

    /**
     * Calcula la secuencia óptima de paradas respetando la restricción de precedencia:
     * recoger (PICKUP) antes de entregar (DELIVERY). Luego consulta la polilínea final a Google Maps.
     */
    public DeliveryRoute execute(Double originLat, Double originLng, List<String> serviceUuids) {
        if (serviceUuids == null || serviceUuids.isEmpty()) {
            throw new IllegalArgumentException("La lista de servicios no puede estar vacía");
        }

        logger.info("Iniciando optimización de ruta (PDP) para {} servicios asignados.", serviceUuids.size());
        Location currentPos = new Location(originLat, originLng);

        List<ServiceDelivery> activeServices = new ArrayList<>();
        for (String uuid : serviceUuids) {
            ServiceDelivery service = serviceDeliveryPort.findByUuidActive(uuid);
            if (service == null) {
                logger.warn("El servicio con UUID {} no se encontró o no está activo.", uuid);
                continue;
            }
            if (service.getOriginDealership() == null || !service.getOriginDealership().getIsGeolocated() ||
                    service.getDealership() == null || !service.getDealership().getIsGeolocated()) {
                logger.warn("El servicio {} fue ignorado por falta de geolocalización en origen o destino.", uuid);
                continue;
            }
            activeServices.add(service);
        }

        if (activeServices.isEmpty()) {
            throw new IllegalArgumentException("No hay servicios válidos geolocalizados para optimizar");
        }

        List<DeliveryRouteStep> steps = new ArrayList<>();
        Set<String> pickedUpServices = new HashSet<>();
        Set<String> deliveredServices = new HashSet<>();

        int orderCounter = 0;
        Location currentLoc = currentPos;
        Long lastDealershipId = null;
        StepAction lastAction = null;

        while (deliveredServices.size() < activeServices.size()) {
            ServiceDelivery nextService = null;
            StepAction nextAction = null;
            Location nextTargetLocation = null;
            Double minDistance = Double.MAX_VALUE;

            for (ServiceDelivery s : activeServices) {
                String uuid = s.getUuid();

                if (!pickedUpServices.contains(uuid)) {
                    Location pickupLoc = s.getOriginDealership().getLocation();
                    Double dist = currentLoc.distanceTo(pickupLoc);
                    if (dist != null && dist < minDistance) {
                        minDistance = dist;
                        nextService = s;
                        nextAction = StepAction.PICKUP;
                        nextTargetLocation = pickupLoc;
                    }
                }

                if (pickedUpServices.contains(uuid) && !deliveredServices.contains(uuid)) {
                    Location deliveryLoc = s.getDealership().getLocation();
                    Double dist = currentLoc.distanceTo(deliveryLoc);
                    if (dist != null && dist < minDistance) {
                        minDistance = dist;
                        nextService = s;
                        nextAction = StepAction.DELIVERY;
                        nextTargetLocation = deliveryLoc;
                    }
                }
            }

            if (nextService == null) {
                logger.error("Inconsistencia en el ruteo: No se encontró una parada siguiente válida.");
                break;
            }

            Long currentDealershipId = (nextAction == StepAction.PICKUP) ?
                    nextService.getOriginDealership().getIdDealership() :
                    nextService.getDealership().getIdDealership();

            if (!Objects.equals(lastDealershipId, currentDealershipId) || lastAction != nextAction) {
                orderCounter++;
                lastDealershipId = currentDealershipId;
                lastAction = nextAction;
            }

            if (nextAction == StepAction.PICKUP) {
                for (ServiceDelivery s : activeServices) {
                    String uuid = s.getUuid();
                    if (!pickedUpServices.contains(uuid) &&
                            Objects.equals(s.getOriginDealership().getIdDealership(), currentDealershipId)) {
                        pickedUpServices.add(uuid);
                        steps.add(new DeliveryRouteStep(
                                uuid,
                                StepAction.PICKUP,
                                currentDealershipId,
                                s.getOriginDealership().getName(),
                                s.getOriginDealership().getLocation(),
                                orderCounter
                        ));
                    }
                }
            } else {
                for (ServiceDelivery s : activeServices) {
                    String uuid = s.getUuid();
                    if (pickedUpServices.contains(uuid) && !deliveredServices.contains(uuid) &&
                            Objects.equals(s.getDealership().getIdDealership(), currentDealershipId)) {
                        deliveredServices.add(uuid);
                        steps.add(new DeliveryRouteStep(
                                uuid,
                                StepAction.DELIVERY,
                                currentDealershipId,
                                s.getDealership().getName(),
                                s.getDealership().getLocation(),
                                orderCounter
                        ));
                    }
                }
            }

            currentLoc = nextTargetLocation;
        }

        List<Location> orderedLocations = new ArrayList<>();
        Location lastAddedLocation = null;
        for (DeliveryRouteStep step : steps) {
            Location stepLoc = step.getLocation();
            if (lastAddedLocation == null || !lastAddedLocation.equals(stepLoc)) {
                orderedLocations.add(stepLoc);
                lastAddedLocation = stepLoc;
            }
        }

        Route routeDetails = locationPort.calculateRouteWithWaypoints(currentPos, orderedLocations);

        return new DeliveryRoute(steps, routeDetails);
    }
}
