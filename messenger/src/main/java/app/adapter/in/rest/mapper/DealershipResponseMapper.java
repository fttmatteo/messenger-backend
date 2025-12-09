package app.adapter.in.rest.mapper;

import app.adapter.in.rest.response.DealershipResponse;
import app.domain.model.Dealership;
import org.springframework.stereotype.Component;

@Component
public class DealershipResponseMapper {

    public DealershipResponse toResponse(Dealership dealership) {
        if (dealership == null) {
            return null;
        }

        return new DealershipResponse(
                dealership.getIdDealership(),
                dealership.getName(),
                dealership.getAddress(),
                dealership.getPhone(),
                dealership.getZone());
    }
}
