package cn.hangsman.operationlog.interceptor;

import cn.hangsman.operationlog.annotation.OperationLogAnnotationParser;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.MethodClassKey;
import org.springframework.util.ClassUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 2022/1/14 21:14
 *
 * @author hangsman
 * @since 1.0
 */

public class OperationLogSource {

    private static final Collection<OperationLogParam> NULL_OPERATION_ATTRIBUTE = Collections.emptyList();

    private final Map<Comparable<?>, Collection<OperationLogParam>> attributeCache = new ConcurrentHashMap<>(128);

    private OperationLogAnnotationParser annotationParser = new OperationLogAnnotationParser();

    public Collection<OperationLogParam> getLogOperations(Method method, Class<?> targetClass) {
        if (method.getDeclaringClass() == Object.class) {
            return null;
        }
        Comparable<?> cacheKey = getCacheKey(method, targetClass);
        Collection<OperationLogParam> cached = this.attributeCache.get(cacheKey);
        if (cached != null) {
            return (cached != NULL_OPERATION_ATTRIBUTE ? cached : null);
        } else {
            Collection<OperationLogParam> operations = computeLogOperations(method, targetClass);
            if (operations != null) {
                this.attributeCache.put(cacheKey, operations);
            } else {
                this.attributeCache.put(cacheKey, NULL_OPERATION_ATTRIBUTE);
            }
            return operations;
        }
    }

    public Collection<OperationLogParam> computeLogOperations(Method method, Class<?> targetClass) {
        if (!Modifier.isPublic(method.getModifiers())) {
            return null;
        }
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        Collection<OperationLogParam> operations = findOperationLogParams(specificMethod);
        if (operations != null) {
            return operations;
        }
        operations = findOperationLogParams(specificMethod.getDeclaringClass());
        if (operations != null && ClassUtils.isUserLevelMethod(method)) {
            return operations;
        }
        if (specificMethod != method) {
            operations = findOperationLogParams(method);
            if (operations != null) {
                return operations;
            }
            operations = findOperationLogParams(method.getDeclaringClass());
            if (operations != null && ClassUtils.isUserLevelMethod(method)) {
                return operations;
            }
        }
        return null;
    }

    protected Collection<OperationLogParam> findOperationLogParams(AnnotatedElement ae) {
        return annotationParser.parseAnnotations(ae);
    }

    protected Collection<OperationLogParam> findOperationLogParams(Class<?> clazz) {
        return annotationParser.parseAnnotations(clazz);
    }

    protected Comparable<?> getCacheKey(Method method, Class<?> targetClass) {
        return new MethodClassKey(method, targetClass);
    }

    public void setAnnotationParser(OperationLogAnnotationParser annotationParser) {
        this.annotationParser = annotationParser;
    }
}
