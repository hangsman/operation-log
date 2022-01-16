package cn.hangsman.operationlog.spring.boot.annotation;

import cn.hangsman.operationlog.spring.boot.autoconfigure.OperationLogAutoConfiguration;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import java.lang.annotation.*;

/**
 * Created by 2022/1/11 13:30
 *
 * @author hangsman
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({OperationLogImportSelector.class, OperationLogAutoConfiguration.class})
public @interface EnableOperationLog {

    AdviceMode mode() default AdviceMode.PROXY;

    boolean proxyTargetClass() default false;

    int order() default Ordered.LOWEST_PRECEDENCE;
}
