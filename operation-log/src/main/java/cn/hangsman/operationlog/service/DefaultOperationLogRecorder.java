package cn.hangsman.operationlog.service;

import cn.hangsman.operationlog.OperationLog;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by 2022/1/11 14:19
 *
 * @author hangsman
 * @since 1.0
 */
@Slf4j
public class DefaultOperationLogRecorder implements OperationLogRecorder {

    @Override
    public void record(OperationLog operationLog) {
        log.info(operationLog.toString());
    }

}
