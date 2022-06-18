package ru.vtb.mssa.digi.integration.migr.mapper

import org.springframework.stereotype.Component
import ru.vtb.mssa.digi.integration.migr.model.Data
import java.security.InvalidParameterException

@Component
class CreationChannelMapper {

    companion object {

        private val converterMap = HashMap<String, Data.CreationChannel>();

        init {
            for (creationChannel in Data.CreationChannel.values()) {
                converterMap.put(creationChannel.value, creationChannel);
            }
        }

        fun map(creationChannelValue: String?): String? {
            return when (creationChannelValue) {
                null -> null
                else -> converterMap[creationChannelValue]?.name
                    ?: throw InvalidParameterException("INTEGRATION_VALIDATION_ERROR saleChannel Invalid field format Value: $creationChannelValue /dto: ApplicationStatus -> dto: AflStatus")
            }
        }
    }
}
