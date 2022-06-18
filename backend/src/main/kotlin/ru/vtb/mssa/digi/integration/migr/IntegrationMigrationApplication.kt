package ru.vtb.mssa.digi.integration.migr

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import ru.vtb.mssa.digi.integration.migr.properties.RestClientProperties

@SpringBootApplication
@EnableConfigurationProperties(RestClientProperties::class)
class IntegrationMigrationApplication

fun main(args: Array<String>) {
	runApplication<IntegrationMigrationApplication>(*args)

}
