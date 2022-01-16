package cn.hangsman.operationlog.expression;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.TypedValue;
import org.springframework.expression.common.ExpressionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 2022/1/16 9:24
 *
 * @author hangsman
 * @since 1.0
 */
public class SpelFunctionExpression implements Expression {

    private final String expressionStr;
    private final Expression[] expressions;
    private final SpelFunction function;

    public SpelFunctionExpression(String expressionStr, Expression[] expressions,
                                  SpelFunction function) {
        this.expressionStr = expressionStr;
        this.expressions = expressions;
        this.function = function;
    }

    @Override
    public String getExpressionString() {
        return this.expressionStr;
    }

    @Override
    public Object getValue(EvaluationContext context) throws EvaluationException {
        if (expressions.length == 1) {
            Expression expression = expressions[0];
            Object result = expression.getValue(context);
            return function.apply(result);
        } else {
            Map<String, Object> variableMap = new HashMap<>();
            for (int i = 0; i < expressions.length; i++) {
                Object result = expressions[i].getValue(context);
                variableMap.put("p" + i, result);
            }
            return function.apply(variableMap);
        }
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
