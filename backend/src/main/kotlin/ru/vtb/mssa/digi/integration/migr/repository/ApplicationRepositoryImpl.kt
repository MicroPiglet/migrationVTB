package ru.vtb.mssa.digi.integration.migr.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.postgresql.util.PGobject
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import ru.vtb.msa.integration.creditconfigurator.dto.CustomerChoiceResponse
import ru.vtb.mssa.digi.integration.migr.model.dao.MigrationStatusDao
import ru.vtb.mssa.digi.integration.migr.model.db.Application
import ru.vtb.mssa.digi.integration.migr.model.db.ApplicationMarker
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationStatus
import ru.vtb.mssa.digi.integration.migr.model.enum.ApplicationType
import ru.vtb24.enterpriseobjectlibrary.business.common.services.loanapplicationscoring.v1.LoanApplicationScoringEBMType
import ru.vtb24.enterpriseobjectlibrary.business.common.services.publishpersonloanapplicationstatus.v2.PublishPersonLoanApplicationStatusEBM
import java.sql.ResultSet
import java.sql.Timestamp
import java.sql.Types.OTHER
import java.util.*

@Repository
class ApplicationRepositoryImpl(
    val namedParameterJdbcTemplate: NamedParameterJdbcTemplate,
    @Qualifier("jsonbObjectMapper") val objectMapper: ObjectMapper,
) : ApplicationRepository {

    override fun findOne(applicationId: UUID): Application? {
        var result: Application? = null
        val mapSqlParameterSource = MapSqlParameterSource().apply {
            addValue("application_id", applicationId)
        }
        namedParameterJdbcTemplate.query(
            """
            select ap.*,
             ason.status as status,
             ason.update_date as status_change_date
            from loanorc.application as ap
                     left join loanorc.application_status as ason
                               on ap.application_status_id = ason.id
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

        }
        val result: MutableList<MigrationStatusDao> = arrayListOf()
        namedParameterJdbcTemplate.query(
            """
            select ap.id,
            ason.update_date 
            from loanorc.application as ap
            left join loanorc.application_status as ason
            on ap.application_status_id = ason.id
            where ason.status = :statusName
            and (:curDate - date(ason.update_date) + 1) between 0 and :days
            AND ap.id NOT IN (select id from loanorc.t1)
                """.trimIndent(),
            param
        ) {
            result.add(processMigrationStatus(it))
        }
        return result
    }


    override fun findByStatus(status: ApplicationStatus): List<MigrationStatusDao> {
        val param = MapSqlParameterSource().apply {
            addValue("statusName", status.name, OTHER)
        }
        val result: MutableList<MigrationStatusDao> = arrayListOf()
        namedParameterJdbcTemplate.query(
            """
            select ap.id,
            ason.update_date 
            from loanorc.application as ap
            left join loanorc.application_status as ason
            on ap.application_status_id = ason.id
            where ason.status = :statusName
            AND ap.id NOT IN (select id from loanorc.t1)
            """.trimIndent(),
            param
        ) {
            result.add(processMigrationStatus(it))
        }
        return result
    }

    override fun findUpdatedApplications(): List<MigrationStatusDao> {
        val result: MutableList<MigrationStatusDao> = arrayListOf()
        namedParameterJdbcTemplate.query(
            """
                select t1.*
                from loanorc.application as ap 
                join loanorc.application_status as ason
                on ap.application_status_id = ason.id
                join loanorc.t1 as t1 
                on ap.id = t1.id
                where ason.update_date != t1.update_date 
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
            objectMapper.readValue<PublishPersonLoanApplicationStatusEBM>(it)
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
        val id = it.getString("id")
        val updateDate = it.getTimestamp("update_date")

        return MigrationStatusDao(
            id = UUID.fromString(id),
            updateDate = updateDate,
        )
    }

}
