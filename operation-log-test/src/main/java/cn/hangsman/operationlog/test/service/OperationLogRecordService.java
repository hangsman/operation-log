package cn.hangsman.operationlog.test.service;

import cn.hangsman.operationlog.OperationLog;
import cn.hangsman.operationlog.service.OperationLogRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by 2022/1/16 15:47
 *
 * @author hangsman
 * @since 1.0
 */
@Service
@Slf4j
public class OperationLogRecordService implements OperationLogRecorder {

    @Autowired
    IUserService userService;

    @Override
    public void record(OperationLog operationLog) {
        log.info(operationLog.toString());
    }
}
