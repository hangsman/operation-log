package cn.hangsman.operationlog.test.spel;

import cn.hangsman.operationlog.spel.SpelFunction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by 2022/1/14 17:28
 *
 * @author hangsman
 * @since 1.0
 */
@Component
@Slf4j
public class JsonSpelFunction implements SpelFunction {

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public Object apply(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("serialization failed", e);
        }
        return "";
    }

    @Override
    public String functionName() {
        return "json";
    }
}
