package ru.vtb.mssa.digi.integration.migr.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.xml.datatype.XMLGregorianCalendar

@Configuration
class ObjectMapperConfig {

    @Primary
    @Bean("jsonbObjectMapper")
    fun customObjectMapper(): ObjectMapper = baseObjectMapper().apply {
        registerModule(JavaTimeModule().apply {
            this.addDeserializer(
                LocalDate::class.java,
                LocalDateDeserializer(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            )
            this.addSerializer(LocalDate::class.java, LocalDateSerializer(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
            this.addSerializer(
                XMLGregorianCalendar::class.java,
                XMLCalendarSerializer(XMLGregorianCalendar::class.java)
            )
            this.addDeserializer(
                XMLGregorianCalendar::class.java,
                XMLCalendarDeserializer(XMLGregorianCalendar::class.java)
            )
        })
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
        setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
    }

    private fun baseObjectMapper() = ObjectMapper().apply {
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        registerModule(KotlinModule())
    }

}
