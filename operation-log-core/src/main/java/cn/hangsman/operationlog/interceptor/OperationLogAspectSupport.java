package cn.hangsman.operationlog.interceptor;

import cn.hangsman.operationlog.OperationLog;

import cn.hangsman.operationlog.service.DefaultOperationLogRecorder;
import cn.hangsman.operationlog.service.DefaultOperatorService;
import cn.hangsman.operationlog.service.OperationLogRecorder;
import cn.hangsman.operationlog.service.OperatorService;
import cn.hangsman.operationlog.spel.SpelFunctionExpressionParser;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 2022/1/14 22:16
 *
 * @author hangsman
 * @since 1.0
 */
public class OperationLogAspectSupport implements InitializingBean {

    private final Map<LogOperationCacheKey, LogOperationMetadata> metadataCache = new ConcurrentHashMap<>(512);

    private OperatorService operatorService = new DefaultOperatorService();

    private OperationLogRecorder operationLogRecorder = new DefaultOperationLogRecorder();

    private OperationLogSource operationSource;

    private SpelFunctionExpressionParser expressionParser;

    protected Object execute(OperationLogInvoker invoker, Object target, Method method, Object[] args) {
        Class<?> targetClass = getTargetClass(target);
        OperationLogSource operationSource = getOperationSource();
        Collection<OperationLogParam> operations = operationSource.getLogOperations(method, targetClass);
        if (!CollectionUtils.isEmpty(operations)) {
            OperationLogParam operation = operations.iterator().next();
            EvaluationContext evaluationContext = getExpressionParser().createEvaluationContext(method, args);
            LogOperationMetadata metadata = getLogOperationMetadata(operation, method, targetClass);
            recordLog(invoker, metadata, evaluationContext);
        } else {
            invoker.invoke();
        }
        if (invoker.getThrowableWrapper() != null) {
            throw invoker.getThrowableWrapper();
        }
        return invoker.getRetValue();
    }

    private void recordLog(final OperationLogInvoker invoker, LogOperationMetadata metadata, EvaluationContext evaluationContext) {
        for (String variableName : metadata.variableExpressionMap.keySet()) {
            Expression expression = metadata.variableExpressionMap.get(variableName);
            evaluationContext.setVariable(variableName, expression.getValue(evaluationContext));
        }
        Date operationTime = new Date();
        invoker.invoke();
        evaluationContext.setVariable("_ret", invoker.getRetValue());
        OperationLogInvoker.ThrowableWrapper throwableWrapper = invoker.getThrowableWrapper();
        evaluationContext.setVariable("_errorMsg", throwableWrapper != null ? throwableWrapper.getMessage() : null);
        if (isConditionPassing(metadata, evaluationContext)) {
            OperationLog.OperationLogBuilder builder = OperationLog.builder();
            Map<String, Expression> expressionMap = metadata.templateExpressionMap;
            OperationLogParam operation = metadata.operation;
            if (throwableWrapper == null) {
                builder.content(expressionMap.get(operation.content).getValue(evaluationContext, String.class));
            } else {
                builder.fail(expressionMap.get(operation.fail).getValue(evaluationContext, String.class));
            }
            builder.detail(expressionMap.get(operation.detail).getValue(evaluationContext, String.class));
            builder.operator(getOperatorService().getOperator());
            builder.operatingTime(operationTime);
            builder.category(metadata.operation.category);
            this.operationLogRecorder.record(builder.build());
        }
    }


    private boolean isConditionPassing(LogOperationMetadata metadata, EvaluationContext evaluationContext) {
        if (StringUtils.hasText(metadata.operation.condition)) {
            Expression expression = metadata.templateExpressionMap.get(metadata.operation.condition);
            Boolean expressionValue = expression.getValue(evaluationContext, Boolean.class);
            return Boolean.TRUE.equals(expressionValue);
        }
        return true;
    }

    private Class<?> getTargetClass(Object target) {
        return AopProxyUtils.ultimateTargetClass(target);
    }

