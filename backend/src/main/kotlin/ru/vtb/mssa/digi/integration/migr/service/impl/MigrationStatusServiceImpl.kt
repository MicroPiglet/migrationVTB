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
    private val queueService: QueueService,
) : MigrationStatusService {

    override fun findIdsByStatus(status: Int): List<UUID> {
        return migrationStatusRepository.findIdsByStatus(status)
    }

    @Transactional
    override fun save(migrationStatus: MigrationStatusT1): MigrationStatusT1 {
        return migrationStatusRepository.save(migrationStatus)
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
        migrationStatusRepository.updateStatusesAndDates(applicationIds, statusCode)
    }

    @Transactional
    override fun setErrorStatus(id: UUID, errorDescription: String?) {
        migrationStatusRepository.setErrorStatus(id, "${HttpStatus.INTERNAL_SERVER_ERROR} $errorDescription")
    }
}
