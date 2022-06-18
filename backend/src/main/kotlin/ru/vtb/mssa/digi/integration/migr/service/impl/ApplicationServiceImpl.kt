package ru.vtb.mssa.digi.integration.migr.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.vtb.mssa.digi.integration.migr.exception.InvalidResponseException
import ru.vtb.mssa.digi.integration.migr.model.dao.MigrationStatusDao
import ru.vtb.mssa.digi.integration.migr.model.db.Application
import ru.vtb.mssa.digi.integration.migr.model.db.MigrationStatusT1
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus
import ru.vtb.mssa.digi.integration.migr.model.enum.MigrationStatus
import ru.vtb.mssa.digi.integration.migr.repository.ApplicationRepository
import ru.vtb.mssa.digi.integration.migr.service.ApplicationService
import ru.vtb.mssa.digi.integration.migr.service.MigrationStatusService
import java.util.*

@Service
class ApplicationServiceImpl(
    private val applicationRepository: ApplicationRepository,
    private val migrationStatusService: MigrationStatusService,
) : ApplicationService {

    @Transactional
    override fun prepareApplicationsForMigration(): ArrayList<MigrationStatusDao> {
        val applicationsForMigration = ArrayList<MigrationStatusDao>(emptyList())

        for (entry: Map.Entry<ApplicationStatus, Int?> in ApplicationStatus.enumMap) {
            val apps = findByStatusAndDays(entry.key, entry.value)
            applicationsForMigration.addAll(ArrayList(apps))
        }
        applicationsForMigration.forEach {
            val migrationStatus = MigrationStatusT1(
                it.id,
                it.updateDate,
                MigrationStatus.READY_TO_MIGRATE.statusCode,
            )
            migrationStatusService.save(migrationStatus)
        }
        return applicationsForMigration
    }

    override fun findByStatusAndDays(status: ApplicationStatus, days: Int?): List<MigrationStatusDao> {
        return when (days) {
            null -> findByStatus(status)
            else -> findByStatusSetAtDaysBefore(status, days)
        }
    }

    override fun findApplication(id: UUID): Application = applicationRepository.findOne(id)
        ?: throw InvalidResponseException("Application not found in loanorc.application table, applicationId=${id}")

    override fun findByStatusSetAtDaysBefore(status: ApplicationStatus, days: Int): List<MigrationStatusDao> =
        applicationRepository.findByStatusSetAtDaysBefore(status, days)

    override fun findByStatus(status: ApplicationStatus): List<MigrationStatusDao> =
        applicationRepository.findByStatus(status)

    override fun prepareUpdatedApplications(): List<MigrationStatusDao> {
        val applicationsForUpdate: List<MigrationStatusDao> = applicationRepository.findUpdatedApplications()
        migrationStatusService.updateStatusesAndDates(applicationsForUpdate.map { it.id },
            MigrationStatus.READY_TO_MIGRATE.statusCode)
        return applicationsForUpdate
    }
}
