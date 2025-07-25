package ai.data.pipeline.spring.sink;

import ai.data.pipeline.spring.properties.SqlConsumerProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

@Component
@Slf4j
public class PostgresConsumer implements Consumer<String> {

    private final ObjectMapper objectMapper;
    private final NamedParameterJdbcTemplate  namedParameterJdbcTemplate;
    private final String sql;

    public PostgresConsumer(ObjectMapper objectMapper,
                            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
                            SqlConsumerProperties properties) {
        this.objectMapper = objectMapper;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.sql = properties.getSql();
    }

    @SneakyThrows
    @Override
    public void accept(String payload) {

        log.info("payload: {}",payload);
        Map<String,Object> map = objectMapper.readValue(payload,Map.class);

        map.put("payload",payload);

        log.info("map: {} payload: {}",map,payload);
        log.info("SQL: {}",sql);
        namedParameterJdbcTemplate.update(sql,map);
    }
}
