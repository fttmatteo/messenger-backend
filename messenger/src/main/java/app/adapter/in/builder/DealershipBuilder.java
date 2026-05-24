package app.adapter.in.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.adapter.in.rest.dealership.DealershipRequest;
import app.adapter.in.rest.dealership.DealershipValidator;
import app.domain.model.Dealership;

/**
 * Builder que transforma DealershipRequest en modelo de dominio con validación.
 */
@Component
public class DealershipBuilder {

    @Autowired
    private DealershipValidator validator;

    public Dealership build(DealershipRequest request) throws Exception {
        Dealership dealership = new Dealership();
        dealership.setName(validator.nameValidator(request.getName()));
        dealership.setAddress(validator.addressValidator(request.getAddress()));
        dealership.setPhone(validator.phoneValidator(request.getPhone()));
        dealership.setZone(validator.zoneValidator(request.getZone()));
        dealership.setWhatsappPin(request.getWhatsappPin());
        return dealership;
    }
}
