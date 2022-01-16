package cn.hangsman.operationlog.expression;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.*;
import org.springframework.expression.common.ExpressionUtils;
import org.springframework.expression.common.TemplateAwareExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by 2022/1/16 9:25
 *
 * @author hangsman
 * @since 1.0
 */
public class SpelFunctionExpressionParser extends TemplateAwareExpressionParser {

    private static final Pattern VALID_FUNCTION_EXPRESSION_PATTERN = Pattern.compile(".*\\$.*?[(].*?[)].*");

    private final ParserContext templateParserContext = new TemplateParserContext("{", "}");

    private final ParserContext functionParserContext = new TemplateParserContext("(", ")");

    private final SpelExpressionParser normalExpressionParser = new SpelExpressionParser();

    private final Map<String, SpelFunction> functionMap = new HashMap<>();

    public SpelFunctionExpressionParser(List<SpelFunction> functions) {
        if (!CollectionUtils.isEmpty(functions)) {
            for (SpelFunction parseFunction : functions) {
                String functionName = parseFunction.functionName();
                Assert.hasLength(functionName, "functionName can not be empty！");
                functionMap.put(functionName, parseFunction);
            }
        }
    }

    @Override
    public Expression parseExpression(String expressionString) throws ParseException {
        return this.parseExpression(expressionString, templateParserContext);
    }

    @Override
    protected Expression doParseExpression(String expressionString, ParserContext context) throws ParseException {
        if (VALID_FUNCTION_EXPRESSION_PATTERN.matcher(expressionString).matches()) {
            return doParseFunctionExpression(expressionString, functionParserContext);
        }
        return normalExpressionParser.parseExpression(expressionString);
    }

    private Expression doParseFunctionExpression(String expressionString, ParserContext context) {
        Map<String, Expression> variableExpressionMap = getVariableExpressionMap(expressionString, context);

        String originExpressionString = expressionString;
        // 将解析出来的方法替换成变量形式
        // #_ret != null ? $json(#_ret) : '' 将被替换成 #_ret != null ? #fun_uuid : ''
        for (Map.Entry<String, Expression> entry : variableExpressionMap.entrySet()) {
            Expression expression = entry.getValue();
            expressionString = expressionString.replace(expression.getExpressionString(), "#" + entry.getKey());
        }
        // 然后解析上面替换好的表达式
        Expression proxyExpression = doParseExpression(expressionString, null);
        // ProxyExpression 会在getValue的时候先处理 variableExpressionMap 中的表达式 然后将返回值放入 EvaluationContext
        return new ProxyExpression(originExpressionString, proxyExpression, variableExpressionMap);
    }

    /**
     * 提取方法中的变量 然后将变量解析成表达式
     */
    private Map<String, Expression> getVariableExpressionMap(String expressionString, ParserContext context) {
        String prefix = context.getExpressionPrefix();
        String suffix = context.getExpressionSuffix();
        Map<String, Expression> variableExpressionMap = new HashMap<>();
        int startIdx = 0;
        while (startIdx < expressionString.length()) {
            int $Index = expressionString.indexOf("$", startIdx);
            int prefixIndex = expressionString.indexOf(prefix, $Index);
            if (prefixIndex >= startIdx) {
                String functionName = expressionString.substring($Index + "$".length(), prefixIndex);
                Assert.hasLength(functionName, "functionName can not be empty:" + expressionString);
                int afterPrefixIndex = prefixIndex + prefix.length();
                int suffixIndex = ExpressionUtil.skipToCorrectEndSuffix(suffix, expressionString, afterPrefixIndex);
                if (suffixIndex == -1) {
                    throw new ParseException(expressionString, prefixIndex,
                            "No ending suffix '" + suffix + "' for expression starting at character " +
                                    prefixIndex + ": " + expressionString.substring(prefixIndex));
                }
                if (suffixIndex == afterPrefixIndex) {
                    throw new ParseException(expressionString, prefixIndex,
                            "No expression defined within delimiter '" + prefix + suffix +
                                    "' at character " + prefixIndex);
                }
                // 提取方法中的变量表达式
                String expr = expressionString.substring(prefixIndex + prefix.length(), suffixIndex);
                if (expr.isEmpty()) {
                    throw new ParseException(expressionString, prefixIndex,
                            "No expression defined within delimiter '" + prefix + suffix +
                                    "' at character " + prefixIndex);
                }
                List<Expression> expressions = new ArrayList<>();
                // 处理变量表达式 多个变量以逗号分隔 $json(1,2,3.....)
                for (String spel : expr.split(",")) {
                    expressions.add(doParseExpression(spel, null));
                }
                String functionExpressionStr = expressionString.substring($Index, suffixIndex + suffix.length());
                SpelFunction function = getFunction(functionName);
                Assert.notNull(function, "expression " + functionExpressionStr + " not find function :" + functionName);
                SpelFunctionExpression functionExpression =
                        new SpelFunctionExpression(functionExpressionStr, expressions.toArray(new Expression[0]), function);
                variableExpressionMap.put(generateVariableId(), functionExpression);
                startIdx = suffixIndex + suffix.length();
            } else {
                break;
            }
        }
        return variableExpressionMap;
    }

    private String generateVariableId() {
        return "fun_" + UUID.randomUUID().toString().replace("-", "");
    }

    private SpelFunction getFunction(String functionName) {
        return this.functionMap.get(functionName);
    }

    private static class ProxyExpression implements Expression {

        private final String expressionString;

        private final Expression proxyExpression;

        private final Map<String, Expression> variableExpressionMap;

        public ProxyExpression(String expressionString, Expression proxyExpression, Map<String, Expression> variableExpressionMap) {
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
            for (Map.Entry<String, Expression> entry : variableExpressionMap.entrySet()) {
                Expression expression = entry.getValue();
                Object value = expression.getValue(context);
                context.setVariable(entry.getKey(), value);
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
}
