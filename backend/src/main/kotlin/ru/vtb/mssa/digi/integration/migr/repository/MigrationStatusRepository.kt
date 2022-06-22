package ru.vtb.mssa.digi.integration.migr.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.vtb.mssa.digi.integration.migr.model.db.MigrationStatusT1
import java.util.*



@Repository
interface MigrationStatusRepository : JpaRepository<MigrationStatusT1, UUID> {

    @Query(value = "select CAST(id AS VARCHAR) from loanorc.t1 where application_migration_status = :status " +
            "order by id LIMIT 3000 for update skip locked", nativeQuery = true)
    fun findIdsByStatus(@Param("status") status: Int): List<UUID>

    fun countMigrationStatusT1ByMigrationStatus(status: Int): Long

    @Modifying
    @Query("update MigrationStatusT1 ms set ms.migrationStatus = :statusCode where ms.id = :id")
    fun updateStatus(id: UUID, statusCode: Int)

    @Modifying
    @Query("update MigrationStatusT1 ms set ms.migrationStatus = :statusCode where ms.id in :ids")
    fun updateStatuses(ids: List<UUID>, statusCode: Int)

    @Modifying
    @Query(value = "update loanorc.t1 t " +
            "set update_date = " +
            "(select ason.update_date " +
            "from loanorc.application " +
            "as ap join loanorc.application_status " +
            "as ason on ap.application_status_id = ason.id " +
            "join loanorc.t1 as t1 " +
            "on ap.id = t1.id " +
            "where ap.id = t.id), " +
            "application_migration_status = :statusCode " +
            "where t.id in :ids",
        nativeQuery = true)
    fun updateStatusesAndDates(@Param("ids") ids: List<UUID>, @Param("statusCode") statusCode: Int)


    @Modifying
    @Query("update MigrationStatusT1 ms set ms.migrationStatus = 0, ms.errorDescription = :errorDescription " +
            "where ms.id = :id")
    fun setErrorStatus(id: UUID, errorDescription: String)

}