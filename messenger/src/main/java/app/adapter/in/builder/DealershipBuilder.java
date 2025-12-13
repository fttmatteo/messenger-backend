package app.adapter.in.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.adapter.in.rest.request.DealershipRequest;
import app.adapter.in.validators.DealershipValidator;
import app.domain.model.Dealership;

/**
 * Builder para construir objetos Dealership validados desde DTOs.
 * 
 * <p>
 * Valida y construye instancias de Dealership aplicando reglas de negocio.
 * </p>
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
        return dealership;
    }
}
