package app.application.usecase.route;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import app.domain.model.*;
import app.domain.model.DeliveryRouteStep.StepAction;
import app.domain.ports.LocationPort;
import app.domain.ports.ServiceDeliveryPort;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Debe validar la optimización de rutas de entrega (PDP)")
class OptimizeDeliveriesRouteUseCaseTest {

    @Mock
    private LocationPort locationPort;

    @Mock
    private ServiceDeliveryPort serviceDeliveryPort;

    @InjectMocks
    private OptimizeDeliveriesRouteUseCase optimizeDeliveriesRoute;

    @Test
    @DisplayName("Debe ordenar correctamente las paradas respetando que PICKUP ocurra antes que DELIVERY")
    void shouldOptimizeDeliveriesRouteCorrectly() {
        Dealership origin1 = new Dealership();
        origin1.setIdDealership(1L);
        origin1.setName("Origen 1");
        origin1.setLatitude(1.0);
        origin1.setLongitude(1.0);
        origin1.setIsGeolocated(true);

        Dealership dest1 = new Dealership();
        dest1.setIdDealership(2L);
        dest1.setName("Destino 1");
        dest1.setLatitude(2.0);
        dest1.setLongitude(2.0);
        dest1.setIsGeolocated(true);

        ServiceDelivery service1 = new ServiceDelivery();
        service1.setUuid("uuid-service-1");
        service1.setOriginDealership(origin1);
        service1.setDealership(dest1);

        Dealership origin2 = new Dealership();
        origin2.setIdDealership(3L);
        origin2.setName("Origen 2");
        origin2.setLatitude(3.0);
        origin2.setLongitude(3.0);
        origin2.setIsGeolocated(true);

        Dealership dest2 = new Dealership();
        dest2.setIdDealership(4L);
        dest2.setName("Destino 2");
        dest2.setLatitude(4.0);
        dest2.setLongitude(4.0);
        dest2.setIsGeolocated(true);

        ServiceDelivery service2 = new ServiceDelivery();
        service2.setUuid("uuid-service-2");
        service2.setOriginDealership(origin2);
        service2.setDealership(dest2);

        when(serviceDeliveryPort.findByUuidActive("uuid-service-1")).thenReturn(service1);
        when(serviceDeliveryPort.findByUuidActive("uuid-service-2")).thenReturn(service2);

        Location start = new Location(0.0, 0.0);
        Route expectedRoute = new Route(start, dest2.getLocation(), Collections.emptyList(), 15000.0, 1200L, "polyline-test");

        when(locationPort.calculateRouteWithWaypoints(any(Location.class), anyList())).thenReturn(expectedRoute);

        DeliveryRoute deliveryRoute = optimizeDeliveriesRoute.execute(0.0, 0.0, List.of("uuid-service-1", "uuid-service-2"));

        assertNotNull(deliveryRoute);
        assertEquals(expectedRoute, deliveryRoute.getRouteDetails());
        List<DeliveryRouteStep> steps = deliveryRoute.getSteps();
        
        assertEquals(4, steps.size());

        int pickup1Order = -1;
        int delivery1Order = -1;
        int pickup2Order = -1;
        int delivery2Order = -1;

        for (DeliveryRouteStep step : steps) {
            if (step.getServiceUuid().equals("uuid-service-1")) {
                if (step.getAction() == StepAction.PICKUP) pickup1Order = step.getOrder();
                else if (step.getAction() == StepAction.DELIVERY) delivery1Order = step.getOrder();
            } else if (step.getServiceUuid().equals("uuid-service-2")) {
                if (step.getAction() == StepAction.PICKUP) pickup2Order = step.getOrder();
                else if (step.getAction() == StepAction.DELIVERY) delivery2Order = step.getOrder();
            }
        }

        assertTrue(pickup1Order > 0);
        assertTrue(delivery1Order > pickup1Order);
        assertTrue(pickup2Order > 0);
        assertTrue(delivery2Order > pickup2Order);

        assertEquals("uuid-service-1", steps.get(0).getServiceUuid());
        assertEquals(StepAction.PICKUP, steps.get(0).getAction());
    }

    @Test
    @DisplayName("Debe lanzar excepción si no hay servicios válidos para optimizar")
    void shouldThrowExceptionIfNoValidServices() {
        when(serviceDeliveryPort.findByUuidActive("invalid-uuid")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> 
            optimizeDeliveriesRoute.execute(0.0, 0.0, List.of("invalid-uuid"))
        );
    }
}
