package app.domain.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.Dealership;
import app.domain.ports.DealershipPort;

/**
 * Servicio de dominio para crear nuevos concesionarios.
 * 
 * Valida la unicidad del nombre antes de crear el concesionario.
 */
@Service
public class CreateDealership {

    private static final Logger logger = LoggerFactory.getLogger(CreateDealership.class);

    @Autowired
    private DealershipPort dealershipPort;

    /**
     * Crea un nuevo concesionario en el sistema.
     * 
     * @param dealership Concesionario a crear.
     * @return El concesionario creado con su ID asignado.
     * @throws Exception Si ya existe un concesionario con el mismo nombre.
     */
    public Dealership create(Dealership dealership) throws Exception {
        logger.debug("Creando concesionario: {}", dealership.getName());
        Dealership existing = dealershipPort.findByName(dealership.getName());
        if (existing != null) {
            throw new BusinessException("Ya existe un concesionario con el nombre " + dealership.getName());
        }
        Dealership saved = dealershipPort.save(dealership);
        logger.info("Concesionario creado exitosamente: {}", saved.getName());
        return saved;
    }
}