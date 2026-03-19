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
         * Busca servicios activos asociados a una placa.
         */
        List<ServiceDelivery> findByPlateNumber(String plateNumber);

        /**
         * Busca servicios asignados a un mensajero.
         */
        List<ServiceDelivery> findByMessengerId(Long messengerId);

        /**
         * Busca un servicio activo por ID.
         */
        ServiceDelivery findByIdActive(Long idServiceDelivery);

        /**
         * Busca un servicio activo (no eliminado) por su UUID público.
         */
        ServiceDelivery findByUuidActive(String uuid);

        /**
         * Recupera todos los servicios marcados como eliminados.
         */
        List<ServiceDelivery> findDeleted();

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
         * Busca servicios de un mensajero que tengan actividad en una fecha específica.
         */
        List<ServiceDelivery> findByMessengerAndDate(Long messengerId, java.time.LocalDate date);

        /**
         * Busca servicios por número de placa filtrado por concesionario.
         */
        List<ServiceDelivery> findByPlateNumberAndDealershipId(String plateNumber, Long dealershipId);

        /**
         * Busca servicios pendientes (no entregados) de un concesionario.
         */
        List<ServiceDelivery> findPendingByDealershipId(Long dealershipId);

        /**
         * Busca servicios por concesionario y una lista de estados específicos.
         */
        List<ServiceDelivery> findByDealershipIdAndStatuses(Long dealershipId,
                        List<app.domain.model.enums.Status> statuses);

        /**
         * Busca servicios por concesionario y una lista de estados específicos con
         * paginación.
         */
        Page<ServiceDelivery> findByDealershipIdAndStatusesPaginated(Long dealershipId,
                        List<app.domain.model.enums.Status> statuses, Pageable pageable);
}
