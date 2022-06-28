package ru.vtb.mssa.digi.integration.migr.model.enum

enum class ApplicationStatus(val days: Int?, val approvedForMigration: Boolean) {
    APPROVED(null, true),
    OFFLINE_CONFIRMATION(null, true),
    ISSUED(1, true),
    ISSUED_IN_EFR(1, true),
    DRAFT(null, true),
    SCORING_START(null, true),
    HARD_DECLINE(90, true),
    STATUS_PUBLISHER_BEST_CHOICE_NO_ASYNC_TASK(90, true),
    STATUS_PUBLISHER_HARD_DECLINE_ONLINE_ASYNC_TASK(90, true),
    CALL_BS_72(null, true),
    CALL_BS_85(null, true),
    GET_MONEY_ACTION_START(null, true),
    STATUS_PUBLISHER_ONLINE_CREDIT_ISSUING_ASYNC_TASK(null, true),
    ONLINE_CREDIT_ISSUING_GET_MONEY_ASYNC_TASK(null, true),
    ERROR_WHILE_ISSUANCE_MONEY(null, true),
    AUTO_APPROVED(null, true),
    SCORING_SEND_TO_INTEGRATION_CC(null, true),
    SCORING_CREDIT_CONVEYOR_PROCESSING(null, true),
    SCORING_COMPLETE(null, true),
    CALL_BS_223_LOAN_PRODUCT(null, true),
    STATUS_PUBLISHER_BEST_CHOICE_OFFLINE_ASYNC_TASK(null, true),
    STATUS_PUBLISHER_BEST_CHOICE_ONLINE_ASYNC_TASK(null, true),
    CANCELED_IN_EFR(null, false),
    SOFT_DECLINE(null, false),
    CANCELED_BY_CLIENT(null, false),
    CUSTOMER_INVALID_DATA_SOFT_DECLINE_CANCELLER(null, false),
    CUSTOMER_INVALID_DATA_SOFT_DECLINE(null, false),
    STATUS_PUBLISHER_CANCEL_ONLINE_ASYNC_TASK(null, false),
    STATUS_PUBLISHER_CANCEL_OFFLINE_ASYNC_TASK(null, false),
    STATUS_PUBLISHER_SOFT_DECLINE_ONLINE_ASYNC_TASK(null, false),
    OFFER_EXPIRED(null, false),
    ERROR_WHILE_SCORING(null, false),
    OFFER_EXPIRATION_STARTED(null, false);


    companion object {
        var enumMap = mutableMapOf<ApplicationStatus, Int?>()

        init {
            for (applicationStatusApprovedForMigration in values()) {
                when (applicationStatusApprovedForMigration.approvedForMigration) {
                    true -> enumMap[applicationStatusApprovedForMigration] = applicationStatusApprovedForMigration.days
                    else -> continue
                }
            }
        }
    }
}
