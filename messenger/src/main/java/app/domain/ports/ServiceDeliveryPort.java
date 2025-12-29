package app.domain.ports;

import app.domain.model.ServiceDelivery;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Puerto de salida para persistencia de servicios de entrega.
 */
public interface ServiceDeliveryPort {

    /**
     * Guarda o actualiza un servicio de entrega.
     */
    ServiceDelivery save(ServiceDelivery serviceDelivery);

    /**
     * Mueve un servicio a la papelera (Soft delete).
     */
    void deleteById(Long idServiceDelivery);

    /**
     * Busca un servicio por ID (incluyendo eliminados).
     */
    ServiceDelivery findById(Long idServiceDelivery);

    /**
     * Recupera todos los servicios (incluyendo eliminados).
     */
    List<ServiceDelivery> findAll();

    /**
     * Busca servicios activos asociados a una placa.
     */
    List<ServiceDelivery> findByPlateNumber(String plateNumber);

    /**
     * Busca servicios asignados a un mensajero.
     */
    List<ServiceDelivery> findByMessengerId(Long messengerId);

    // Métodos para soft delete (papelera)

    /**
     * Recupera todos los servicios activos.
     */
    List<ServiceDelivery> findAllActive();

    /**
     * Busca un servicio activo por ID.
     */
    ServiceDelivery findByIdActive(Long idServiceDelivery);

    /**
     * Recupera todos los servicios marcados como eliminados.
     */
    List<ServiceDelivery> findDeleted();

    // Páginas paginadas

    /**
     * Recupera servicios con paginación.
     * 
     * @param deleted  Filtro por estado de eliminación (false = activos, true =
     *                 papelera)
     * @param pageable Configuración de paginación y ordenamiento
     * @return Página de servicios
     */
    Page<ServiceDelivery> findAllPaginated(Boolean deleted, Pageable pageable);

    /**
     * Recupera servicios de un mensajero específico con paginación.
     * 
     * @param messengerId ID del mensajero
     * @param deleted     Filtro por estado de eliminación
     * @param pageable    Configuración de paginación y ordenamiento
     * @return Página de servicios del mensajero
     */
    Page<ServiceDelivery> findByMessengerPaginated(Long messengerId, Boolean deleted, Pageable pageable);

    /**
     * Busca servicios eliminados hace más tiempo que la fecha indicada.
     */
    List<ServiceDelivery> findDeletedExpiredBefore(LocalDateTime date);

    /**
     * Elimina físicamente un servicio de la base de datos.
     */
    void hardDeleteById(Long idServiceDelivery);

    /**
     * Elimina físicamente todos los servicios en la papelera.
     */
    int hardDeleteAllDeleted();

    // Estadísticas diarias por mensajero

    /**
     * Obtiene estadísticas diarias de un mensajero en un rango de fechas.
     */
    List<app.domain.model.DailyStatistics> findDailyStatsByMessenger(
            Long messengerId,
            java.time.LocalDate from,
            java.time.LocalDate to);
}