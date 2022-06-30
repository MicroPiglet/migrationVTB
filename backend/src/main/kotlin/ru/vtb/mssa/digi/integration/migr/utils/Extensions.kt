package ru.vtb.mssa.digi.integration.migr.utils

import java.math.BigDecimal
import java.math.RoundingMode.FLOOR
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar

internal fun XMLGregorianCalendar.toLocalDate(): LocalDate = LocalDate.of(this.year, this.month, this.day)

fun BigDecimal.format(): BigDecimal = this.setScale(2, FLOOR)

fun LocalDate.toXmlDate(): XMLGregorianCalendar {
    val gcal = GregorianCalendar.from(this.atStartOfDay(ZoneId.systemDefault()))
    return DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal)
}

fun LocalDateTime.toXmlDateTime(): XMLGregorianCalendar {
    val gcal = GregorianCalendar.from(this.atZone(ZoneId.systemDefault()))
    return DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal)
}

internal fun String.toUUID(): UUID = UUID.fromString(this)