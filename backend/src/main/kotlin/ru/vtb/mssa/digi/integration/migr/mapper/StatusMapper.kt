package ru.vtb.mssa.digi.integration.migr.mapper

import org.springframework.stereotype.Component
import ru.vtb.mssa.digi.integration.migr.model.enum.AflStatus
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus.APPROVED
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus.AUTO_APPROVED
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus.CALL_BS_223_LOAN_PRODUCT
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus.CALL_BS_72
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus.CALL_BS_85
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus.DRAFT
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus.ERROR_WHILE_ISSUANCE_MONEY
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus.GET_MONEY_ACTION_START
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus.HARD_DECLINE
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus.ISSUED
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus.ISSUED_IN_EFR
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus.OFFLINE_CONFIRMATION
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus.ONLINE_CREDIT_ISSUING_GET_MONEY_ASYNC_TASK
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus.SCORING_COMPLETE
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus.SCORING_CREDIT_CONVEYOR_PROCESSING
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus.SCORING_SEND_TO_INTEGRATION_CC
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus.SCORING_START
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus.STATUS_PUBLISHER_BEST_CHOICE_NO_ASYNC_TASK
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus.STATUS_PUBLISHER_BEST_CHOICE_OFFLINE_ASYNC_TASK
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus.STATUS_PUBLISHER_BEST_CHOICE_ONLINE_ASYNC_TASK
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus.STATUS_PUBLISHER_HARD_DECLINE_ONLINE_ASYNC_TASK
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus.STATUS_PUBLISHER_ONLINE_CREDIT_ISSUING_ASYNC_TASK
import java.security.InvalidParameterException

@Component
class StatusMapper {

    companion object {

        private val converterMap = mapOf(
            DRAFT.name to AflStatus.DRAFT.name,
            SCORING_START.name to AflStatus.DRAFT.name,
            SCORING_SEND_TO_INTEGRATION_CC.name to AflStatus.SCORING.name,
            SCORING_CREDIT_CONVEYOR_PROCESSING.name to AflStatus.SCORING.name,
            SCORING_COMPLETE.name to AflStatus.SCORING.name,
            HARD_DECLINE.name to AflStatus.HARD_DECLINE.name,
            CALL_BS_223_LOAN_PRODUCT.name to AflStatus.SCORING.name,
            APPROVED.name to AflStatus.APPROVED.name,
            AUTO_APPROVED.name to AflStatus.IN_PROGRESS.name,
            CALL_BS_72.name to AflStatus.IN_PROGRESS.name,
            CALL_BS_85.name to AflStatus.IN_PROGRESS.name,
            GET_MONEY_ACTION_START.name to AflStatus.IN_PROGRESS.name,
            STATUS_PUBLISHER_BEST_CHOICE_NO_ASYNC_TASK.name to AflStatus.HARD_DECLINE.name,
            STATUS_PUBLISHER_BEST_CHOICE_OFFLINE_ASYNC_TASK.name to AflStatus.SCORING.name,
            STATUS_PUBLISHER_BEST_CHOICE_ONLINE_ASYNC_TASK.name to AflStatus.SCORING.name,
            STATUS_PUBLISHER_ONLINE_CREDIT_ISSUING_ASYNC_TASK.name to AflStatus.IN_PROGRESS.name,
            ONLINE_CREDIT_ISSUING_GET_MONEY_ASYNC_TASK.name to AflStatus.IN_PROGRESS.name,
            STATUS_PUBLISHER_HARD_DECLINE_ONLINE_ASYNC_TASK.name to AflStatus.HARD_DECLINE.name,
            OFFLINE_CONFIRMATION.name to AflStatus.APPROVED.name,
            ISSUED.name to AflStatus.COMPLETED.name,
            ISSUED_IN_EFR.name to AflStatus.COMPLETED.name,
            ERROR_WHILE_ISSUANCE_MONEY.name to AflStatus.IN_PROGRESS.name
        )

        fun map(status: String?): String {
            return when (status) {
                null -> throw InvalidParameterException("Application status is null")
                else -> converterMap[status]
                    ?: throw InvalidParameterException("Application status not approved for migration. Status: $status, ApplicationStatus -> dto: AflStatus")
            }
        }
    }
}
