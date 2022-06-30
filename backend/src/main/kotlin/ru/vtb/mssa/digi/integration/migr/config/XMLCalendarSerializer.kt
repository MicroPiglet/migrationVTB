package ru.vtb.mssa.digi.integration.migr.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.io.IOException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import javax.xml.datatype.XMLGregorianCalendar

class XMLCalendarSerializer(clazz: Class<XMLGregorianCalendar>) : StdSerializer<XMLGregorianCalendar>(clazz) {

    @Throws(IOException::class)
    override fun serialize(value: XMLGregorianCalendar, jgen: JsonGenerator, provider: SerializerProvider) {
        val zonedDateTime = value.toGregorianCalendar().toZonedDateTime()
        val offsetDateTime = zonedDateTime.toOffsetDateTime()
        val format: DateTimeFormatter = if (isDate(zonedDateTime)) {
            ISO_LOCAL_DATE
        } else {
            ISO_OFFSET_DATE_TIME
        }
        jgen.writeString(offsetDateTime.format(format))
    }

    private fun isDate(zonedDateTime: ZonedDateTime): Boolean {
        return zonedDateTime.hour == 0 && zonedDateTime.minute == 0 && zonedDateTime.second == 0
    }
}