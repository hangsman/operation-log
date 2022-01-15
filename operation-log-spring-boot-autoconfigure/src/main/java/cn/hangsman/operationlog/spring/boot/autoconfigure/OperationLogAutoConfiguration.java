package cn.hangsman.operationlog.spring.boot.autoconfigure;

import cn.hangsman.operationlog.spel.SpelFunction;
import cn.hangsman.operationlog.spel.SpelFunctionExpressionParser;
import cn.hangsman.operationlog.spel.SpelFunctionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Created by 2022/1/15 14:26
 *
 * @author hangsman
 * @since 1.0
 */
@Configuration(proxyBeanMethods = false)
public class OperationLogAutoConfiguration {

    @Bean
    SpelFunctionExpressionParser spelFunctionExpressionParser() {
        return new SpelFunctionExpressionParser();
    }

    @Bean
    public SpelFunctionFactory spelFunctionFactory(@Autowired(required = false) List<SpelFunction> parseFunctions) {
        return new SpelFunctionFactory(parseFunctions);
    }
}
