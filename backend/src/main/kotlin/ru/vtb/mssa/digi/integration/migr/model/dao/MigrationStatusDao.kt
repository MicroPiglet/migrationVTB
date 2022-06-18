package ru.vtb.mssa.digi.integration.migr.model.dao

import java.sql.Timestamp
import java.util.*

class MigrationStatusDao(
    val id: UUID,
    val updateDate: Timestamp,
    val migrationStatus: Int? = null,
    val errorDescription: String? = null,
)