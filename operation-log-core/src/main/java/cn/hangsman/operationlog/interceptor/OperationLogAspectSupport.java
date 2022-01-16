package cn.hangsman.operationlog.interceptor;

import cn.hangsman.operationlog.OperationLog;
import cn.hangsman.operationlog.expression.OperationLogExpressionEvaluator;
import cn.hangsman.operationlog.service.DefaultOperationLogRecorder;
import cn.hangsman.operationlog.service.DefaultOperatorService;
import cn.hangsman.operationlog.service.OperationLogRecorder;
import cn.hangsman.operationlog.service.OperatorService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 2022/1/14 22:16
 *
 * @author hangsman
 * @since 1.0
 */
@Getter
@Setter
public class OperationLogAspectSupport implements BeanFactoryAware, SmartInitializingSingleton {

    private final Map<LogOperationCacheKey, LogOperationMetadata> metadataCache = new ConcurrentHashMap<>(512);

    private OperatorService operatorService = new DefaultOperatorService();

    private OperationLogRecorder operationLogRecorder = new DefaultOperationLogRecorder();

    private OperationLogSource operationSource;

    private OperationLogExpressionEvaluator evaluator;

    private BeanFactory beanFactory;

    protected OperationLogInvoker execute(final OperationLogInvoker invoker, Object target, Method method, Object[] args) {
        Class<?> targetClass = getTargetClass(target);
        OperationLogSource operationSource = getOperationSource();
        Collection<OperationLogParam> operations = operationSource.getLogOperations(method, targetClass);
        if (!CollectionUtils.isEmpty(operations)) {
            OperationLogParam operation = operations.iterator().next();
            LogOperationContext operationContext = createLogOperationContext(operation, method, args, target, targetClass);
            if (operationContext.isConditionPassing()) {
                operationContext.resolveBeforeHandle();
                boolean invokeSuccess = invoke(invoker, operationContext.evaluationContext);
                recordLog(operationContext, invokeSuccess, new Date());
            }
        } else {
            invoker.invoke();
        }
        return invoker;
    }

    protected boolean invoke(OperationLogInvoker invoker, EvaluationContext evaluationContext) {
        invoker.invoke();
        evaluationContext.setVariable("_ret", invoker.getRetValue());
        evaluationContext.setVariable("_errorMsg", invoker.getThrowable() != null ? invoker.getThrowable().getMessage() : "");
        return invoker.getThrowable() == null;
    }

    protected void recordLog(LogOperationContext operationContext, boolean invokeSuccess, Date operationTime) {
        OperationLogParam operation = operationContext.metadata.operation;
        OperationLog.OperationLogBuilder builder = OperationLog.builder();
        builder.operator(this.operatorService.getOperator());
        builder.operationTime(operationTime);
        builder.category(operation.category);
        builder.detail(operationContext.parseTemplate(operation.detail));
        if (invokeSuccess) {
            builder.content(operationContext.parseTemplate(operation.content));
        } else {
            builder.fail(operationContext.parseTemplate(operation.fail));
        }
        this.operationLogRecorder.record(builder.build());
    }

    private Class<?> getTargetClass(Object target) {
        return AopProxyUtils.ultimateTargetClass(target);
    }

    protected LogOperationContext createLogOperationContext(
            OperationLogParam operation, Method method, Object[] args, Object target, Class<?> targetClass) {
        LogOperationMetadata metadata = getLogOperationMetadata(operation, method, targetClass);
        return new LogOperationContext(metadata, args, target);
    }

    protected LogOperationMetadata getLogOperationMetadata(OperationLogParam operation, Method method, Class<?> targetClass) {
        LogOperationCacheKey cacheKey = new LogOperationCacheKey(operation, method, targetClass);
        LogOperationMetadata metadata = this.metadataCache.get(cacheKey);
        if (metadata == null) {
            metadata = new LogOperationMetadata(operation, method, targetClass);
            this.metadataCache.put(cacheKey, metadata);
        }
        return metadata;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterSingletonsInstantiated() {
        try {
            setEvaluator(this.beanFactory.getBean(OperationLogExpressionEvaluator.class));
            setOperationLogRecorder(this.beanFactory.getBean(OperationLogRecorder.class));
            setOperatorService(this.beanFactory.getBean(OperatorService.class));
        } catch (NoUniqueBeanDefinitionException ex) {
            throw new IllegalStateException("no unique bean of type.", ex);
        } catch (NoSuchBeanDefinitionException ex) {
            throw new IllegalStateException("no bean of type found. ", ex);
        }
    }

    protected static class LogOperationMetadata {

        private final OperationLogParam operation;

        private final Method method;

        private final Method targetMethod;

        private final AnnotatedElementKey methodKey;

        public LogOperationMetadata(OperationLogParam operation, Method method, Class<?> targetClass) {
            this.operation = operation;
            this.method = BridgeMethodResolver.findBridgedMethod(method);
            this.targetMethod = (!Proxy.isProxyClass(targetClass) ?
                    AopUtils.getMostSpecificMethod(method, targetClass) : this.method);
            this.methodKey = new AnnotatedElementKey(this.targetMethod, targetClass);
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

    @Getter
    protected class LogOperationContext {

        private final LogOperationMetadata metadata;

        private final Object[] args;

        private final Object target;

        private final EvaluationContext evaluationContext;

        private Boolean conditionPassing;


        public LogOperationContext(LogOperationMetadata metadata, Object[] args, Object target) {
            this.metadata = metadata;
            this.args = extractArgs(metadata.method, args);
            this.target = target;
            this.evaluationContext = createEvaluationContext(metadata.targetMethod, args);
        }

        private EvaluationContext createEvaluationContext(Method targetMethod, Object[] args) {
            return evaluator.createEvaluationContext(targetMethod, args, beanFactory);
        }

        private Object[] extractArgs(Method method, Object[] args) {
            if (!method.isVarArgs()) {
                return args;
            }
            Object[] varArgs = ObjectUtils.toObjectArray(args[args.length - 1]);
            Object[] combinedArgs = new Object[args.length - 1 + varArgs.length];
            System.arraycopy(args, 0, combinedArgs, 0, args.length - 1);
            System.arraycopy(varArgs, 0, combinedArgs, args.length - 1, varArgs.length);
            return combinedArgs;
        }

        protected boolean isConditionPassing() {
            if (this.conditionPassing == null) {
                if (StringUtils.hasText(this.metadata.operation.getCondition())) {
                    this.conditionPassing = evaluator.condition(this.metadata.operation.getCondition(),
                            this.metadata.methodKey, evaluationContext);
                } else {
                    this.conditionPassing = true;
                }
            }
            return this.conditionPassing;
        }

        protected void resolveBeforeHandle() {
            metadata.operation.before.forEach((key, value) -> {
                Object result = evaluator.parseExpression(value, metadata.methodKey, evaluationContext, Object.class);
                evaluationContext.setVariable(key, result);
            });
        }

        protected String parseTemplate(String template) {
            return evaluator.parseExpression(template, metadata.methodKey, evaluationContext, String.class);
        }

    }
}
