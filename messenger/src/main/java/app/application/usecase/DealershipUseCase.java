package app.application.usecase;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    /**
     * Crea un nuevo concesionario en el sistema.
     */
    @CacheEvict(value = "dealerships", allEntries = true)
    @app.infrastructure.audit.AuditableAction(action = "CREATE_DEALERSHIP", description = "Crear nuevo concesionario")
    public Dealership create(Dealership dealership) throws Exception {
        Dealership created = createDealership.create(dealership);
        return created;
    }

    /**
     * Actualiza la información de un concesionario existente.
     */
    @CacheEvict(value = "dealerships", allEntries = true)
    @app.infrastructure.audit.AuditableAction(action = "UPDATE_DEALERSHIP", description = "Actualizar concesionario")
    public Dealership update(Long id, Dealership dealership) throws Exception {
        Dealership updated = updateDealership.update(id, dealership);
        return updated;
    }

    /**
     * Busca un concesionario por su ID.
     */
    @Cacheable(value = "dealerships", key = "'id:' + #id")
    public Dealership findById(Long id) throws Exception {
        Dealership dealership = searchDealership.findById(id);
        if (dealership == null) {
            throw new RuntimeException("Concesionario no encontrado con ID: " + id);
        }
        return dealership;
    }

    /**
     * Busca un concesionario por su nombre exacto.
     */
    public Dealership findByName(String name) throws Exception {
        return searchDealership.findByName(name);
    }

    /**
     * Recupera todos los concesionarios registrados.
     */
    @Cacheable(value = "dealerships", key = "'all'")
    public List<Dealership> findAll() {
        return searchDealership.findAll();
    }

    /**
     * Elimina un concesionario por su ID.
     */
    @CacheEvict(value = "dealerships", allEntries = true)
    @app.infrastructure.audit.AuditableAction(action = "DELETE_DEALERSHIP", description = "Eliminar concesionario")
    public void deleteById(Long id) throws Exception {
        deleteDealership.deleteById(id);
        logger.warn("Eliminando concesionario ID: {}", id);
    }
}