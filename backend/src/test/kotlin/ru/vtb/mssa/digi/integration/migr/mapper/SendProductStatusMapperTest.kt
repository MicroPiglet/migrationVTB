package ru.vtb.mssa.digi.integration.migr.mapper


import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import ru.vtb.mssa.digi.integration.migr.model.Data
import ru.vtb.mssa.digi.integration.migr.model.Product
import ru.vtb.mssa.digi.integration.migr.model.SendProductStatusRequest
import ru.vtb.mssa.digi.integration.migr.model.db.Application
import ru.vtb.mssa.digi.integration.migr.repository.ApplicationRepository
import ru.vtb.mssa.digi.integration.migr.util.MigrationTestUtil
import ru.vtb.mssa.digi.integration.migr.util.PrepareDataHelper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class SendProductStatusMapperTest {
    private val dataProducer = PrepareDataHelper()
    private val sendProductStatusMapper = SendProductStatusMapper()
    private val appId = 12345L
    private lateinit var applicationRepository: ApplicationRepository
    private val date1 = OffsetDateTime.of(LocalDateTime.parse("2020-07-30 21:40:45.882",
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), OffsetDateTime.now().offset)
    private val date2 = OffsetDateTime.of(LocalDateTime.parse("2019-07-30 00:00:00.000",
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")), OffsetDateTime.now().offset)
    private val date3 = LocalDate.of(2025, 7,30)
    private val expectedProductStatusRequest = createSendProductStatusRequest(date1, date2, date3)
    lateinit var testApp: Application


    @Test
    fun mapSendProductStatusTest() {

        testApp = dataProducer.testApp()
        applicationRepository = mock {
            on { findOne(any()) } doReturn testApp
        }

        val result = sendProductStatusMapper.mapRequest(appId, testApp)

        MigrationTestUtil.assertEqualsWithJackson(result, expectedProductStatusRequest)
    }


    private fun createSendProductStatusRequest(
        created: OffsetDateTime,
        dateIssue: OffsetDateTime,
        localDate: LocalDate
    ): SendProductStatusRequest {
        return SendProductStatusRequest(
            status = "COMPLETED", data =
            Data(
                created = created,
                updated = created.plusDays(2L),
                stage = "ISSUED_IN_EFR",
                creationChannel = Data.CreationChannel.telebank,
                scoringDate = created.plusDays(3),
                endDate = localDate,
                offerId = null,
                product = listOf(
                    Product(
                        type = "PACL3",
                        amount = "87000",
                        totalAmount = "87000",
                        crossSales = false,
                        dateIssue = dateIssue,
                        rate = "16.2",
                        payment = "2124",
                        term = 60
                    )
                )
            )
        )
    }
}