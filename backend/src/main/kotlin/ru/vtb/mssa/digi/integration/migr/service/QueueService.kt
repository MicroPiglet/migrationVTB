package ru.vtb.mssa.digi.integration.migr.service

import java.util.*

interface QueueService {
    fun getAppIdsInPublishStatusTopic(): List<UUID>
}
