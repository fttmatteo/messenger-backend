package app.application.usecase;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.model.Dealership;
import app.domain.services.CreateDealership;
import app.domain.services.DeleteDealership;
import app.domain.services.SearchDealership;
import app.domain.services.UpdateDealership;

/**
 * Caso de uso de aplicación para gestionar concesionarios.
 * 
 * Orquesta las operaciones CRUD de concesionarios delegando en los servicios
 * de dominio. Punto de entrada desde controladores REST.
 */
@Service
public class DealershipUseCase {

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
     * 
     * @param dealership La entidad del concesionario a crear.
     * @throws Exception Si ocurre un error durante el proceso de creación.
     */
    public void create(Dealership dealership) throws Exception {
        createDealership.create(dealership);
    }

    /**
     * Actualiza la información de un concesionario existente.
     * 
     * @param id         El ID del concesionario a actualizar.
     * @param dealership Los nuevos datos del concesionario.
     * @throws Exception Si el concesionario no existe o hay un error en la
     *                   actualización.
     */
    public void update(Long id, Dealership dealership) throws Exception {
        updateDealership.update(id, dealership);
    }

    /**
     * Busca un concesionario por su ID único.
     * 
     * @param id El ID del concesionario.
     * @return El concesionario encontrado.
     * @throws Exception Si no se encuentra el concesionario.
     */
    public Dealership findById(Long id) throws Exception {
        return searchDealership.findById(id);
    }

    /**
     * Busca un concesionario por su nombre.
     * 
     * @param name El nombre del concesionario.
     * @return El concesionario encontrado.
     * @throws Exception Si no se encuentra el concesionario.
     */
    public Dealership findByName(String name) throws Exception {
        return searchDealership.findByName(name);
    }

    /**
     * Obtiene una lista de todos los concesionarios registrados.
     * 
     * @return Lista completa de concesionarios.
     */
    public List<Dealership> findAll() {
        return searchDealership.findAll();
    }

    /**
     * Elimina un concesionario por su ID.
     * 
     * @param id El ID del concesionario a eliminar.
     * @throws Exception Si el concesionario no existe o no se puede eliminar.
     */
    public void deleteById(Long id) throws Exception {
        deleteDealership.deleteById(id);
    }

    /**
     * Elimina un concesionario por su nombre.
     * 
     * @param name El nombre del concesionario a eliminar.
     * @throws Exception Si el concesionario no existe o no se puede eliminar.
     */
    public void deleteByName(String name) throws Exception {
        deleteDealership.deleteByName(name);
    }
}