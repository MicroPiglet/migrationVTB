package ru.vtb.mssa.digi.integration.migr.model.db

import java.math.BigDecimal

data class BisquitLoanContractMarker(
    val loanAmount: BigDecimal,
    val loanTerm: Int,
    val applyInsurance: Boolean,
    val monthlyIncome: BigDecimal
)
