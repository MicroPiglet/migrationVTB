package ru.vtb.mssa.digi.integration.migr.model.enum

enum class ApplicationStatus(val days: Int?) {
    APPROVED(null),
    OFFLINE_CONFIRMATION(null),
    ISSUED(1),
    ISSUED_IN_EFR(1),
    DRAFT(null),
    SCORING_START(null),
    HARD_DECLINE(90),
    STATUS_PUBLISHER_BEST_CHOICE_NO_ASYNC_TASK(90),
    STATUS_PUBLISHER_HARD_DECLINE_ONLINE_ASYNC_TASK(90),
    CALL_BS_72(null),
    CALL_BS_85(null),
    GET_MONEY_ACTION_START(null),
    STATUS_PUBLISHER_ONLINE_CREDIT_ISSUING_ASYNC_TASK(null),
    ONLINE_CREDIT_ISSUING_GET_MONEY_ASYNC_TASK(null),
    ERROR_WHILE_ISSUANCE_MONEY(null),
    AUTO_APPROVED(null),
    SCORING_SEND_TO_INTEGRATION_CC(null),
    SCORING_CREDIT_CONVEYOR_PROCESSING(null),
    SCORING_COMPLETE(null),
    CALL_BS_223_LOAN_PRODUCT(null),
    STATUS_PUBLISHER_BEST_CHOICE_OFFLINE_ASYNC_TASK(null),
    STATUS_PUBLISHER_BEST_CHOICE_ONLINE_ASYNC_TASK(null);

    companion object {
        var enumMap = mutableMapOf<ApplicationStatus, Int?>()

        init {
            for (applicationStatus in values()) {
                enumMap.put(applicationStatus, applicationStatus.days)
            }
        }
    }
}
