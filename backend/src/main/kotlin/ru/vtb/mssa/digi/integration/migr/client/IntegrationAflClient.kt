package ru.vtb.mssa.digi.integration.migr.client

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder
import ru.vtb.mssa.digi.integration.migr.exception.ExternalServiceUnavailableException
import ru.vtb.mssa.digi.integration.migr.model.SendProductStatusRequest
import ru.vtb.mssa.digi.integration.migr.properties.RestClientProperties


@Service
class IntegrationAflClient(
    restTemplateBuilder: RestTemplateBuilder,
    restClientProperties: RestClientProperties,
    @Qualifier("aflClientJsonObjectMapper") val objectMapper: ObjectMapper
) {

    private val restTemplate: RestTemplate
    private val property: RestClientProperties.RestClientProperty

    init {
        restTemplate = restTemplateBuilder.build()
        property = restClientProperties.afl
    }

    fun sendProductStatusRequestToAfl(
        mdmId: String,
        applicationId: String,
        request: SendProductStatusRequest,
    ) = try {
        objectMapper.writeValueAsString(request)
        val httpEntity = HttpEntity(objectMapper.writeValueAsString(request), httpHeaders())
        val path = "/application/v1/$mdmId/$applicationId"

        restTemplate.postForEntity(
            path.asUri(), httpEntity, SendProductStatusRequest::class.java
        ).statusCode.let {
            when (it.isError) {
                false -> it
                true -> throw ResponseStatusException(
                    it, "Error during integration-afl invocation! mdmId: $mdmId, applicationId: $applicationId"
                )
            }
        }
    } catch (e: HttpStatusCodeException) {
        throwExceptionIfIntegrationMdmIsUnavailable(e)
    }


    private fun throwExceptionIfIntegrationMdmIsUnavailable(e: HttpStatusCodeException) {
        throw ExternalServiceUnavailableException(
            "Error during afl invocation! Body:" + e.responseBodyAsString
        )
    }

    private fun String.asUri() = UriComponentsBuilder.fromHttpUrl(property.url).path(this).toUriString()

    private fun httpHeaders(): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        return headers
    }
}