package ru.vtb.mssa.digi.integration.migr.repository

import ru.vtb.mssa.digi.integration.migr.model.dao.MigrationStatusDao
import ru.vtb.mssa.digi.integration.migr.model.db.Application
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus
import java.util.*


interface ApplicationRepository {

    fun findOne(applicationId: UUID): Application?
    fun findByStatusSetAtDaysBefore(status: ApplicationStatus, days: Int): List<MigrationStatusDao>
    fun findByStatus(status: ApplicationStatus): List<MigrationStatusDao>
    fun findUpdatedApplications(): List<MigrationStatusDao>
    fun findNotApprovedForMigrationAppsInfo(): List<MigrationStatusDao>
}
