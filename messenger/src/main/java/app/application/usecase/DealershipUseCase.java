package app.application.usecase;

import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import app.domain.model.Dealership;
import app.application.usecase.dealership.CreateDealership;
import app.application.usecase.dealership.DeleteDealership;
import app.application.usecase.dealership.SearchDealership;
import app.application.usecase.dealership.UpdateDealership;

/**
 * Caso de uso para gestión de concesionarios.
 */
@Service
public class DealershipUseCase {

    private final CreateDealership createDealership;
    private final UpdateDealership updateDealership;
    private final SearchDealership searchDealership;
    private final DeleteDealership deleteDealership;

    public DealershipUseCase(
            CreateDealership createDealership,
            UpdateDealership updateDealership,
            SearchDealership searchDealership,
            DeleteDealership deleteDealership) {
        this.createDealership = createDealership;
        this.updateDealership = updateDealership;
        this.searchDealership = searchDealership;
        this.deleteDealership = deleteDealership;
    }

    /**
     * Crea un nuevo concesionario en el sistema.
     */
    @CacheEvict(value = "dealerships", allEntries = true)
    public Dealership create(Dealership dealership) throws Exception {
        Dealership created = createDealership.create(dealership);

        return created;
    }

    /**
     * Actualiza la información de un concesionario existente.
     */
    @CacheEvict(value = "dealerships", allEntries = true)
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
            throw new RuntimeException("Concesionario no encontrado.");
        }
        return dealership;
    }

    /**
     * Busca un concesionario por su UUID público.
     */
    public Dealership findByUuid(String uuid) {
        return searchDealership.findByUuid(uuid);
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
    public void deleteById(Long id) throws Exception {
        deleteDealership.deleteById(id);

    }
}