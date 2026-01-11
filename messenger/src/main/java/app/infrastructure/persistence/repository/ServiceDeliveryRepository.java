package app.infrastructure.persistence.repository;

import app.domain.model.enums.Status;
import app.infrastructure.persistence.entities.ServiceDeliveryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para servicios de entrega.
 */
@Repository
public interface ServiceDeliveryRepository extends JpaRepository<ServiceDeliveryEntity, Long> {

        /**
         * Encuentra servicios por estado actual.
         */
        List<ServiceDeliveryEntity> findByCurrentStatus(Status currentStatus);

        /**
         * Encuentra servicios de un mensajero específico.
         */
        List<ServiceDeliveryEntity> findByMessenger_Document(Long messengerDocument);

        /**
         * Encuentra servicios por placa.
         */
        List<ServiceDeliveryEntity> findByPlate_PlateNumber(String plateNumber);

        /**
         * Encuentra servicios por concesionario.
         */
        List<ServiceDeliveryEntity> findByDealership_IdDealership(Long dealershipId);

        /**
         * Encuentra servicios de un mensajero específico.
         */
        List<ServiceDeliveryEntity> findByMessenger_IdEmployee(Long messengerId);

        /**
         * Encuentra servicios no eliminados.
         */
        List<ServiceDeliveryEntity> findByDeletedFalse();

        /**
         * Encuentra un servicio por su ID y estado de eliminación.
         */
        Optional<ServiceDeliveryEntity> findByIdServiceDeliveryAndDeletedFalse(Long id);

        /**
         * Encuentra servicios eliminados.
         */
        List<ServiceDeliveryEntity> findByDeletedTrue();

        /**
         * Encuentra servicios con paginación filtrado por estado de eliminación
         */
        Page<ServiceDeliveryEntity> findByDeleted(Boolean deleted, Pageable pageable);

        /**
         * Encuentra servicios de un mensajero específico con paginación
         */
        Page<ServiceDeliveryEntity> findByMessenger_IdEmployeeAndDeleted(Long messengerId, Boolean deleted,
                        Pageable pageable);

        /**
         * Encuentra servicios por estado de eliminación y lista de estados con
         * paginación
         */
        Page<ServiceDeliveryEntity> findByDeletedAndCurrentStatusIn(Boolean deleted, List<Status> statuses,
                        Pageable pageable);

        /**
         * Encuentra servicios de un mensajero específico por estado de eliminación y
         * lista de estados con paginación
         */
        Page<ServiceDeliveryEntity> findByMessenger_IdEmployeeAndDeletedAndCurrentStatusIn(Long messengerId,
                        Boolean deleted, List<Status> statuses, Pageable pageable);

        /**
         * Busca servicios por keyword en múltiples campos (ID, placa, concesionario,
         * mensajero)
         */
        @Query("SELECT s FROM ServiceDeliveryEntity s WHERE s.deleted = :deleted AND (" +
                        "CAST(s.idServiceDelivery AS string) LIKE %:keyword% OR " +
                        "LOWER(s.plate.plateNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(s.dealership.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(s.messenger.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                        "(:statuses IS NULL OR s.currentStatus IN :statuses)")
        Page<ServiceDeliveryEntity> searchAll(@Param("keyword") String keyword, @Param("deleted") Boolean deleted,
                        @Param("statuses") List<Status> statuses, Pageable pageable);

        /**
         * Busca servicios de un mensajero específico por keyword
         */
        @Query("SELECT s FROM ServiceDeliveryEntity s WHERE s.messenger.idEmployee = :messengerId AND s.deleted = :deleted AND ("
                        +
                        "CAST(s.idServiceDelivery AS string) LIKE %:keyword% OR " +
                        "LOWER(s.plate.plateNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(s.dealership.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                        "(:statuses IS NULL OR s.currentStatus IN :statuses)")
        Page<ServiceDeliveryEntity> searchByMessenger(@Param("messengerId") Long messengerId,
                        @Param("keyword") String keyword, @Param("deleted") Boolean deleted,
                        @Param("statuses") List<Status> statuses, Pageable pageable);

        /**
         * Encuentra servicios eliminados antes de una fecha específica
         */
        List<ServiceDeliveryEntity> findByDeletedTrueAndDeletedAtBefore(LocalDateTime date);

        /**
         * Encuentra servicios por placa y estado de eliminación
         */
        List<ServiceDeliveryEntity> findByPlate_PlateNumberAndDeletedFalse(String plateNumber);

        /**
         * Busca estadísticas diarias de servicios por mensajero
         */
        @Query(value = """
                        SELECT DATE(created_at) as date,
                               SUM(CASE WHEN current_status = 'ASSIGNED' THEN 1 ELSE 0 END) as assigned,
                               SUM(CASE WHEN current_status = 'DELIVERED' THEN 1 ELSE 0 END) as delivered,
                               SUM(CASE WHEN current_status = 'RETURNED' THEN 1 ELSE 0 END) as returned,
                               SUM(CASE WHEN current_status = 'CANCELED' THEN 1 ELSE 0 END) as canceled,
                               COUNT(*) as total
                        FROM service_deliveries
                        WHERE messenger_id = :messengerId
                          AND created_at >= :fromDate
                          AND created_at < :toDate
                          AND deleted = false
                        GROUP BY DATE(created_at)
                        ORDER BY date DESC
                        """, nativeQuery = true)
        List<Object[]> findDailyStatsByMessenger(
                        @Param("messengerId") Long messengerId,
                        @Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate);
}