package cn.hangsman.operationlog.interceptor;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractBeanFactoryPointcutAdvisor;

/**
 * Created by 2022/1/14 21:30
 *
 * @author hangsman
 * @since 1.0
 */
public class BeanFactoryOperationLogSourceAdvisor extends AbstractBeanFactoryPointcutAdvisor {

    private OperationLogSource operationLogSource;

    private final OperationLogSourcePointcut pointcut = new OperationLogSourcePointcut() {
        @Override
        protected OperationLogSource getLogOperationSource() {
            return BeanFactoryOperationLogSourceAdvisor.this.operationLogSource;
        }
    };

    public void setLogOperationSource(OperationLogSource operationLogSource) {
        this.operationLogSource = operationLogSource;
    }

    public void setClassFilter(ClassFilter classFilter) {
        this.pointcut.setClassFilter(classFilter);
    }

    public Pointcut getPointcut() {
        return this.pointcut;
    }
}
