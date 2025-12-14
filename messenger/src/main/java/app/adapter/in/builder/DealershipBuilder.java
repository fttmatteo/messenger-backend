package app.adapter.in.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import app.adapter.in.rest.request.DealershipRequest;
import app.adapter.in.validators.DealershipValidator;
import app.domain.model.Dealership;

/**
 * Componente encargado de la construcción de objetos {@link Dealership}.
 *
 * Aplica validaciones de reglas de negocio a través de
 * {@link DealershipValidator}
 * antes de crear la instancia del modelo de dominio.
 */
@Component
public class DealershipBuilder {

    @Autowired
    private DealershipValidator validator;

    /**
     * Construye una instancia de Dealership a partir de la solicitud.
     *
     * Valida cada campo (nombre, dirección, teléfono, zona) antes de asignarlo.
     *
     * @param request Datos de entrada para la creación/actualización del
     *                concesionario.
     * @return Instancia de {@link Dealership} validada.
     * @throws Exception Si alguna validación de campo falla.
     */
    public Dealership build(DealershipRequest request) throws Exception {
        Dealership dealership = new Dealership();
        dealership.setName(validator.nameValidator(request.getName()));
        dealership.setAddress(validator.addressValidator(request.getAddress()));
        dealership.setPhone(validator.phoneValidator(request.getPhone()));
        dealership.setZone(validator.zoneValidator(request.getZone()));
        return dealership;
    }
}
