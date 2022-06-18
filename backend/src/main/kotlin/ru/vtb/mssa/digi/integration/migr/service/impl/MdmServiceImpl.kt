package ru.vtb.mssa.digi.integration.migr.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.vtb.mssa.digi.integration.migr.client.IntegrationMdmClient
import ru.vtb.mssa.digi.integration.migr.exception.InvalidResponseException
import ru.vtb.mssa.digi.integration.migr.service.AflService
import java.time.LocalDateTime.now

@Service
class MdmServiceImpl(
    private val integrationMdmClient: IntegrationMdmClient
) : AflService {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(ApplicationServiceImpl::class.java)
    }

    override fun getPartyUIdByUncId(uncId: String): Long {
        val crossRefResponse = integrationMdmClient.getCrossReferencesByUnc(uncId)
        val crossReferences = crossRefResponse.crossReferences.filter { it.expiredAt?.isAfter(now()) ?: true }
        if (crossReferences.isEmpty()) {
            log.debug("int-mdm: crossRef is empty, unc=${uncId}")
            throw InvalidResponseException(
                "int-mdm: crossRef is empty, unc=${uncId}")
        }
        return crossRefResponse.partyUId
            ?: throw InvalidResponseException(
                "int-mdm: Customer cross-ref mdmId not found, unc=${uncId}")
    }
}
