package cn.hangsman.operationlog.expression;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.TypedValue;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 2022/1/16 9:20
 *
 * @author hangsman
 * @since 1.0
 */
public class OperationLogExpressionEvaluator extends CachedExpressionEvaluator {

    private final Map<ExpressionKey, Expression> expressionCache = new ConcurrentHashMap<>(64);

    public OperationLogExpressionEvaluator(ExpressionParser parser) {
        super(parser);
    }

    public EvaluationContext createEvaluationContext(Method method, Object[] arguments, BeanFactory beanFactory) {
        MethodBasedEvaluationContext evaluationContext = new MethodBasedEvaluationContext(
                TypedValue.NULL, method, arguments, getParameterNameDiscoverer());
        if (beanFactory != null) {
            evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
        }
        return evaluationContext;
    }

    public <T> T parseExpression(String expressionStr, AnnotatedElementKey methodKey, EvaluationContext evaluationContext, Class<T> desiredResultType) {
        return getExpression(this.expressionCache, methodKey, expressionStr).getValue(evaluationContext, desiredResultType);
    }

    public boolean condition(String conditionExpression, AnnotatedElementKey methodKey, EvaluationContext evalContext) {
        return (Boolean.TRUE.equals(parseExpression(conditionExpression, methodKey, evalContext, Boolean.class)));
    }
}
