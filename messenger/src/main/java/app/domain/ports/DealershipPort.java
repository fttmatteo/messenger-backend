package app.domain.ports;

import app.domain.model.Dealership;
import java.util.List;

/**
 * Puerto de salida para persistencia de concesionarios.
 */
public interface DealershipPort {

    /**
     * Guarda o actualiza un concesionario.
     */
    Dealership save(Dealership dealership);

    /**
     * Elimina un concesionario por su ID.
     */
    void deleteById(Long idDealership);

    /**
     * Busca un concesionario por su ID.
     */
    Dealership findById(Long idDealership);

    /**
     * Busca un concesionario por su nombre exacto.
     */
    Dealership findByName(String name);

    /**
     * Recupera todos los concesionarios registrados.
     */
    List<Dealership> findAll();

    /**
     * Busca un concesionario por su PIN de WhatsApp.
     */
    Dealership findByWhatsappPin(String whatsappPin);

    /**
     * Busca un concesionario por su UUID público.
     */
    Dealership findByUuid(String uuid);
}