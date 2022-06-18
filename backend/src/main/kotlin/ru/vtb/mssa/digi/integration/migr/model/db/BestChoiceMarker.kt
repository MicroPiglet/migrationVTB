package ru.vtb.mssa.digi.integration.migr.model.db

import java.math.BigDecimal

data class BestChoiceMarker(
    val loanAmount: BigDecimal,
    val loanTerm: Int,
    val applyInsurance: Boolean,
    val isSalaryCustomer: Boolean,
    val personalizedOfferId: String
)
