package app.application.usecase;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.model.Dealership;
import app.domain.services.CreateDealership;
import app.domain.services.DeleteDealership;
import app.domain.services.SearchDealership;
import app.domain.services.UpdateDealership;

/**
 * Caso de uso para gestión de concesionarios.
 */
@Service
public class DealershipUseCase {

    private static final Logger logger = LoggerFactory.getLogger(DealershipUseCase.class);

    @Autowired
    private CreateDealership createDealership;
    @Autowired
    private UpdateDealership updateDealership;
    @Autowired
    private SearchDealership searchDealership;
    @Autowired
    private DeleteDealership deleteDealership;

    public Dealership create(Dealership dealership) throws Exception {
        logger.info("Creando concesionario: {}", dealership.getName());
        Dealership created = createDealership.create(dealership);
        logger.info("Concesionario creado con ID: {}", created.getIdDealership());
        return created;
    }

    public Dealership update(Long id, Dealership dealership) throws Exception {
        logger.info("Actualizando concesionario ID: {}", id);
        Dealership updated = updateDealership.update(id, dealership);
        logger.info("Concesionario ID: {} actualizado", id);
        return updated;
    }

    public Dealership findById(Long id) throws Exception {
        return searchDealership.findById(id);
    }

    public Dealership findByName(String name) throws Exception {
        return searchDealership.findByName(name);
    }

    public List<Dealership> findAll() {
        return searchDealership.findAll();
    }

    public void deleteById(Long id) throws Exception {
        logger.warn("Eliminando concesionario ID: {}", id);
        deleteDealership.deleteById(id);
        logger.info("Concesionario ID: {} eliminado", id);
    }
}