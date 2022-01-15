package cn.hangsman.operationlog.interceptor;

import cn.hangsman.operationlog.annotation.OperationLog;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;

/**
 * Created by 2022/1/14 21:28
 *
 * @author hangsman
 * @since 1.0
 */
public abstract class OperationLogSourcePointcut extends StaticMethodMatcherPointcut {


    public OperationLogSourcePointcut() {
        setClassFilter(new OperationLogClassFilter());
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        OperationLogSource operationSource = getLogOperationSource();
        return operationSource != null && !CollectionUtils.isEmpty(operationSource.getLogOperations(method, targetClass));
    }

    protected abstract OperationLogSource getLogOperationSource();


    private static class OperationLogClassFilter implements ClassFilter {
        @Override
        public boolean matches(Class<?> clazz) {
            return AnnotationUtils.isCandidateClass(clazz, OperationLog.class);
        }
    }


}
