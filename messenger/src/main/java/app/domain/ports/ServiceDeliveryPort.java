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
     * Busca un servicio activo por ID.
     */
    ServiceDelivery findByIdActive(Long idServiceDelivery);

    /**
     * Busca un servicio activo (no eliminado) por su UUID público.
     */
    ServiceDelivery findByUuidActive(String uuid);

    /**
     * Busca un servicio por su UUID público incluyendo eliminados.
     */
    ServiceDelivery findByUuidIncludingDeleted(String uuid);

    /**
     * Recupera servicios marcados como eliminados (papelera) con paginación.
     */
    Page<ServiceDelivery> findDeleted(Pageable pageable);

    /**
     * Recupera servicios con paginación y filtro de estado.
     */
    Page<ServiceDelivery> findAllPaginated(String keyword, Boolean deleted,
            List<app.domain.model.enums.Status> statuses, Pageable pageable);

    /**
     * Recupera servicios de un mensajero específico con paginación y filtro de
     * estado.
     */
    Page<ServiceDelivery> findByMessengerPaginated(Long messengerId, String keyword, Boolean deleted,
            List<app.domain.model.enums.Status> statuses, Pageable pageable);

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

    /**
     * Obtiene estadísticas diarias de un mensajero en un rango de fechas.
     */
    List<app.domain.model.DailyStatistics> findDailyStatsByMessenger(
            Long messengerId,
            java.time.LocalDate from,
            java.time.LocalDate to);

    /**
     * Busca servicios de un mensajero que tengan actividad en una fecha específica con paginación.
     */
    Page<ServiceDelivery> findByMessengerAndDate(Long messengerId, java.time.LocalDate date, Pageable pageable);

    /**
     * Busca servicios por número de placa filtrado por concesionario con paginación.
     */
    Page<ServiceDelivery> findByPlateAndDealershipPaginated(String plateNumber, Long dealershipId, Pageable pageable);

    /**
     * Busca servicios por concesionario y una lista de estados específicos con
     * paginación.
     */
    Page<ServiceDelivery> findByDealershipIdAndStatusesPaginated(Long dealershipId,
            List<app.domain.model.enums.Status> statuses, Pageable pageable);

    /**
     * Recupera todas las fotos registradas en el sistema.
     */
    List<app.domain.model.Photo> findAllPhotos();

    /**
     * Actualiza la información de una foto.
     */
    void updatePhoto(app.domain.model.Photo photo);

    /**
     * Recupera todas las firmas registradas en el sistema.
     */
    List<app.domain.model.Signature> findAllSignatures();

    /**
     * Actualiza la información de una firma.
     */
    void updateSignature(app.domain.model.Signature signature);
}
