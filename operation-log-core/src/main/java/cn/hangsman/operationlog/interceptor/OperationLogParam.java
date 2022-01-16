package cn.hangsman.operationlog.interceptor;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

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

    Map<String, String> before;

}
