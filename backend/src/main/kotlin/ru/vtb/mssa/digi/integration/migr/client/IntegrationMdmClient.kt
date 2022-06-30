package ru.vtb.mssa.digi.integration.migr.client

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import ru.vtb.msa.integration.mdm.api.dto.model.RelativeCrossReferenceId
import ru.vtb.msa.integration.mdm.api.dto.model.UserCrossReferencesDTO
import ru.vtb.mssa.digi.integration.migr.exception.ExternalServiceUnavailableException
import ru.vtb.mssa.digi.integration.migr.exception.InvalidResponseException
import ru.vtb.mssa.digi.integration.migr.properties.RestClientProperties


@Service
class IntegrationMdmClient(
    restTemplateBuilder: RestTemplateBuilder, restClientProperties: RestClientProperties
) {
    private val restTemplate: RestTemplate
    private val property: RestClientProperties.RestClientProperty

    init {
        restTemplate = restTemplateBuilder.build()
        property = restClientProperties.mdm
    }

    fun getCrossReferencesByUnc(uncId: String): UserCrossReferencesDTO = try {
        val httpEntity = HttpEntity(
            RelativeCrossReferenceId(
                externalId = uncId, system = "cm.SystemInstance.AC"
            )
        )
        restTemplate.postForEntity(
            "/crossref/person/get/relative".asUri(), httpEntity, UserCrossReferencesDTO::class.java
        ).body ?: throw InvalidResponseException("Cannot get cross references by uncId: $uncId")
    } catch (e: HttpStatusCodeException) {
        throwExceptionIfIntegrationMdmIsUnavailable(e)
    }

    private fun throwExceptionIfIntegrationMdmIsUnavailable(e: HttpStatusCodeException): Nothing {
        throw ExternalServiceUnavailableException(
            "Error during integration-mdm-v2 invocation! Body: " + e.responseBodyAsString
        )
    }

    private fun String.asUri() = UriComponentsBuilder.fromHttpUrl(property.url).path(this).toUriString()
}