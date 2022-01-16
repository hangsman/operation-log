package cn.hangsman.operationlog.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.Assert;

import java.lang.reflect.Method;

/**
 * Created by 2022/1/14 22:15
 *
 * @author hangsman
 * @since 1.0
 */
public class OperationLogInterceptor extends OperationLogAspectSupport implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        OperationLogInvoker invoker = new OperationLogInvoker(invocation);
        Object target = invocation.getThis();
        Assert.state(target != null, "Target must not be null");
        try {
            return execute(invoker, target, method, invocation.getArguments());
        } catch (OperationLogInvoker.ThrowableWrapper th) {
            throw th.getOriginal();
        }
    }
}
