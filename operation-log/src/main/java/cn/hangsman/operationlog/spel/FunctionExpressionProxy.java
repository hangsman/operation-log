package cn.hangsman.operationlog.spel;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.TypedValue;
import org.springframework.expression.common.ExpressionUtils;

import java.util.Map;

/**
 * Created by 2022/1/12 15:42
 *
 * @author hangsman
 * @since 1.0
 */
public class FunctionExpressionProxy implements Expression {

    private final String expressionString;

    private final Expression proxyExpression;
    private final Map<String, Expression> variableExpressionMap;

    public FunctionExpressionProxy(String expressionString, Expression proxyExpression, Map<String, Expression> variableExpressionMap) {
        this.expressionString = expressionString;
        this.proxyExpression = proxyExpression;
        this.variableExpressionMap = variableExpressionMap;
    }

    @Override
    public String getExpressionString() {
        return this.expressionString;
    }

    @Override
    public Object getValue(EvaluationContext context) throws EvaluationException {
        for (String key : variableExpressionMap.keySet()) {
            Expression expression = variableExpressionMap.get(key);
            Object value = expression.getValue(context);
            context.setVariable(key, value);
        }
        return proxyExpression.getValue(context);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValue(EvaluationContext context, Class<T> desiredResultType) throws EvaluationException {
        Object result = getValue(context);
        if (desiredResultType == null) {
            return (T) result;
        } else {
            return ExpressionUtils.convertTypedValue(
                    context, new TypedValue(result), desiredResultType);
        }
    }

    @Override
    public Object getValue() throws EvaluationException {
        return null;
    }

    @Override
    public <T> T getValue(Class<T> desiredResultType) throws EvaluationException {
        return null;
    }

    @Override
    public Object getValue(Object rootObject) throws EvaluationException {
        return null;
    }

    @Override
    public <T> T getValue(Object rootObject, Class<T> desiredResultType) throws EvaluationException {
        return null;
    }

    @Override
    public Object getValue(EvaluationContext context, Object rootObject) throws EvaluationException {
        return null;
    }

    @Override
    public <T> T getValue(EvaluationContext context, Object rootObject, Class<T> desiredResultType) throws EvaluationException {
        return null;
    }

    @Override
    public Class<?> getValueType() throws EvaluationException {
        return null;
    }

    @Override
    public Class<?> getValueType(Object rootObject) throws EvaluationException {
        return null;
    }

    @Override
    public Class<?> getValueType(EvaluationContext context) throws EvaluationException {
        return null;
    }

    @Override
    public Class<?> getValueType(EvaluationContext context, Object rootObject) throws EvaluationException {
        return null;
    }

    @Override
    public TypeDescriptor getValueTypeDescriptor() throws EvaluationException {
        return null;
    }

    @Override
    public TypeDescriptor getValueTypeDescriptor(Object rootObject) throws EvaluationException {
        return null;
    }

    @Override
    public TypeDescriptor getValueTypeDescriptor(EvaluationContext context) throws EvaluationException {
        return null;
    }

    @Override
    public TypeDescriptor getValueTypeDescriptor(EvaluationContext context, Object rootObject) throws EvaluationException {
        return null;
    }

    @Override
    public boolean isWritable(Object rootObject) throws EvaluationException {
        return false;
    }

    @Override
    public boolean isWritable(EvaluationContext context) throws EvaluationException {
        return false;
    }

    @Override
    public boolean isWritable(EvaluationContext context, Object rootObject) throws EvaluationException {
        return false;
    }

    @Override
    public void setValue(Object rootObject, Object value) throws EvaluationException {

    }

    @Override
    public void setValue(EvaluationContext context, Object value) throws EvaluationException {

    }

    @Override
    public void setValue(EvaluationContext context, Object rootObject, Object value) throws EvaluationException {

    }
}
