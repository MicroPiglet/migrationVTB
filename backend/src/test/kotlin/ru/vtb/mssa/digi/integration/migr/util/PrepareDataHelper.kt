package ru.vtb.mssa.digi.integration.migr.util

import com.fasterxml.jackson.module.kotlin.readValue
import ru.vtb.mssa.digi.integration.migr.config.ObjectMapperConfig
import ru.vtb.mssa.digi.integration.migr.model.db.Application
import java.io.IOException


class PrepareDataHelper {

    fun testApp(): Application = extractApp("json/testApp.json")

    private fun extractApp(path: String): Application {
        val jsonMapper = ObjectMapperConfig().customObjectMapper()
        val myApp: Application
        try {

            val resource = javaClass.classLoader.getResource(path).readText()
            myApp = jsonMapper.readValue(resource)

        } catch (e: IOException) {
            throw e
        }

        return myApp
    }
}