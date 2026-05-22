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
  @EntityGraph(attributePaths = { "plate", "dealership", "messenger", "signature" })
  Optional<ServiceDeliveryEntity> findByIdServiceDeliveryAndDeletedFalse(Long id);

  @EntityGraph(attributePaths = { "plate", "dealership", "messenger" })
  List<ServiceDeliveryEntity> findByDeletedTrue();

  @EntityGraph(attributePaths = { "plate", "dealership", "messenger", "signature" })
  Optional<ServiceDeliveryEntity> findByUuidAndDeletedFalse(String uuid);

  @EntityGraph(attributePaths = { "plate", "dealership", "messenger", "signature" })
  Optional<ServiceDeliveryEntity> findByUuid(String uuid);

  @EntityGraph(attributePaths = { "plate", "dealership", "messenger" })
  Page<ServiceDeliveryEntity> findByDeleted(Boolean deleted, Pageable pageable);

  @EntityGraph(attributePaths = { "plate", "dealership", "messenger" })
  Page<ServiceDeliveryEntity> findByMessenger_IdEmployeeAndDeleted(Long messengerId, Boolean deleted,
      Pageable pageable);

  @EntityGraph(attributePaths = { "plate", "dealership", "messenger" })
  Page<ServiceDeliveryEntity> findByDeletedAndCurrentStatusIn(Boolean deleted, List<Status> statuses,
      Pageable pageable);

  @EntityGraph(attributePaths = { "plate", "dealership", "messenger" })
  Page<ServiceDeliveryEntity> findByMessenger_IdEmployeeAndDeletedAndCurrentStatusIn(Long messengerId,
      Boolean deleted, List<Status> statuses, Pageable pageable);

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

  List<ServiceDeliveryEntity> findByDeletedTrueAndDeletedAtBefore(LocalDateTime date);



  @EntityGraph(attributePaths = { "plate", "dealership", "messenger" })
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

  @EntityGraph(attributePaths = { "plate", "dealership", "messenger" })
  Page<ServiceDeliveryEntity> findByPlate_PlateNumberAndDealership_IdDealershipAndDeletedFalse(
      String plateNumber, Long dealershipId, Pageable pageable);

  @EntityGraph(attributePaths = { "plate", "dealership", "messenger" })
  Page<ServiceDeliveryEntity> findByDealership_IdDealershipAndCurrentStatusInAndDeletedFalse(
      Long dealershipId, List<Status> statuses, Pageable pageable);

  @EntityGraph(attributePaths = { "plate", "dealership", "messenger" })
  Page<ServiceDeliveryEntity> findByPlate_PlateNumberAndDeletedFalse(
      String plateNumber, Pageable pageable);

  @EntityGraph(attributePaths = { "plate", "dealership", "messenger" })
  Page<ServiceDeliveryEntity> findByCurrentStatusInAndDeletedFalse(
      List<Status> statuses, Pageable pageable);
}