package ru.vtb.mssa.digi.integration.migr.validation

import ru.vtb.mssa.digi.integration.migr.exception.InvalidFieldException


internal fun verifyCondition(
    condition: Boolean, message: String? = null
) {
    if (condition) throw InvalidFieldException("Request processing: $message is null or empty")
}