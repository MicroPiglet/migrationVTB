package ru.vtb.mssa.digi.integration.migr.service

import ru.vtb.mssa.digi.integration.migr.model.dao.MigrationStatusDao
import ru.vtb.mssa.digi.integration.migr.model.db.Application
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus
import java.util.*
import kotlin.collections.ArrayList

interface ApplicationService {
    fun findApplication(id: UUID): Application
    fun findByStatusAndDays(status: ApplicationStatus, days: Int?): List<MigrationStatusDao>
    fun findByStatusSetAtDaysBefore(status: ApplicationStatus, days: Int): List<MigrationStatusDao>
    fun findByStatus(status: ApplicationStatus): List<MigrationStatusDao>
    fun prepareUpdatedApplications()
    fun prepareApplicationsForMigration()
}
