package ru.vtb.mssa.digi.integration.migr.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.vtb.mssa.digi.integration.migr.repository.QueueRepository
import ru.vtb.mssa.digi.integration.migr.service.QueueService
import java.util.*
import javax.transaction.Transactional

@Service
class QueueServiceImpl(
    private val queueRepository: QueueRepository,
) : QueueService {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(ApplicationServiceImpl::class.java)
    }

    override fun getAppIdsInPublishStatusTopic(): List<UUID> {
        return queueRepository.getAppIdsInPublishStatusTopic()
    }
}

