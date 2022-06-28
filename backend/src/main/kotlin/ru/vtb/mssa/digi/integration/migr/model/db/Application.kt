package ru.vtb.mssa.digi.integration.migr.model.db


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.vtb.msa.integration.creditconfigurator.dto.CustomerChoiceResponse
import ru.vtb.mssa.digi.integration.migr.model.dao.PublishPersonLoanApplicationStatusEBMDto
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationType
import ru.vtb24.enterpriseobjectlibrary.business.common.services.loanapplicationscoring.v1.LoanApplicationScoringEBMType
import ru.vtb24.enterpriseobjectlibrary.business.common.services.publishpersonloanapplicationstatus.v2.PublishPersonLoanApplicationStatusEBM
import java.time.LocalDateTime
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class Application(
    val id: UUID,
    val clientUncId: String,
    val type: ApplicationType,
    var status: ApplicationStatus,
    val createDate: LocalDateTime,
    val updateDate: LocalDateTime,
    var bestChoiceResult: CustomerChoiceResponse? = null,
    var scoringRequest: LoanApplicationScoringEBMType? = null,
    var scoringResult: PublishPersonLoanApplicationStatusEBMDto? = null,
    var marker: ApplicationMarker? = null,
    var typeCode: String? = null,
)
