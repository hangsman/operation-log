package cn.hangsman.operationlog.interceptor;

import lombok.Getter;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Created by 2022/1/14 22:13
 *
 * @author hangsman
 * @since 1.0
 */
@Getter
class OperationLogInvoker {

    private final MethodInvocation invocation;
    private Object retValue;
    private Throwable throwable;

    public OperationLogInvoker(MethodInvocation invocation) {
        this.invocation = invocation;
    }

    public void invoke() {
        try {
            retValue = invocation.proceed();
        } catch (Throwable ex) {
            throwable = ex;
        }
    }

}
