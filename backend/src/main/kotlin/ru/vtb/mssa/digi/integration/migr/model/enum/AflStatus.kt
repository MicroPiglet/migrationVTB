package ru.vtb.mssa.digi.integration.migr.model.enum

enum class AflStatus {
    DRAFT,
    USER_CANCELED_BEFORE_SCORING,
    SENDED_TO_SCORING,
    APPROVED,
    HARD_DECLINE,
    SOFT_DECLINED,
    USER_CANCELED_AFTER_APPROVING,
    COMPLETED,
    OFFER_EXPIRED,
    DELETED,
    CREDIT_HISTORY_CONSENT,
    CLIENT_PASSPORT_CHANGED,
    IN_PROGRESS,
    SCORING
    ;
}