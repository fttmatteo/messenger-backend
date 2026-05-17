package app.infrastructure.persistence.repository;

import app.domain.model.enums.Status;
import app.infrastructure.persistence.entities.ServiceDeliveryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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
   * Encuentra un servicio por su ID y estado de eliminación.
   * Usa EntityGraph para cargar relaciones y evitar N+1.
   */
  @EntityGraph(attributePaths = { "plate", "dealership", "messenger", "signature" })
  Optional<ServiceDeliveryEntity> findByIdServiceDeliveryAndDeletedFalse(Long id);

  /**
   * Encuentra servicios eliminados.
   * Usa EntityGraph para cargar relaciones y evitar N+1.
   */
  @EntityGraph(attributePaths = { "plate", "dealership", "messenger" })
  List<ServiceDeliveryEntity> findByDeletedTrue();

  /**
   * Encuentra un servicio por su UUID público y que no esté eliminado.
   * Usa EntityGraph para cargar relaciones y evitar N+1.
   */
  @EntityGraph(attributePaths = { "plate", "dealership", "messenger", "signature" })
  Optional<ServiceDeliveryEntity> findByUuidAndDeletedFalse(String uuid);

  /**
   * Encuentra un servicio por su UUID público incluyendo eliminados.
   * Usa EntityGraph para cargar relaciones y evitar N+1.
   */
  @EntityGraph(attributePaths = { "plate", "dealership", "messenger", "signature" })
  Optional<ServiceDeliveryEntity> findByUuid(String uuid);

  /**
   * Encuentra servicios con paginación filtrado por estado de eliminación.
   * Usa EntityGraph para cargar relaciones y evitar N+1.
   */
  @EntityGraph(attributePaths = { "plate", "dealership", "messenger" })
  Page<ServiceDeliveryEntity> findByDeleted(Boolean deleted, Pageable pageable);

  /**
   * Encuentra servicios de un mensajero específico con paginación.
   * Usa EntityGraph para cargar relaciones y evitar N+1.
   */
  @EntityGraph(attributePaths = { "plate", "dealership", "messenger" })
  Page<ServiceDeliveryEntity> findByMessenger_IdEmployeeAndDeleted(Long messengerId, Boolean deleted,
      Pageable pageable);

  /**
   * Encuentra servicios por estado de eliminación y lista de estados con
   * paginación.
   * Usa EntityGraph para cargar relaciones y evitar N+1.
   */
  @EntityGraph(attributePaths = { "plate", "dealership", "messenger" })
  Page<ServiceDeliveryEntity> findByDeletedAndCurrentStatusIn(Boolean deleted, List<Status> statuses,
      Pageable pageable);

  /**
   * Encuentra servicios de un mensajero específico por estado de eliminación y
   * lista de estados con
   * paginación.
   * Usa EntityGraph para cargar relaciones y evitar N+1.
   */
  @EntityGraph(attributePaths = { "plate", "dealership", "messenger" })
  Page<ServiceDeliveryEntity> findByMessenger_IdEmployeeAndDeletedAndCurrentStatusIn(Long messengerId,
      Boolean deleted, List<Status> statuses, Pageable pageable);

  /**
   * Busca servicios por keyword en múltiples campos usando FULLTEXT MATCH
   * AGAINST.
   * Más eficiente que LIKE %keyword% para grandes volúmenes de datos.
   */
  @Query(value = """
      -- noinspection SqlDialectInspection
      -- noinspection SqlNoDataSourceInspection
      SELECT DISTINCT s.* FROM service_deliveries s
      JOIN plates p ON s.plate_id = p.id_plate
      JOIN dealerships d ON s.dealership_id = d.id_dealership
      JOIN employees e ON s.messenger_id = e.id_employee
      WHERE s.deleted = :deleted
      AND (
          CAST(s.id_service_delivery AS CHAR) LIKE :keywordLike OR
          MATCH(p.plate_number) AGAINST(:keywordBoolean IN BOOLEAN MODE) OR
          MATCH(d.name) AGAINST(:keywordBoolean IN BOOLEAN MODE) OR
          MATCH(e.full_name) AGAINST(:keywordBoolean IN BOOLEAN MODE)
      )
      AND (:statuses IS NULL OR s.current_status IN (:statuses))
      """, countQuery = """
      -- noinspection SqlDialectInspection
      -- noinspection SqlNoDataSourceInspection
      SELECT COUNT(DISTINCT s.id_service_delivery) FROM service_deliveries s
      JOIN plates p ON s.plate_id = p.id_plate
      JOIN dealerships d ON s.dealership_id = d.id_dealership
      JOIN employees e ON s.messenger_id = e.id_employee
      WHERE s.deleted = :deleted
      AND (
          CAST(s.id_service_delivery AS CHAR) LIKE :keywordLike OR
          MATCH(p.plate_number) AGAINST(:keywordBoolean IN BOOLEAN MODE) OR
          MATCH(d.name) AGAINST(:keywordBoolean IN BOOLEAN MODE) OR
          MATCH(e.full_name) AGAINST(:keywordBoolean IN BOOLEAN MODE)
      )
      AND (:statuses IS NULL OR s.current_status IN (:statuses))
      """, nativeQuery = true)
  Page<ServiceDeliveryEntity> searchAll(
      @Param("keywordLike") String keywordLike,
      @Param("keywordBoolean") String keywordBoolean,
      @Param("deleted") Boolean deleted,
      @Param("statuses") List<String> statuses,
      Pageable pageable);

  /**
   * Busca servicios de un mensajero específico por keyword usando FULLTEXT MATCH
   * AGAINST.
   */
  @Query(value = """
      -- noinspection SqlDialectInspection
      -- noinspection SqlNoDataSourceInspection
      SELECT DISTINCT s.* FROM service_deliveries s
      JOIN plates p ON s.plate_id = p.id_plate
      JOIN dealerships d ON s.dealership_id = d.id_dealership
      WHERE s.messenger_id = :messengerId
      AND s.deleted = :deleted
      AND (
          CAST(s.id_service_delivery AS CHAR) LIKE :keywordLike OR
          MATCH(p.plate_number) AGAINST(:keywordBoolean IN BOOLEAN MODE) OR
          MATCH(d.name) AGAINST(:keywordBoolean IN BOOLEAN MODE)
      )
      AND (:statuses IS NULL OR s.current_status IN (:statuses))
      """, countQuery = """
      -- noinspection SqlDialectInspection
      -- noinspection SqlNoDataSourceInspection
      SELECT COUNT(DISTINCT s.id_service_delivery) FROM service_deliveries s
      JOIN plates p ON s.plate_id = p.id_plate
      JOIN dealerships d ON s.dealership_id = d.id_dealership
      WHERE s.messenger_id = :messengerId
      AND s.deleted = :deleted
      AND (
          CAST(s.id_service_delivery AS CHAR) LIKE :keywordLike OR
          MATCH(p.plate_number) AGAINST(:keywordBoolean IN BOOLEAN MODE) OR
          MATCH(d.name) AGAINST(:keywordBoolean IN BOOLEAN MODE)
      )
      AND (:statuses IS NULL OR s.current_status IN (:statuses))
      """, nativeQuery = true)
  Page<ServiceDeliveryEntity> searchByMessenger(
      @Param("messengerId") Long messengerId,
      @Param("keywordLike") String keywordLike,
      @Param("keywordBoolean") String keywordBoolean,
      @Param("deleted") Boolean deleted,
      @Param("statuses") List<String> statuses,
      Pageable pageable);

  /**
   * Encuentra servicios eliminados antes de una fecha específica
   */
  List<ServiceDeliveryEntity> findByDeletedTrueAndDeletedAtBefore(LocalDateTime date);

  /**
   * Busca estadísticas diarias de servicios por mensajero
   */
  @Query(value = """
      -- noinspection SqlDialectInspection
      -- noinspection SqlNoDataSourceInspection
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

  /**
   * Busca servicios de un mensajero que tengan actividad en una fecha específica.
   * Usa EntityGraph para cargar relaciones y evitar N+1.
   */
  @EntityGraph(attributePaths = { "plate", "dealership", "messenger", "history", "history.changedBy" })
  @Query("SELECT DISTINCT s FROM ServiceDeliveryEntity s LEFT JOIN s.history h " +
      "WHERE s.messenger.idEmployee = :messengerId " +
      "AND s.deleted = false " +
      "AND (" +
      "   (s.createdAt >= :startOfDay AND s.createdAt < :endOfDay) " +
      "   OR " +
      "   (h.changeDate >= :startOfDay AND h.changeDate < :endOfDay) " +
      ")")
  Page<ServiceDeliveryEntity> findByMessengerAndDate(
      @Param("messengerId") Long messengerId,
      @Param("startOfDay") LocalDateTime startOfDay,
      @Param("endOfDay") LocalDateTime endOfDay,
      Pageable pageable);

  /**
   * Encuentra servicios por placa y concesionario (no eliminados) con paginación.
   */
  @EntityGraph(attributePaths = { "plate", "dealership", "messenger", "history" })
  Page<ServiceDeliveryEntity> findByPlate_PlateNumberAndDealership_IdDealershipAndDeletedFalse(
      String plateNumber, Long dealershipId, Pageable pageable);

  /**
   * Encuentra servicios por concesionario y estados (no eliminados) con
   * paginación.
   */
  @EntityGraph(attributePaths = { "plate", "dealership", "messenger", "history" })
  Page<ServiceDeliveryEntity> findByDealership_IdDealershipAndCurrentStatusInAndDeletedFalse(
      Long dealershipId, List<Status> statuses, Pageable pageable);

  /**
   * Encuentra servicios por placa (no eliminados) con paginación de manera global.
   */
  @EntityGraph(attributePaths = { "plate", "dealership", "messenger", "history" })
  Page<ServiceDeliveryEntity> findByPlate_PlateNumberAndDeletedFalse(
      String plateNumber, Pageable pageable);

  /**
   * Encuentra servicios por estados (no eliminados) con paginación de manera global.
   */
  @EntityGraph(attributePaths = { "plate", "dealership", "messenger", "history" })
  Page<ServiceDeliveryEntity> findByCurrentStatusInAndDeletedFalse(
      List<Status> statuses, Pageable pageable);
}