    protected LogOperationMetadata getLogOperationMetadata(OperationLogParam operation, Method method, Class<?> targetClass) {
        LogOperationCacheKey cacheKey = new LogOperationCacheKey(operation, method, targetClass);
        LogOperationMetadata metadata = this.metadataCache.get(cacheKey);
        if (metadata == null) {
            Map<String, Expression> variableExpressionMap = new HashMap<>();
            for (String template : operation.before) {
                if (StringUtils.hasText(template)) {
                    int delimiterIndex = template.indexOf("=");
                    String variableName = template.substring(0, delimiterIndex);
                    String expressionStr = template.substring(delimiterIndex + 1);
                    Expression expression = getExpressionParser().parseExpression(expressionStr);
                    variableExpressionMap.put(variableName, expression);
                }
            }
            Map<String, Expression> templateExpressionMap = new HashMap<>();
            templateExpressionMap.put(operation.content, getExpressionParser().parseExpression(operation.content));
            templateExpressionMap.put(operation.fail, getExpressionParser().parseExpression(operation.fail));
            templateExpressionMap.put(operation.detail, getExpressionParser().parseExpression(operation.detail));
            templateExpressionMap.put(operation.condition, getExpressionParser().parseExpression(operation.condition));
            metadata = new LogOperationMetadata(operation, variableExpressionMap, templateExpressionMap);
            this.metadataCache.put(cacheKey, metadata);
        }
        return metadata;
    }

    public OperatorService getOperatorService() {
        return operatorService;
    }

    public void setOperatorService(OperatorService operatorService) {
        this.operatorService = operatorService;
    }

    public void setOperationLogRecorder(OperationLogRecorder operationLogRecorder) {
        this.operationLogRecorder = operationLogRecorder;
    }

    public OperationLogSource getOperationSource() {
        return operationSource;
    }

    public void setOperationSource(OperationLogSource operationSource) {
        this.operationSource = operationSource;
    }

    public SpelFunctionExpressionParser getExpressionParser() {
        return expressionParser;
    }

    public void setExpressionParser(SpelFunctionExpressionParser expressionParser) {
        this.expressionParser = expressionParser;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(expressionParser, "expressionParser can not be empty");
        Assert.notNull(operatorService, "expressionEvaluator can not be empty");
        Assert.notNull(operationLogRecorder, "OperationLogRecorder can not be empty");
    }


    protected static class LogOperationMetadata {

        private final OperationLogParam operation;

        private final Map<String, Expression> variableExpressionMap;

        private final Map<String, Expression> templateExpressionMap;

        public LogOperationMetadata(OperationLogParam operation, Map<String, Expression> variableExpressionMap, Map<String, Expression> templateExpressionMap) {
            this.operation = operation;
            this.variableExpressionMap = variableExpressionMap;
            this.templateExpressionMap = templateExpressionMap;
        }
    }

    private static final class LogOperationCacheKey implements Comparable<LogOperationCacheKey> {

        private final OperationLogParam operation;

        private final AnnotatedElementKey methodCacheKey;

        private LogOperationCacheKey(OperationLogParam operationLogParam, Method method, Class<?> targetClass) {
            this.operation = operationLogParam;
            this.methodCacheKey = new AnnotatedElementKey(method, targetClass);
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof LogOperationCacheKey)) {
                return false;
            }
            LogOperationCacheKey otherKey = (LogOperationCacheKey) other;
            return (this.operation.equals(otherKey.operation) &&
                    this.methodCacheKey.equals(otherKey.methodCacheKey));
        }

        @Override
        public int hashCode() {
            return (this.operation.hashCode() * 31 + this.methodCacheKey.hashCode());
        }

        @Override
        public String toString() {
            return this.operation + " on " + this.methodCacheKey;
        }

        @Override
        public int compareTo(LogOperationCacheKey other) {
            int result = this.operation.getName().compareTo(other.operation.getName());
            if (result == 0) {
                result = this.methodCacheKey.compareTo(other.methodCacheKey);
            }
            return result;
        }
    }
}
