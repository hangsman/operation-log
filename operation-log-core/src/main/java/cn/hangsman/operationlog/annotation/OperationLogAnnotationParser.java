package cn.hangsman.operationlog.annotation;

import cn.hangsman.operationlog.interceptor.OperationLogParam;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by 2022/1/14 21:27
 *
 * @author hangsman
 * @since 1.0
 */
public class OperationLogAnnotationParser {

    public Collection<OperationLogParam> parseAnnotations(AnnotatedElement ae) {
        Collection<OperationLogParam> ops = parseAnnotations(ae, false);
        if (ops != null && ops.size() > 1) {
            Collection<OperationLogParam> localOps = parseAnnotations(ae, true);
            if (localOps != null) {
                return localOps;
            }
        }
        return ops;
    }

    private Collection<OperationLogParam> parseAnnotations(AnnotatedElement ae, boolean localOnly) {
        Collection<OperationLog> ans = (localOnly ?
                AnnotatedElementUtils.getAllMergedAnnotations(ae, OperationLog.class) :
                AnnotatedElementUtils.findAllMergedAnnotations(ae, OperationLog.class));
        if (ans.isEmpty()) {
            return null;
        }
        return ans.stream().map(an -> OperationLogParam.builder()
                .name(ae.toString())
                .content(an.content())
                .fail(an.fail())
                .category(an.category())
                .detail(an.detail())
                .condition(an.condition())
                .before(paresToMap(an.before()))
                .additional(paresToMap(an.additional()))
                .build()).collect(Collectors.toCollection(ArrayList::new));
    }


    /**
     * 将 变量名={spel表达式}解析为键值对形式
     */
    private Map<String, String> paresToMap(String[] templates) {
        if (ObjectUtils.isEmpty(templates)) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new HashMap<>();
        for (String template : templates) {
            if (StringUtils.hasText(template)) {
                int delimiterIndex = template.indexOf("=");
                String variableName = template.substring(0, delimiterIndex);
                String expressionStr = template.substring(delimiterIndex + 1);
                map.put(variableName, expressionStr);
            }
        }
        return map;
    }
}
