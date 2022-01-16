package cn.hangsman.operationlog.annotation;

import cn.hangsman.operationlog.interceptor.OperationLogParam;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
        return ans.stream().map(an -> {
            Map<String, String> beforeHandles = new HashMap<>();
            for (String template : an.before()) {
                if (StringUtils.hasText(template)) {
                    int delimiterIndex = template.indexOf("=");
                    String variableName = template.substring(0, delimiterIndex);
                    String expressionStr = template.substring(delimiterIndex + 1);
                    beforeHandles.put(variableName, expressionStr);
                }
            }
            return OperationLogParam.builder()
                    .name(ae.toString())
                    .content(an.content())
                    .fail(an.fail())
                    .category(an.category())
                    .detail(an.detail())
                    .condition(an.condition())
                    .before(beforeHandles).build();
        }).collect(Collectors.toCollection(ArrayList::new));
    }

}
