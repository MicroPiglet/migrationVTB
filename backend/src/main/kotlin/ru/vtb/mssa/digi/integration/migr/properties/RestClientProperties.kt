package ru.vtb.mssa.digi.integration.migr.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.NestedConfigurationProperty
import java.time.Duration

@ConstructorBinding
@ConfigurationProperties("endpoint")
data class RestClientProperties(
    @NestedConfigurationProperty var afl: RestClientProperty, @NestedConfigurationProperty var mdm: RestClientProperty
) {
    data class RestClientProperty(val url: String, val readTimeout: Duration, val connectionTimeout: Duration)
}
