package ru.vtb.mssa.digi.integration.migr.client

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import ru.vtb.mssa.digi.integration.migr.exception.ExternalServiceUnavailableException
import ru.vtb.mssa.digi.integration.migr.model.SendProductStatusRequest
import ru.vtb.mssa.digi.integration.migr.properties.RestClientProperties


@Service
class IntegrationAflClient(
    restTemplateBuilder: RestTemplateBuilder,
    restClientProperties: RestClientProperties,
) {

    private val restTemplate: RestTemplate
    private val property: RestClientProperties.RestClientProperty

    init {
        restTemplate = restTemplateBuilder.build()
        property = restClientProperties.afl
    }

    companion object {
        private const val CROSS_REFERENCES_BY_EXTERNAL_ID = "/application/v1/"
    }

    fun sendProductStatusRequestToAfl(
        mdmId: String,
        applicationId: String,
        request: SendProductStatusRequest,
    ) = try {
        val httpEntity = HttpEntity(request)
        val path = CROSS_REFERENCES_BY_EXTERNAL_ID.plus(mdmId).plus("/").plus(applicationId)

        restTemplate.postForEntity(
            path.asUri(),
            httpEntity,
            Unit::class.java
        ).statusCode.let {
            when (it.isError) {
                false -> it
                true -> throw NullPointerException("Error during integration-afl invocation! mdmId: $mdmId, applicationId: $applicationId")
            }
        }
    } catch (e: HttpStatusCodeException) {
        throwExceptionIfIntegrationMdmIsUnavailable(e)
    }


    private fun throwExceptionIfIntegrationMdmIsUnavailable(e: HttpStatusCodeException): Nothing {
        throw ExternalServiceUnavailableException(
            "Error during afl invocation! Body:" +
                    if (e.responseBodyAsString.length >= 215) e.responseBodyAsString else e.responseBodyAsString.substring(0, 215))
    }

    private fun String.asUri() = UriComponentsBuilder
        .fromHttpUrl(property.url)
        .path(this)
        .toUriString()
}