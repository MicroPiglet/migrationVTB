package ru.vtb.mssa.digi.integration.migr.mapper

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.vtb.mssa.digi.integration.migr.model.Data
import ru.vtb.mssa.digi.integration.migr.model.Product
import ru.vtb.mssa.digi.integration.migr.model.SendProductStatusRequest
import ru.vtb.mssa.digi.integration.migr.model.db.Application
import ru.vtb.mssa.digi.integration.migr.model.enum.AflStatus
import ru.vtb.mssa.digi.integration.migr.service.impl.ApplicationServiceImpl
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.*
import javax.xml.datatype.XMLGregorianCalendar
import kotlin.streams.toList

@Component
class SendProductStatusMapper {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(ApplicationServiceImpl::class.java)
    }

    fun mapRequest(
        applicationId: UUID, application: Application,
    ): SendProductStatusRequest {
        return SendProductStatusRequest(
            status = StatusMapper.map(application.status.name)!!,
            data = Data(
                created = application.createDate.atOffset(OffsetDateTime.now().offset),
                updated = application.updateDate.atOffset(OffsetDateTime.now().offset),
                stage = application.status.toString(),
                creationChannel = Data.CreationChannel.valueOf(CreationChannelMapper.map(application.scoringRequest?.dataArea
                    ?.loanApplicationEBO?.saleChannel?.value)!!),
                scoringDate = xmlGregorianCalendarToOffsetDateTime(application.scoringRequest?.dataArea?.loanApplicationEBO?.auditHistory?.createdDateTime!!),
                endDate = XmlGregorianCalendarToLocalDate(
                    application.scoringResult?.dataArea?.loanApplicationEBO?.desicionReport?.first()!!.decisionEndDate
                ),
                offerId = null,
                product = application.bestChoiceResult?.productList?.stream()?.map { productBC ->
                    Product(
                        type = application.typeCode.toString(),
                        amount = productBC.cashAmount.toString(),
                        totalAmount = (if (StatusMapper.map(application.status.name) == AflStatus.SCORING.toString()
                            || StatusMapper.map(application.status.name) == AflStatus.DRAFT.toString()
                        ) {
                            productBC.cashAmount.toString()
                        } else {
                            productBC.totalAmount.toString()
                        }),
                        crossSales = false,
                        dateIssue = application.marker?.loanContractMarker?.issueDate?.atStartOfDay(ZoneId.systemDefault())
                            ?.toOffsetDateTime(),
                        rate = productBC.rate.toString(),
                        payment = productBC.payment.toString(),
                        term = productBC.term
                    )
                }?.toList()).also { log.debug("Mapped SendProductStatusRequest with applicationId $applicationId: SendProductStatusRequest: $it") })

    }

    private fun XmlGregorianCalendarToLocalDate(xmlGregorianCalendar: XMLGregorianCalendar): LocalDate? {
        return LocalDate.of(
            xmlGregorianCalendar.year,
            xmlGregorianCalendar.month,
            xmlGregorianCalendar.day);
    }

    private fun xmlGregorianCalendarToOffsetDateTime(xmlGregorianCalendar: XMLGregorianCalendar): OffsetDateTime? {
        return xmlGregorianCalendar.toGregorianCalendar().toZonedDateTime().toOffsetDateTime()
    }
}