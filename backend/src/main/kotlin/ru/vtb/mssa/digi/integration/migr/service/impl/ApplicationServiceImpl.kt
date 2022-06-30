package ru.vtb.mssa.digi.integration.migr.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
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
import ru.vtb.mssa.digi.integration.migr.service.MigrationService.Companion.notMigrated
import ru.vtb.mssa.digi.integration.migr.service.MigrationStatusService
import ru.vtb.mssa.digi.integration.migr.validation.ApplicationValidator
import java.util.*

@Service
class ApplicationServiceImpl(
    private val applicationRepository: ApplicationRepository,
    private val migrationStatusService: MigrationStatusService,
    private val applicationValidator: ApplicationValidator,
) : ApplicationService {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ApplicationServiceImpl::class.java)
        private var preparedTotal: Int = 0
    }

    @Transactional
    override fun prepareApplicationsForMigration() {
        log.debug("Start preparing applications for migration ")

        for (entry: Map.Entry<ApplicationStatus, Int?> in ApplicationStatus.enumMap) {
            val apps = findByStatusAndDays(entry.key, entry.value)
            apps.forEach {
                val migrationStatus = MigrationStatusT1(
                    it.id,
                    it.updateDate,
                    MigrationStatus.READY_TO_MIGRATE.statusCode,
                )
                migrationStatusService.save(migrationStatus)
                preparedTotal++
            }
            log.debug("Successfully prepared (saved to loanorc.t1 table) applications in status ${entry.key} ")
        }
        log.debug("Preparing applications for migration finished, prepared total: $preparedTotal")
    }

    override fun findByStatusAndDays(status: ApplicationStatus, days: Int?): List<MigrationStatusDao> {
        return when (days) {
            null -> findByStatus(status)
            else -> findByStatusSetAtDaysBefore(status, days)
        }
    }

    override fun findApplication(id: UUID): Application {
        val application: Application = applicationRepository.findOne(id)
            ?: throw InvalidResponseException("Application not found in loanorc.application table, applicationId=${id}")
        applicationValidator.verifyApplicationFields(application)
        return application
    }

    override fun findByStatusSetAtDaysBefore(status: ApplicationStatus, days: Int): List<MigrationStatusDao> =
        applicationRepository.findByStatusSetAtDaysBefore(status, days)

    override fun findByStatus(status: ApplicationStatus): List<MigrationStatusDao> =
        applicationRepository.findByStatus(status)

    @Transactional
    override fun prepareUpdatedApplications() {
        log.debug("Start updating applications for migration in loanorc.t1 table ")
        val applicationsForUpdate: List<MigrationStatusDao> = applicationRepository.findUpdatedApplications()
        applicationsForUpdate.forEach {
            val migrationStatus = MigrationStatusT1(
                it.id,
                it.updateDate,
                MigrationStatus.READY_TO_MIGRATE.statusCode,
            )
            migrationStatusService.save(migrationStatus)
            log.debug("Updating applications for migration in loanorc.t1 table finished")
        }
    }

    override fun saveNotApprovedForMigrationAppsInfo() {
        val NOT_APPROVED_APPS_MESSAGE =
            "${HttpStatus.INTERNAL_SERVER_ERROR} " + "Не передается в выгрузке - последняя запись (дата и статус) в таблице application_status не проходит по срокам "
        val applicationsForUpdate: List<MigrationStatusDao> =
            applicationRepository.findNotApprovedForMigrationAppsInfo()
        applicationsForUpdate.forEach {
            val migrationStatus = MigrationStatusT1(
                it.id, it.updateDate, MigrationStatus.ERROR.statusCode, NOT_APPROVED_APPS_MESSAGE
            )
            migrationStatusService.save(migrationStatus)
            notMigrated++
        }
    }
}
