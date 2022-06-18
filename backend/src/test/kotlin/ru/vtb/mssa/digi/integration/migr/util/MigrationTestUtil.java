package ru.vtb.mssa.digi.integration.migr.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.SneakyThrows;
import ru.vtb.mssa.digi.integration.migr.config.ObjectMapperConfig;

import static org.assertj.core.api.Assertions.assertThat;

public class MigrationTestUtil {

    @SneakyThrows
    public static <T> void assertEqualsWithJackson(T actual, T expected) {

        ObjectMapper mapper = new ObjectMapperConfig().customObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        String actualJson = mapper.writeValueAsString(actual);

        String expectedJson = mapper.writeValueAsString(expected);

        assertThat(actualJson).isEqualTo(expectedJson);
    }
}
