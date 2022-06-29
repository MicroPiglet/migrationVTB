package ru.vtb.mssa.digi.integration.migr.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import config.toUUID
import org.postgresql.util.PGobject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.vtb.msa.integration.creditconfigurator.dto.CustomerChoiceResponse
import ru.vtb.mssa.digi.integration.migr.model.dao.MigrationStatusDao
import ru.vtb.mssa.digi.integration.migr.model.dao.PublishPersonLoanApplicationStatusEBMDto
import ru.vtb.mssa.digi.integration.migr.model.db.Application
import ru.vtb.mssa.digi.integration.migr.model.db.ApplicationMarker
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationType
import ru.vtb.mssa.digi.integration.migr.service.impl.ApplicationServiceImpl
import ru.vtb24.enterpriseobjectlibrary.business.common.services.loanapplicationscoring.v1.LoanApplicationScoringEBMType
import java.sql.ResultSet
import java.sql.Timestamp
import java.sql.Types.OTHER
import java.util.*

@Repository
class ApplicationRepositoryImpl(
    val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    @Qualifier("jsonbObjectMapper") val objectMapper: ObjectMapper,
) : ApplicationRepository {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ApplicationServiceImpl::class.java)
    }

    override fun findOne(applicationId: UUID): Application? {
        var result: Application? = null
        val mapSqlParameterSource = MapSqlParameterSource().apply {
            addValue("application_id", applicationId)
        }
        namedParameterJdbcTemplate.query(
            """
            select ap.id,
                ap.client_unc_id,
                 ap.type_code,
                 ap.create_date,
                 ap.scoring_result,
                 ap.scoring_request,
                 ap.best_choice_result,
                 ap.marker,
                 ap.type_code,
             ason.status as status,
             ason.update_date as update_date
            from loanorc.application as ap
                     left join loanorc.application_status as ason
                     on ap.id  = ason.application_id 
            where ap.id = :application_id
            """.trimIndent(),
            mapSqlParameterSource
        ) {
            result = processApplicationRow(it)
        }
        return result
    }


    override fun findByStatusSetAtDaysBefore(status: ApplicationStatus, days: Int): List<MigrationStatusDao> {
        val param = MapSqlParameterSource().apply {
            addValue("statusName", status.name, OTHER)
            addValue("days", days)
            addValue("curDate", Timestamp(System.currentTimeMillis()))
            addValue("lastUpdatedApplicationStatusIds", findLastUpdatedApplicationStatusIds().map { it }, OTHER)

        }
        val result: MutableList<MigrationStatusDao> = arrayListOf()
        namedParameterJdbcTemplate.query(
            """
            select ap.id,
            ason.update_date, t1.id 
            from loanorc.application as ap
            join loanorc.application_status as ason
            on ap.id  = ason.application_id 
            and ason.id in (:lastUpdatedApplicationStatusIds)
            and ason.status = :statusName      
            left join loanorc.t1 as t1
            on ap.id  = t1.id
            where t1.id is NUll   
            and (:curDate - date(ason.update_date)) between 0 and :days
                """.trimIndent(),
            param
        ) {
            if (it.getString("id") != null) {
                result.add(processMigrationStatus(it))
            }
        }
        return result
    }


    override fun findByStatus(status: ApplicationStatus): List<MigrationStatusDao> {
        val param = MapSqlParameterSource().apply {
            addValue("statusName", status.name, OTHER)
            addValue("lastUpdatedApplicationStatusIds", findLastUpdatedApplicationStatusIds().map { it }, OTHER)
        }

        val result: MutableList<MigrationStatusDao> = arrayListOf()
        namedParameterJdbcTemplate.query(
            """
            select ap.id,
            ason.update_date, t1.id 
            from loanorc.application as ap
            join loanorc.application_status as ason
            on ap.id  = ason.application_id 
            and ason.id in (:lastUpdatedApplicationStatusIds)
            and ason.status = :statusName
            left join loanorc.t1 as t1
            on ap.id  = t1.id
            where t1.id is NUll
            """.trimIndent(),
            param
        ) {
            if (it.getString("id") != null) {
                result.add(processMigrationStatus(it))
            }
        }
        return result
    }

    override fun findUpdatedApplications(): List<MigrationStatusDao> {
        val param = MapSqlParameterSource().apply {
            addValue("lastUpdatedApplicationStatusIds", findLastUpdatedApplicationStatusIds().map { it }, OTHER)
        }
        val result: MutableList<MigrationStatusDao> = arrayListOf()

        namedParameterJdbcTemplate.query(
            """
             select ap.id,
            ason.update_date, t1.id 
            from loanorc.application as ap
            join loanorc.application_status as ason
            on ap.id  = ason.application_id 
            and ason.id in (:lastUpdatedApplicationStatusIds)
            join loanorc.t1 as t1
            on ap.id  = t1.id
            where ason.update_date != t1.update_date 
                """.trimIndent(),
            param
        ) {
            result.add(processMigrationStatus(it))
        }
        return result
    }

    fun findLastUpdatedApplicationStatusIds(): List<UUID> {
        val result: MutableList<UUID> = arrayListOf()

        namedParameterJdbcTemplate.query(
            """
                SELECT  as2.id
                FROM loanorc.application_status as2 
                LEFT JOIN loanorc.application_status as3        
                ON as2.application_id  = as3.application_id 
                AND as2.update_date < as3.update_date 
                WHERE as3.update_date  is null
                """.trimIndent(),
        ) {
            result.add(it.getString("id").toUUID())
        }
        return result
    }

    override fun findNotApprovedForMigrationAppsInfo(): List<MigrationStatusDao> {
        val result: MutableList<MigrationStatusDao> = arrayListOf()

        namedParameterJdbcTemplate.query(
            """
            select ap.id,
            ason.update_date, ason.status , t1.id,
            now() - ason.update_date  
            from loanorc.application as ap
            join loanorc.application_status as ason
            on ap.id  = ason.application_id 
            and ason.id in (SELECT  as2.id
            FROM loanorc.application_status as2 
            LEFT JOIN loanorc.application_status as3        
            ON as2.application_id  = as3.application_id 
            AND as2.update_date < as3.update_date 
            WHERE as3.update_date  is null)
            left join loanorc.t1 as t1
            on ap.id  = t1.id
            where t1.id is null
                """.trimIndent(),
        ) {
            result.add(processMigrationStatus(it))
        }
        return result
    }

    private fun processApplicationRow(it: ResultSet): Application {
        val id = it.getString("id")
        val clientUncId = it.getString("client_unc_id")
        val type = it.getString("type_code")
        val status = it.getString("status")
        val createDate = it.getTimestamp("create_date")
        val updateDate = it.getTimestamp("update_date")
        val scoringResp = (it.getObject("scoring_result") as PGobject?)?.value?.let {
            objectMapper.readValue<PublishPersonLoanApplicationStatusEBMDto>(it)
        }
        val scoringReq = (it.getObject("scoring_request") as PGobject?)?.value?.let {
            objectMapper.readValue<LoanApplicationScoringEBMType>(it)
        }
        val bestChoices = (it.getObject("best_choice_result") as PGobject?)?.value?.let {
            objectMapper.readValue<CustomerChoiceResponse>(it)
        }
        val marker = (it.getObject("marker") as PGobject?)?.value?.let {
            objectMapper.readValue<ApplicationMarker>(it)
        }
        val typeCode = it.getString("type_code")


        return Application(
            id = UUID.fromString(id),
            clientUncId = clientUncId,
            type = ApplicationType.valueOf(type),
            status = ApplicationStatus.valueOf(status),
            createDate = createDate.toLocalDateTime(),
            updateDate = updateDate.toLocalDateTime(),
            scoringRequest = scoringReq,
            scoringResult = scoringResp,
            bestChoiceResult = bestChoices,
            marker = marker,
            typeCode = typeCode
        )
    }

    private fun processMigrationStatus(it: ResultSet): MigrationStatusDao {
        log.debug("processMigrationStatus ${it.getString("id")}")
        val id = it.getString("id")
        val updateDate = it.getTimestamp("update_date")

        return MigrationStatusDao(
            id = UUID.fromString(id),
            updateDate = updateDate,
        )
    }
}
