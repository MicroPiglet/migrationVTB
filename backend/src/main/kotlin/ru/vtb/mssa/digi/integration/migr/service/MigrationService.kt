package ru.vtb.mssa.digi.integration.migr.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Service
import ru.vtb.mssa.digi.integration.migr.client.IntegrationAflClient
import ru.vtb.mssa.digi.integration.migr.mapper.SendProductStatusMapper
import ru.vtb.mssa.digi.integration.migr.model.enum.MigrationStatus
import ru.vtb.mssa.digi.integration.migr.service.impl.ApplicationServiceImpl
import java.util.*
import kotlin.system.exitProcess

@Service
class MigrationService(
    private val applicationService: ApplicationService,
    private val sendProductStatusMapper: SendProductStatusMapper,
    private val aflService: AflService,
    private val aflClient: IntegrationAflClient,
    val migrationStatusService: MigrationStatusService,
    private val queueService: QueueService,
) : CommandLineRunner {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ApplicationServiceImpl::class.java)
        private var successful: Int = 0
        private var failed: Int = 0
        private var REQUEST_RER_SECOND: Int = 100
    }

    override fun run(vararg args: String?) {
        try {
            prepareMigrationStatusTable()
        } catch (e: Exception) {
            log.debug("Cannot migrate an applications, message:  ${e.message} \n cause: ${e.cause} \n stackTraceToString: ${e.stackTraceToString()}")
        }
        while (migrationStatusService.isApplicationsInReadyStatusExist()
                .also { log.debug("Is applications in ready status exist: $it") }
        ) {
            val applicationIds =
                findAndFilterApplicationIdsToMigrate()

            migrate(applicationIds)
        }
        log.debug("Successfully sent applications total: $successful , failed: $failed ")
        exitProcess(0)
    }

    fun prepareMigrationStatusTable() {
        applicationService.prepareApplicationsForMigration()
        applicationService.prepareUpdatedApplications()
    }

    fun findAndFilterApplicationIdsToMigrate(): ArrayList<UUID> {
        val applicationIds =
            ArrayList(migrationStatusService.findIdsByStatus(MigrationStatus.READY_TO_MIGRATE.statusCode))
        val appIdsInPublishStatusTopic: List<UUID> = queueService.getAppIdsInPublishStatusTopic()
        migrationStatusService.updateStatuses(appIdsInPublishStatusTopic,
            MigrationStatus.SENT_BY_THE_ORCHESTRATOR.statusCode)
        applicationIds.removeAll(appIdsInPublishStatusTopic)
        log.debug("Valid applicationIds in ready status: count in portion: ${applicationIds.size}, ids: $applicationIds")
        return applicationIds
    }

    fun migrate(applicationIds: ArrayList<UUID>) {
        applicationIds.forEach { applicationId ->
            safeRequest(applicationId) {
                sendWithThrottle {
                    try {
                        val application = applicationService.findApplication(applicationId)
                        val mdmId = aflService.getPartyUIdByUncId(application.clientUncId)
                        val productStatusRequest = sendProductStatusMapper.mapRequest(
                            mdmId, application)
                        aflClient.sendProductStatusRequestToAfl(mdmId.toString(),
                            applicationId.toString(),
                            productStatusRequest)

                        log.debug("Application was migrated, application id: $applicationId")
                        migrationStatusService.updateStatus(applicationId, MigrationStatus.SUCCESS.statusCode)
                        successful++
                    } catch (e: Exception) {
                        log.debug("Cannot migrate an application with id: $applicationId,  ${e.stackTraceToString()}")
                        migrationStatusService.setErrorStatus(applicationId, e.message ?: e.stackTraceToString())
                        failed++
                    }
                }
            }
        }
    }


    private fun safeRequest(applicationId: UUID, sendRequest: () -> Unit) {
        try {
            log.debug("Sending request, application with id: {}", applicationId)
            sendRequest()
        } catch (e: Exception) {
            log.error("Exception while processing application with id: {}",
                applicationId,
                e.message)
        }
    }

    private fun sendWithThrottle(sendRequest: () -> Unit) {
        val startRequest = System.currentTimeMillis()
        sendRequest()
        val endRequest = System.currentTimeMillis()
        val timeForSleep = 1000L / REQUEST_RER_SECOND - (endRequest - startRequest)
        if (timeForSleep > 0) Thread.sleep(timeForSleep)

    }
}