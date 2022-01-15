package cn.hangsman.operationlog.interceptor;

import org.aopalliance.intercept.MethodInvocation;

/**
 * Created by 2022/1/14 22:13
 *
 * @author hangsman
 * @since 1.0
 */
class OperationLogInvoker {

    private final MethodInvocation invocation;
    private Object retValue;
    private ThrowableWrapper throwableWrapper;

    public OperationLogInvoker(MethodInvocation invocation) {
        this.invocation = invocation;
    }

    public void invoke() throws ThrowableWrapper {
        try {
            retValue = invocation.proceed();
        } catch (Throwable ex) {
            throwableWrapper = new ThrowableWrapper(ex);
        }
    }

    public Object getRetValue() {
        return retValue;
    }

    public ThrowableWrapper getThrowableWrapper() {
        return throwableWrapper;
    }

    static class ThrowableWrapper extends RuntimeException {

        private final Throwable original;

        public ThrowableWrapper(Throwable original) {
            super(original.getMessage(), original);
            this.original = original;
        }

        public Throwable getOriginal() {
            return this.original;
        }
    }
}
