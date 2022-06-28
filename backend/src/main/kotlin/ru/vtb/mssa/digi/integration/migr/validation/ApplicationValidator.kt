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
            verifyCondition(
                scoringRequest?.dataArea?.loanApplicationEBO?.saleChannel == null,
                "application.scoringRequest.dataArea.loanApplicationEBO.saleChannel->creationChannel"
            )
            verifyCondition(typeCode == null, "application.typeCode->type")
            verifyCondition(bestChoiceResult?.productList?.any { product -> product.cashAmount == null } == true,
                "application.product.bestChoiceResult.productList.cashAmount -> amount")
            verifyCondition(bestChoiceResult?.productList?.any { product -> product.totalAmount == null } == true && StatusMapper.map(
                status.name
            ) != AflStatus.SCORING.toString() || StatusMapper.map(
                status.name
            ) != AflStatus.DRAFT.toString(),
                "application.product.bestChoiceResult.productList.totalAmount -> totalAmount")
        }
    }
}