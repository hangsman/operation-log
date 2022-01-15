package cn.hangsman.operationlog.spring.boot.annotation;

import cn.hangsman.operationlog.spring.boot.autoconfigure.OperationLogProxyConfiguration;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.context.annotation.AutoProxyRegistrar;

/**
 * Created by 2022/1/11 15:38
 *
 * @author hangsman
 * @since 1.0
 */
public class OperationLogImportSelector extends AdviceModeImportSelector<EnableOperationLog> {

    @Override
    protected String[] selectImports(AdviceMode adviceMode) {
        switch (adviceMode) {
            case PROXY:
                return new String[]{AutoProxyRegistrar.class.getName(), OperationLogProxyConfiguration.class.getName()};
            case ASPECTJ:
                return new String[]{"cn.hangsman.operationlog.spring.boot.autoconfigure.OperationLogProxyAutoConfiguration"};
            default:
                return null;
        }
    }
}
