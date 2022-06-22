package ru.vtb.mssa.digi.integration.migr.service

import ru.vtb.mssa.digi.integration.migr.model.db.MigrationStatusT1
import java.util.*


interface MigrationStatusService {

    fun findIdsByStatus(status: Int): List<UUID>
    fun save(migrationStatus: MigrationStatusT1): MigrationStatusT1
    fun findAll(): List<MigrationStatusT1>
    fun isApplicationsInReadyStatusExist(): Boolean
    fun updateStatus(applicationId: UUID, statusCode: Int)
    fun updateStatuses(applicationIds: List<UUID>, statusCode: Int)
    fun updateStatusesAndDates(applicationIds: List<UUID>, statusCode: Int)
    fun setErrorStatus(id: UUID, errorDescription: String?)

}
