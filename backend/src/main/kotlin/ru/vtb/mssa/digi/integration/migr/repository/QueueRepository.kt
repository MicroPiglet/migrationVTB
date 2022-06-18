package ru.vtb.mssa.digi.integration.migr.repository

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class QueueRepository(
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
) {

    companion object {
        const val TABLE_NAME = "loanorc.multi_pod_queue"
        const val TOPIC_NAME = "PUBLISH-STATUS-TO-AFL"
    }

    fun getAppIdsInPublishStatusTopic(): List <UUID> {
        val param = MapSqlParameterSource().apply {
            addValue("topic", TOPIC_NAME)
        }
        return namedParameterJdbcTemplate.queryForList("""
            select distinct destination_id  
            from $TABLE_NAME 
            where topic = :topic 
            """.trimIndent(),
            param,
            UUID::class.java)
        }
    }
