package app.adapter.in.rest.mapper;

import app.adapter.in.rest.response.DealershipResponse;
import app.domain.model.Dealership;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entidades Dealership a DTOs de respuesta.
 * 
 * Transforma objetos de dominio Dealership a DealershipResponse, incluyendo
 * información de geolocalización.
 */
@Component
public class DealershipResponseMapper {

    public DealershipResponse toResponse(Dealership dealership) {
        if (dealership == null) {
            return null;
        }

        DealershipResponse response = new DealershipResponse(
                dealership.getIdDealership(),
                dealership.getName(),
                dealership.getAddress(),
                dealership.getPhone(),
                dealership.getZone());

        // Añadir campos de geolocalización
        response.setLatitude(dealership.getLatitude());
        response.setLongitude(dealership.getLongitude());
        response.setIsGeolocated(dealership.getIsGeolocated());

        return response;
    }
}
