package ru.vtb.mssa.digi.integration.migr.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import ru.vtb.mssa.digi.integration.migr.model.db.MigrationStatusT1
import ru.vtb.mssa.digi.integration.migr.model.enum.MigrationStatus
import ru.vtb.mssa.digi.integration.migr.repository.MigrationStatusRepository
import ru.vtb.mssa.digi.integration.migr.service.MigrationStatusService
import ru.vtb.mssa.digi.integration.migr.service.QueueService
import java.util.*
import javax.transaction.Transactional

@Service
class MigrationStatusServiceImpl(
    val migrationStatusRepository: MigrationStatusRepository,
) : MigrationStatusService {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ApplicationServiceImpl::class.java)
    }

    override fun findIdsByStatus(status: Int): List<UUID> {
        return migrationStatusRepository.findIdsByStatus(status)
    }

    @Transactional
    override fun save(migrationStatus: MigrationStatusT1): MigrationStatusT1 {
        return migrationStatusRepository.save(migrationStatus).also {
            log.debug("Successfully saved to loanorc.t1 table. MigrationStatus:  ${migrationStatus.id} ")
        }
    }

    override fun findAll(): List<MigrationStatusT1> {
        return migrationStatusRepository.findAll()
    }

    override fun isApplicationsInReadyStatusExist(): Boolean {
        return migrationStatusRepository.countMigrationStatusT1ByMigrationStatus(MigrationStatus.READY_TO_MIGRATE.statusCode) != 0L
    }

    @Transactional
    override fun updateStatus(applicationId: UUID, statusCode: Int) {
        migrationStatusRepository.updateStatus(applicationId, statusCode)
    }

    @Transactional
    override fun updateStatuses(applicationIds: List<UUID>, statusCode: Int) {
        migrationStatusRepository.updateStatuses(applicationIds, statusCode)
    }

    override fun updateStatusesAndDates(applicationIds: List<UUID>, statusCode: Int) {
        migrationStatusRepository.updateStatusesAndDates(applicationIds, statusCode).also {
            log.debug("Successfully updated statuses in loanorc.t1 table for applications:  $applicationIds ")
        }
    }

    @Transactional
    override fun setErrorStatus(id: UUID, errorDescription: String) {
        migrationStatusRepository.setErrorStatus(id, "${HttpStatus.INTERNAL_SERVER_ERROR} " +
            if (errorDescription.length <= 210) errorDescription else errorDescription.substring(0, 209)
        )
    }
}
