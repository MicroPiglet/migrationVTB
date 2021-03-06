package ru.vtb.mssa.digi.integration.migr.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Service
import ru.vtb.mssa.digi.integration.migr.service.impl.ApplicationServiceImpl
import kotlin.system.exitProcess

@Service
class MigrationService(
    private val applicationService: ApplicationService,
    private val migrationIteration: MigrationIteration,
) : CommandLineRunner {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ApplicationServiceImpl::class.java)
        var successful: Int = 0
        var notMigrated: Int = 0
    }

    override fun run(vararg args: String?) {
        try {
            prepareMigrationStatusTable()
        } catch (e: Exception) {
            log.debug("Cannot migrate an applications, message:  ${e.message} \n cause: ${e.cause} \n stackTraceToString: ${e.stackTraceToString()}")
        }

        while (migrationIteration.migrateApplications()) {
            log.debug("Preparing applications in process")
        }
        log.debug("Successfully migrated applications total: $successful , not migrated: $notMigrated ")
        exitProcess(0)
    }

    fun prepareMigrationStatusTable() {
        applicationService.prepareApplicationsForMigration()
        applicationService.prepareUpdatedApplications()
        applicationService.saveNotApprovedForMigrationAppsInfo()

    }
}