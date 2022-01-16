package cn.hangsman.operationlog.spring.boot.autoconfigure;

import cn.hangsman.operationlog.interceptor.BeanFactoryOperationLogSourceAdvisor;
import cn.hangsman.operationlog.interceptor.OperationLogInterceptor;
import cn.hangsman.operationlog.interceptor.OperationLogSource;
import cn.hangsman.operationlog.service.OperationLogRecorder;
import cn.hangsman.operationlog.service.OperatorService;
import cn.hangsman.operationlog.spring.boot.annotation.EnableOperationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Created by 2022/1/11 13:30
 *
 * @author hangsman
 * @since 1.0
 */
@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class OperationLogProxyConfiguration implements ImportAware {

    protected AnnotationAttributes enableOperationLog;

    @Bean
    public BeanFactoryOperationLogSourceAdvisor advisor() {
        BeanFactoryOperationLogSourceAdvisor advisor = new BeanFactoryOperationLogSourceAdvisor();
        advisor.setAdvice(operationLogInterceptor());
        advisor.setLogOperationSource(logOperationSource());
        if (this.enableOperationLog != null) {
            advisor.setOrder(this.enableOperationLog.<Integer>getNumber("order"));
        }
        return advisor;
    }

    @Bean
    public OperationLogInterceptor operationLogInterceptor() {
        OperationLogInterceptor interceptor = new OperationLogInterceptor();
        interceptor.setOperationSource(logOperationSource());
        return interceptor;
    }

    @Bean
    public OperationLogSource logOperationSource() {
        return new OperationLogSource();
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableOperationLog = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableOperationLog.class.getName()));
    }
}
