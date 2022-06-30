package ru.vtb.mssa.digi.integration.migr.model.db

import org.hibernate.annotations.Type
import java.sql.Timestamp
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "T1", schema = "loanorc")
class MigrationStatusT1(

    @Id @Type(type = "pg-uuid") val id: UUID,
    @Column(name = "update_date", nullable = false) val updateDate: Timestamp,
    @Column(name = "application_migration_status", nullable = true) val migrationStatus: Int,
    @Column(name = "error_description", nullable = true) val errorDescription: String? = null,
)