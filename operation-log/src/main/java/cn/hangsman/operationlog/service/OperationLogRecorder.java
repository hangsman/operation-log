package cn.hangsman.operationlog.service;

import cn.hangsman.operationlog.OperationLog;

/**
 * Created by 2022/1/11 14:18
 *
 * @author hangsman
 * @since 1.0
 */
public interface OperationLogRecorder {
    void record(OperationLog operationLog);
}
