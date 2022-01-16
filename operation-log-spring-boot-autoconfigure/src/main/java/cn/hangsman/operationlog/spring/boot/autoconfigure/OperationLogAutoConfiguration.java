package cn.hangsman.operationlog.spring.boot.autoconfigure;

import cn.hangsman.operationlog.expression.OperationLogExpressionEvaluator;
import cn.hangsman.operationlog.expression.SpelFunction;
import cn.hangsman.operationlog.expression.SpelFunctionExpressionParser;
import cn.hangsman.operationlog.service.DefaultOperationLogRecorder;
import cn.hangsman.operationlog.service.DefaultOperatorService;
import cn.hangsman.operationlog.service.OperationLogRecorder;
import cn.hangsman.operationlog.service.OperatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
    public SpelFunctionExpressionParser spelFunctionExpressionParser(@Autowired(required = false) List<SpelFunction> parseFunctions) {
        return new SpelFunctionExpressionParser(parseFunctions);
    }

    @Bean
    public OperationLogExpressionEvaluator operationLogExpressionEvaluator(SpelFunctionExpressionParser expressionParser) {
        return new OperationLogExpressionEvaluator(expressionParser);
    }

    @Bean
    @ConditionalOnMissingBean(OperationLogRecorder.class)
    public OperationLogRecorder operationLogRecorder() {
        return new DefaultOperationLogRecorder();
    }

    @Bean
    @ConditionalOnMissingBean(OperatorService.class)
    public OperatorService operatorService() {
        return new DefaultOperatorService();
    }
}
