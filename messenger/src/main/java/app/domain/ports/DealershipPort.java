package app.domain.ports;

import app.domain.model.Dealership;
import java.util.List;

/**
 * Puerto (interfaz) para operaciones de persistencia de concesionarios.
 * 
 * Define las operaciones necesarias para gestionar concesionarios de vehículos,
 * incluyendo datos de ubicación y contacto.
 */
public interface DealershipPort {
    /**
     * Guarda o actualiza un concesionario en la base de datos.
     * 
     * @param dealership Concesionario a guardar.
     * @return El concesionario guardado con su ID asignado.
     */
    Dealership save(Dealership dealership);

    /**
     * Elimina un concesionario por su ID.
     * 
     * @param idDealership ID del concesionario a eliminar.
     */
    void deleteById(Long idDealership);

    /**
     * Elimina un concesionario por su nombre.
     * 
     * @param dealershipName Nombre del concesionario a eliminar.
     */
    void deleteByName(String dealershipName);

    /**
     * Busca un concesionario por su ID.
     * 
     * @param idDealership ID del concesionario.
     * @return Concesionario encontrado o null si no existe.
     */
    Dealership findById(Long idDealership);

    /**
     * Busca un concesionario por su nombre exacto.
     * 
     * @param name Nombre del concesionario.
     * @return Concesionario encontrado o null si no existe.
     */
    Dealership findByName(String name);

    /**
     * Obtiene todos los concesionarios registrados.
     * 
     * @return Lista de todos los concesionarios.
     */
    List<Dealership> findAll();
}