package ru.vtb.mssa.digi.integration.migr.validation

import org.springframework.stereotype.Component
import ru.vtb.mssa.digi.integration.migr.mapper.StatusMapper
import ru.vtb.mssa.digi.integration.migr.model.db.Application
import ru.vtb.mssa.digi.integration.migr.model.enum.AflStatus

/**
 * Осуществляет валидацию заявки, выгруженной из БД loanorc
 * */
@Component
class ApplicationValidator {

    fun verifyApplicationFields(application: Application) {
        with(application) {
            verifyCondition(scoringRequest?.dataArea?.loanApplicationEBO?.saleChannel == null,
                "application.scoringRequest.dataArea.loanApplicationEBO.saleChannel")
            if (bestChoiceResult != null && bestChoiceResult!!.productList.isNotEmpty()) {
                verifyCondition(typeCode == null, "application.typeCode")
                verifyCondition(bestChoiceResult!!.productList.first().cashAmount == null,
                    "application.product.bestChoiceResult.productList.cashAmount")
                if (StatusMapper.map(status.name) != AflStatus.SCORING.toString() ||
                    StatusMapper.map(status.name) != AflStatus.DRAFT.toString()
                ) {
                    verifyCondition(bestChoiceResult!!.productList.first().totalAmount == null,
                        "application.product.bestChoiceResult.productList.totalAmount")
                }
            }
            verifyCondition(scoringResult?.dataArea?.loanApplicationEBO?.desicionReport?.firstOrNull()?.decisionEndDate == null,
                "application.scoringResult.dataArea.loanApplicationEBO.desicionReport.decisionEndDate -> endDate")
        }
    }
}