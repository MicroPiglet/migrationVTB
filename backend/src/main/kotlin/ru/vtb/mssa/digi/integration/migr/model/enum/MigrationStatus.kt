package ru.vtb.mssa.digi.integration.migr.model.enum

enum class MigrationStatus(val statusCode: Int) {
    ERROR(0),
    READY_TO_MIGRATE(1),
    SENT_BY_THE_ORCHESTRATOR(2),
    SUCCESS(3);


    companion object {
        var enumMap = mutableMapOf<MigrationStatus, Int>()

        init {
            for (migrationStatus in values()) {
                enumMap.put(migrationStatus, migrationStatus.statusCode)
            }
        }
    }
}
