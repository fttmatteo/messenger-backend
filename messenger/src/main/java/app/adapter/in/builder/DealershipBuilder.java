package app.adapter.in.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.domain.model.Dealership;
import app.adapter.in.validators.DealershipValidator;

@Component
public class DealershipBuilder {

    @Autowired
    private DealershipValidator validator;

    public Dealership build(String name, String address, String phone, String zone) throws Exception {
        Dealership dealership = new Dealership();
        dealership.setName(validator.nameValidator(name));
        dealership.setAddress(validator.addressValidator(address));
        dealership.setPhone(validator.phoneValidator(phone));
        dealership.setZone(validator.zoneValidator(zone));
        return dealership;
    }
}
