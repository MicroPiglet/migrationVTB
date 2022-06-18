package ru.vtb.mssa.digi.integration.migr.model.db

import java.math.BigDecimal
import java.time.LocalDate

data class KodMarker(
    val loanAmount: BigDecimal,
    val loanTerm: Int,
    val applyInsurance: Boolean,
    val issueDate: LocalDate,
    val monthlyIncome: BigDecimal
)