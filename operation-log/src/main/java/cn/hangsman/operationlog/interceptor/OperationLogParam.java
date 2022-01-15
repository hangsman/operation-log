package cn.hangsman.operationlog.interceptor;

import lombok.Builder;
import lombok.Data;

/**
 * Created by 2022/1/14 21:13
 *
 * @author hangsman
 * @since 1.0
 */
@Data
@Builder
public class OperationLogParam {

    String name;

    String content;

    String fail;

    String category;

    String detail;

    String condition;

    String[] before;

}
