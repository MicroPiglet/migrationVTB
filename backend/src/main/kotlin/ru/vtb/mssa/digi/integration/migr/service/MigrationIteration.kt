package ru.vtb.mssa.digi.integration.migr.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.vtb.mssa.digi.integration.migr.client.IntegrationAflClient
import ru.vtb.mssa.digi.integration.migr.mapper.SendProductStatusMapper
import ru.vtb.mssa.digi.integration.migr.model.enum.MigrationStatus
import ru.vtb.mssa.digi.integration.migr.service.MigrationService.Companion.failed
import ru.vtb.mssa.digi.integration.migr.service.MigrationService.Companion.successful
import java.util.*

@Service
class MigrationIteration(
    private val applicationService: ApplicationService,
    private val sendProductStatusMapper: SendProductStatusMapper,
    private val aflService: AflService,
    private val aflClient: IntegrationAflClient,
    val migrationStatusService: MigrationStatusService,
    private val queueService: QueueService,
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(MigrationIteration::class.java)
        private var REQUEST_RER_SECOND: Int = 100
    }


    @Transactional
    fun migrateApplications(): Boolean {
        val applicationIds =
            ArrayList(migrationStatusService.findIdsByStatus(MigrationStatus.READY_TO_MIGRATE.statusCode))
        val appIdsInPublishStatusTopic: List<UUID> = queueService.getAppIdsInPublishStatusTopic()
        migrationStatusService.updateStatuses(appIdsInPublishStatusTopic,
            MigrationStatus.SENT_BY_THE_ORCHESTRATOR.statusCode)
        applicationIds.removeAll(appIdsInPublishStatusTopic)
        log.debug("Valid applicationIds in ready status: count in portion: ${applicationIds.size}")
        return if (applicationIds.isEmpty()) {
            false
        } else {
            migrate(applicationIds)
            true
        }
    }

    private fun migrate(applicationIds: ArrayList<UUID>) {
        applicationIds.forEach { applicationId ->
            safeRequest(applicationId) {
                sendWithThrottle {
                    try {
                        val application = applicationService.findApplication(applicationId)
                        val mdmId = aflService.getPartyUIdByUncId(application.clientUncId)
                        val productStatusRequest = sendProductStatusMapper.mapRequest(
                            applicationId, application)
                        aflClient.sendProductStatusRequestToAfl(mdmId.toString(),
                            applicationId.toString(),
                            productStatusRequest)

                        log.debug("Application was migrated, application id: $applicationId")
                        migrationStatusService.updateStatus(applicationId, MigrationStatus.SUCCESS.statusCode)
                        successful++
                    } catch (e: Exception) {
                        log.debug("Cannot migrate an application with id: $applicationId,  ${e.stackTraceToString()}")
                        migrationStatusService.setErrorStatus(applicationId,
                            if (e.message.isNullOrBlank())"" else {e.message!!})
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