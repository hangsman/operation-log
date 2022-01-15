package cn.hangsman.operationlog.spel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.*;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.expression.common.TemplateAwareExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by 2022/1/12 9:58
 *
 * @author hangsman
 * @since 1.0
 */
@Slf4j
public class SpelFunctionExpressionParser extends TemplateAwareExpressionParser implements SmartInitializingSingleton, BeanFactoryAware {

    private static final String FUNCTION_VARIABLE_PREFIX = "$";

    private final LiteralExpression EMPTY_EXPRESSION = new LiteralExpression("");

    private final ParserContext functionParserContext = new TemplateParserContext("(", ")");

    private final ParserContext templateParserContext = new TemplateParserContext("{", "}");

    private final SpelExpressionParser spelExpressionParser = new SpelExpressionParser();

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private SpelFunctionFactory functionFactory;

    private BeanFactory beanFactory;

    public EvaluationContext createEvaluationContext(Method method, Object[] arguments) {
        MethodBasedEvaluationContext evaluationContext = new MethodBasedEvaluationContext(
                TypedValue.NULL, method, arguments, this.parameterNameDiscoverer);
        if (beanFactory != null) {
            evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
        }
        return evaluationContext;
    }

    @Override
    public Expression parseExpression(String expressionString) throws ParseException {
        if (!StringUtils.hasText(expressionString)) {
            return EMPTY_EXPRESSION;
        }
        try {
            return this.parseExpression(expressionString, templateParserContext);
        } catch (Exception e) {
            log.error("parse expression error : {}", expressionString);
            throw e;
        }
    }

    @Override
    protected Expression doParseExpression(String expressionString, ParserContext context) throws ParseException {
        if (expressionString.contains(FUNCTION_VARIABLE_PREFIX)) {
            return doParseFunctionExpression(expressionString, functionParserContext);
        } else {
            return spelExpressionParser.parseExpression(expressionString);
        }
    }

    private Expression doParseFunctionExpression(String expressionString, ParserContext context) {
        String prefix = context.getExpressionPrefix();
        String suffix = context.getExpressionSuffix();
        Map<String, Expression> variableExpressionMap = new HashMap<>();
        int startIdx = 0;
        while (startIdx < expressionString.length()) {
            int $Index = expressionString.indexOf(FUNCTION_VARIABLE_PREFIX, startIdx);
            int prefixIndex = expressionString.indexOf(prefix, $Index);
            if (prefixIndex >= startIdx) {
                String functionName = expressionString.substring($Index + FUNCTION_VARIABLE_PREFIX.length(), prefixIndex);
                Assert.hasLength(functionName, "functionName can not be empty:" + expressionString);
                int afterPrefixIndex = prefixIndex + prefix.length();
                int suffixIndex = SpelUtil.skipToCorrectEndSuffix(suffix, expressionString, afterPrefixIndex);
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
                String expr = expressionString.substring(prefixIndex + prefix.length(), suffixIndex);
                if (expr.isEmpty()) {
                    throw new ParseException(expressionString, prefixIndex,
                            "No expression defined within delimiter '" + prefix + suffix +
                                    "' at character " + prefixIndex);
                }
                List<Expression> expressions = new ArrayList<>();
                for (String spel : expr.split(",")) {
                    expressions.add(doParseExpression(spel, null));
                }
                String expressionStr = FUNCTION_VARIABLE_PREFIX + functionName + prefix + expr + suffix;
                SpelFunction function = functionFactory.getFunction(functionName);
                Assert.notNull(function, "could not find function :" + functionName);
                FunctionExpression functionExpression = new FunctionExpression(expressionStr, expressions.toArray(new Expression[0]), function);
                String variableId = UUID.randomUUID().toString().replace("-", "");
                variableExpressionMap.put("function_" + variableId, functionExpression);
                startIdx = suffixIndex + suffix.length();
            } else {
                break;
            }
        }
        String originExpressionString = expressionString;
        for (String key : variableExpressionMap.keySet()) {
            Expression expression = variableExpressionMap.get(key);
            expressionString = expressionString.replace(expression.getExpressionString(), "#" + key);
        }
        Expression proxyExpression = doParseExpression(expressionString, null);
        return new FunctionExpressionProxy(originExpressionString, proxyExpression, variableExpressionMap);
    }

    @Override
    public void afterSingletonsInstantiated() {
        try {
            setFunctionFactory(this.beanFactory.getBean(SpelFunctionFactory.class));
        } catch (NoUniqueBeanDefinitionException ex) {
            throw new IllegalStateException("no unique bean of type " +
                    "SpelFunctionFactory found. Mark one as primary or declare a specific SpelFunctionFactory to use.", ex);
        } catch (NoSuchBeanDefinitionException ex) {
            throw new IllegalStateException("no bean of type SpelFunctionFactory found. " +
                    "Register a SpelFunctionFactory bean or remove the @EnableOperationLog annotation from your configuration.", ex);
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public void setFunctionFactory(SpelFunctionFactory functionFactory) {
        this.functionFactory = functionFactory;
    }
}
