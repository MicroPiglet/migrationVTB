package ru.vtb.mssa.digi.integration.migr.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import ru.vtb.mssa.digi.integration.migr.utils.toXmlDate
import ru.vtb.mssa.digi.integration.migr.utils.toXmlDateTime
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_DATE
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.time.format.DateTimeFormatter.ofPattern
import javax.xml.datatype.XMLGregorianCalendar

class XMLCalendarDeserializer(clazz: Class<XMLGregorianCalendar>) : StdDeserializer<XMLGregorianCalendar>(clazz) {

    @Throws(IOException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): XMLGregorianCalendar {
        return try {
            val localDate = LocalDate.from(ISO_DATE.parse(jp.valueAsString))
            localDate.toXmlDate()
        } catch (ex: Exception) {
            try {
                val localDate = LocalDateTime.from(ISO_OFFSET_DATE_TIME.parse(jp.valueAsString))
                localDate.toXmlDateTime()
            } catch (ex: Exception) {
                val localDate = LocalDateTime.from(ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(jp.valueAsString))
                localDate.toXmlDateTime()
            }
        }
    }
}